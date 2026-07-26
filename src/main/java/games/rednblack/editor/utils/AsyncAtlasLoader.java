package games.rednblack.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.utils.Array;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Loads a folder of texture atlases without freezing the editor.
 * <p>
 * {@code new TextureAtlas(file)} bundles two very different jobs: parsing the {@code .atlas}
 * descriptor and decoding its PNG pages (plain CPU work, no GL involved) and uploading those pages
 * as GL textures (render thread only). On a project with big packs the decoding is the bulk of the
 * time, so here it runs on a worker pool while the render thread only uploads, a few milliseconds
 * per frame. This is the same split libGDX's own {@code TextureAtlasLoader} does for
 * {@code AssetManager}, and it works because {@link TextureAtlas#load(TextureAtlasData)} reuses a
 * page's {@link Page#texture} when it is already set instead of reading the file again.
 * <p>
 * Construct, then {@link #start()} from the render thread; every {@link Listener} callback comes
 * back on the render thread.
 */
public class AsyncAtlasLoader {

    public interface Listener {
        /** Fraction of the pages already uploaded, {@code 0..1}. */
        void onProgress(float progress);

        /** Atlases keyed by pack file name (without extension), in folder order. */
        void onFinished(Map<String, TextureAtlas> atlases);

        /** Nothing is left loaded when this is called. */
        void onFailed(Throwable error);
    }

    /** Cap, in megabytes, on decoded pages waiting for the render thread — decoders block above it. */
    private static final int IN_FLIGHT_BUDGET_MB = 256;

    /** How long a single frame may spend uploading pages. */
    private static final long UPLOAD_BUDGET_NANOS = 6_000_000L;

    private final FileHandle packFolder;
    private final Listener listener;
    private final ExecutorService decoders;
    private final int decoderThreads;

    private final ConcurrentLinkedQueue<PreparedPage> uploadQueue = new ConcurrentLinkedQueue<>();
    private final Semaphore inFlightBudget = new Semaphore(IN_FLIGHT_BUDGET_MB);

    private volatile Array<Pack> packs;
    /** Written after {@link #packs}, so reading it non-negative also publishes them. */
    private volatile int totalPages = -1;
    private volatile Throwable error;
    private volatile boolean cancelled;

    private final AtomicLong readNanos = new AtomicLong();
    private final AtomicLong decodeNanos = new AtomicLong();

    private int uploadedPages;
    private long startedAt;
    private long parseNanos;
    private long uploadNanos;

    public AsyncAtlasLoader(String packFolderPath, Listener listener) {
        this.packFolder = new FileHandle(packFolderPath);
        this.listener = listener;

        this.decoderThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
        this.decoders = Executors.newFixedThreadPool(decoderThreads, runnable -> {
            Thread thread = new Thread(runnable, "AtlasDecoder");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        startedAt = System.nanoTime();

        Thread parser = new Thread(this::parseAndDecode, "AtlasLoader");
        parser.setDaemon(true);
        parser.start();

        Gdx.app.postRunnable(this::pump);
    }

    /** Worker side: parse every descriptor, then hand each page to the decoder pool. */
    private void parseAndDecode() {
        try {
            long parseStartedAt = System.nanoTime();
            Array<Pack> parsed = new Array<>();
            int pages = 0;
            if (packFolder.exists()) {
                for (FileHandle file : packFolder.list()) {
                    if (!file.extension().equals("atlas")) continue;
                    TextureAtlasData data = new TextureAtlasData(file, file.parent(), false);
                    parsed.add(new Pack(file.nameWithoutExtension(), data));
                    pages += data.getPages().size;
                }
            }
            parseNanos = System.nanoTime() - parseStartedAt;
            packs = parsed;
            totalPages = pages;

            for (Pack pack : parsed) {
                for (Page page : pack.data.getPages())
                    decoders.execute(() -> decode(page));
            }
        } catch (Throwable t) {
            error = t;
        }
    }

    private void decode(Page page) {
        int cost = pageCostMB(page);
        boolean acquired = false;
        try {
            if (cancelled || error != null) return;
            inFlightBudget.acquire(cost);
            acquired = true;
            if (cancelled || error != null) return;

            // Read and decode are separate steps only so the two can be timed apart: on a big project
            // it matters whether the wait is the disk or the PNG decoder.
            long readAt = System.nanoTime();
            byte[] encoded = page.textureFile.readBytes();
            long decodeAt = System.nanoTime();
            Pixmap pixmap = new Pixmap(encoded, 0, encoded.length);
            long decodedAt = System.nanoTime();
            readNanos.addAndGet(decodeAt - readAt);
            decodeNanos.addAndGet(decodedAt - decodeAt);

            TextureData data = new FileTextureData(page.textureFile, pixmap, page.format, page.useMipMaps);
            data.prepare();
            uploadQueue.add(new PreparedPage(page, data, cost));
            acquired = false;
        } catch (Throwable t) {
            error = t;
        } finally {
            if (acquired) inFlightBudget.release(cost);
        }
    }

    /** Render thread: upload whatever the decoders have ready, within this frame's budget. */
    private void pump() {
        if (error != null) {
            abort();
            return;
        }

        long uploadStartedAt = System.nanoTime();
        long deadline = uploadStartedAt + UPLOAD_BUDGET_NANOS;
        PreparedPage prepared;
        while ((prepared = uploadQueue.poll()) != null) {
            prepared.page.texture = new Texture(prepared.data);
            inFlightBudget.release(prepared.costMB);
            uploadedPages++;
            if (System.nanoTime() > deadline) break;
        }
        uploadNanos += System.nanoTime() - uploadStartedAt;

        int total = totalPages;
        if (total >= 0) {
            listener.onProgress(total == 0 ? 1f : (float) uploadedPages / total);
            if (uploadedPages == total) {
                finish();
                return;
            }
        }

        Gdx.app.postRunnable(this::pump);
    }

    private void finish() {
        decoders.shutdown();
        System.out.println("Loaded " + uploadedPages + " atlas pages in " + ms(System.nanoTime() - startedAt)
                + "ms (parse " + ms(parseNanos)
                + "ms | read " + ms(readNanos.get()) + "ms + decode " + ms(decodeNanos.get())
                + "ms over " + decoderThreads + " threads | upload " + ms(uploadNanos) + "ms)");

        Map<String, TextureAtlas> atlases = new LinkedHashMap<>();
        try {
            for (Pack pack : packs)
                atlases.put(pack.name, new TextureAtlas(pack.data));
        } catch (Throwable t) {
            for (TextureAtlas atlas : atlases.values())
                atlas.dispose();
            disposeUploadedPages();
            listener.onFailed(t);
            return;
        }

        listener.onFinished(atlases);
    }

    private void abort() {
        Throwable cause = error;
        cancelled = true;
        decoders.shutdownNow();

        PreparedPage prepared;
        while ((prepared = uploadQueue.poll()) != null) {
            Pixmap pixmap = prepared.data.consumePixmap();
            if (pixmap != null && prepared.data.disposePixmap()) pixmap.dispose();
        }
        disposeUploadedPages();

        listener.onFailed(cause);
    }

    /** Pages uploaded before the failure are owned by nothing yet, so they have to go back manually. */
    private void disposeUploadedPages() {
        Array<Pack> parsed = packs;
        if (parsed == null) return;
        for (Pack pack : parsed) {
            for (Page page : pack.data.getPages()) {
                if (page.texture == null) continue;
                page.texture.dispose();
                page.texture = null;
            }
        }
    }

    private static long ms(long nanos) {
        return nanos / 1_000_000L;
    }

    private static int pageCostMB(Page page) {
        long bytes = (long) page.width * (long) page.height * 4L;
        int megabytes = (int) (bytes / (1024L * 1024L));
        return Math.max(1, Math.min(IN_FLIGHT_BUDGET_MB, megabytes));
    }

    private static class Pack {
        final String name;
        final TextureAtlasData data;

        Pack(String name, TextureAtlasData data) {
            this.name = name;
            this.data = data;
        }
    }

    private static class PreparedPage {
        final Page page;
        final TextureData data;
        final int costMB;

        PreparedPage(Page page, TextureData data, int costMB) {
            this.page = page;
            this.data = data;
            this.costMB = costMB;
        }
    }
}
