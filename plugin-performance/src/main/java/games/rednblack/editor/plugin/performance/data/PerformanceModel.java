package games.rednblack.editor.plugin.performance.data;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.ecs.Aspect;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.EntitySubscription;
import games.rednblack.editor.renderer.ecs.SystemInvocationStrategy;
import games.rednblack.editor.renderer.lights.RayHandler;
import games.rednblack.editor.renderer.systems.strategy.HyperLap2dInvocationStrategy;
import games.rednblack.editor.renderer.utils.profiling.FrameProfiler;
import games.rednblack.editor.renderer.utils.TextureArrayPolygonSpriteBatch;
import games.rednblack.editor.renderer.utils.profiling.SystemProfiler;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Everything the panel draws, sampled once a frame and kept for the length of the window.
 * <p>
 * The split it reports is the one the editor's render loop already makes: the scene pass is what a game
 * pays for, everything else is the editor. The profiler itself draws in a window of its own and so appears
 * in neither: opening it does not show up as the editor getting slower.
 */
public class PerformanceModel {

    public static final int SERIES_MS = 0;
    public static final int SERIES_DRAW_CALLS = 1;
    public static final int SERIES_GL_CALLS = 2;
    public static final int SERIES_SHADER_SWITCHES = 3;
    public static final int SERIES_TEXTURE_BINDINGS = 4;
    public static final int SERIES_VERTICES = 5;
    public static final int SERIES_COUNT = 6;

    private static final int[] COUNTER_OF_SERIES = {
            -1,
            //draw calls come off the batch, which costs nothing; the rest need the GL profiler
            FrameProfiler.COUNTER_FLUSHES,
            FrameProfiler.COUNTER_GL_CALLS,
            FrameProfiler.COUNTER_SHADER_SWITCHES,
            FrameProfiler.COUNTER_TEXTURE_BINDINGS,
            FrameProfiler.COUNTER_VERTICES
    };

    public static final int WINDOW = FrameProfiler.WINDOW;

    private static final float BYTES_TO_MB = 1f / (1024f * 1024f);

    private final Metric[] series = new Metric[SERIES_COUNT * Scope.values().length];
    private final Metric fps = new Metric(WINDOW);
    private final Metric loop = new Metric(WINDOW);
    private final Metric heapUsed = new Metric(WINDOW);

    private final List<GarbageCollectorMXBean> collectors = ManagementFactory.getGarbageCollectorMXBeans();

    private Engine engine;
    private EntitySubscription allEntities;
    private SceneLoader sceneLoader;
    private Batch uiBatch;
    private SystemProfiler systemProfiler;
    /** On unless the panel is told otherwise: the counters are worth having, and the cost is now visible. */
    private boolean glDetailWanted = true;

    private boolean running;
    private boolean paused;

    /** What the display runs at until a measured frame says otherwise. */
    private int displayRefreshRate = 60;
    /** The rate the frame is judged against, or 0 to follow the display. */
    private int budgetFps;

    private long previousHeapUsed;
    private long allocatedInWindow;
    private long allocationWindowStart;
    private float allocationRate;

    private long previousGcCount;
    private long previousGcTime;
    private long gcWindowStart;
    private float gcPerSecond;
    private float gcMillisPerSecond;

    private int entityCount;
    private int bodyCount;
    private int contactCount;
    private int jointCount;
    private int lightCount;
    private int softLightCount;
    private int sinceSceneSample;

    public PerformanceModel() {
        for (int i = 0; i < series.length; i++) {
            series[i] = new Metric(WINDOW);
        }

        Graphics graphics = Gdx.graphics;
        if (graphics != null && graphics.getDisplayMode() != null) {
            int refreshRate = graphics.getDisplayMode().refreshRate;
            if (refreshRate >= 30 && refreshRate <= 480) displayRefreshRate = refreshRate;
        }
    }

    /** True once a scene has been handed over: before that there is nothing to report on. */
    public boolean isAttached() {
        return engine != null;
    }

    /** The scene changed: pick up its engine, its world, its lights and the batches that draw it. */
    public void attach(Engine engine, SceneLoader sceneLoader, Batch uiBatch) {
        this.engine = engine;
        this.sceneLoader = sceneLoader;
        this.uiBatch = uiBatch;
        this.allEntities = engine == null ? null : engine.getAspectSubscriptionManager().get(Aspect.all());

        installFlushCounter();
        if (running) attachSystemProfiler();
    }

    /**
     * A batch counts its own flushes, and a flush is a draw call. Read at the slot boundaries it gives the
     * same split as the GL profiler would, for none of the price.
     */
    private void installFlushCounter() {
        final Batch sceneBatch = sceneLoader == null ? null : sceneLoader.getBatch();
        final Batch editorBatch = uiBatch;

        if (sceneBatch == null && editorBatch == null) {
            FrameProfiler.setFlushCounter(null);
            return;
        }

        FrameProfiler.setFlushCounter(() -> flushesOf(sceneBatch) + flushesOf(editorBatch));
    }

    private static int flushesOf(Batch batch) {
        //the runtime's batches shadow the field they inherit, so they have to be asked first
        if (batch instanceof TextureArrayPolygonSpriteBatch) return ((TextureArrayPolygonSpriteBatch) batch).totalRenderCalls;
        if (batch instanceof games.rednblack.editor.renderer.utils.PolygonSpriteBatch)
            return ((games.rednblack.editor.renderer.utils.PolygonSpriteBatch) batch).totalRenderCalls;
        if (batch instanceof com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch)
            return ((com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch) batch).totalRenderCalls;
        if (batch instanceof SpriteBatch) return ((SpriteBatch) batch).totalRenderCalls;
        return 0;
    }

    /**
     * Asks for the counters that need the GL profiler. It is only a wish: it is granted from the editor's
     * own frame, because the profiler wraps whichever window is current when it is switched on.
     */
    public void setGLDetail(boolean wanted) {
        glDetailWanted = wanted;
    }

    public boolean isGLDetail() {
        return FrameProfiler.isGLDetail();
    }

    /**
     * Starts or stops measuring. Nothing is measured while the panel is closed: the GL profiler wraps every
     * call and the per-system hook is a branch in the middle of the engine, and neither is worth paying for
     * when nobody is reading.
     */
    public void setRunning(boolean running) {
        if (this.running == running) return;
        this.running = running;

        FrameProfiler.setEnabled(running);

        if (running) {
            reset();
            //put back on every start, not only when a scene loads: closing the window takes it away
            installFlushCounter();
            attachSystemProfiler();
        } else {
            detachSystemProfiler();
            FrameProfiler.setFlushCounter(null);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    public void reset() {
        for (Metric metric : series) metric.clear();
        fps.clear();
        loop.clear();
        heapUsed.clear();
        FrameProfiler.reset();

        previousHeapUsed = 0;
        allocatedInWindow = 0;
        allocationWindowStart = 0;
        allocationRate = 0f;
        previousGcCount = 0;
        previousGcTime = 0;
        gcWindowStart = 0;
        gcPerSecond = 0f;
        gcMillisPerSecond = 0f;
    }

    /** Takes one sample. Called from the panel's act, once per frame, with the previous frame complete. */
    public void update() {
        if (!running) return;

        //granted here, where the editor's window is the current one
        if (glDetailWanted != FrameProfiler.isGLDetail()) FrameProfiler.setGLDetail(glDetailWanted);

        if (paused || FrameProfiler.sampleCount() == 0) return;

        float runtimeMillis = FrameProfiler.slotMillis(FrameProfiler.SLOT_SCENE, 0);
        float editorMillis = sumEditorSlots();

        add(SERIES_MS, runtimeMillis, editorMillis, FrameProfiler.frameMillis(0));

        for (int s = 1; s < SERIES_COUNT; s++) {
            int counter = COUNTER_OF_SERIES[s];
            float runtimeValue = FrameProfiler.counter(FrameProfiler.SLOT_SCENE, counter, 0);
            float editorValue = sumEditorSlots(counter);

            add(s, runtimeValue, editorValue, runtimeValue + editorValue);
        }

        fps.add(FrameProfiler.framesPerSecond());
        loop.add(FrameProfiler.periodMillis(0));

        sampleMemory();
        sampleScene();
    }

    public Metric series(int seriesId, Scope scope) {
        return series[seriesId * Scope.values().length + scope.ordinal()];
    }

    public Metric fps() {
        return fps;
    }

    /**
     * One editor frame to the next, start to start. What this has over the frame time is everything the
     * application does outside the editor's render: other windows drawing and swapping, the compositor,
     * and the sleep that paces the loop.
     */
    public Metric loopMillis() {
        return loop;
    }

    public Metric heapUsedMb() {
        return heapUsed;
    }

    /**
     * The frame budget the numbers are judged against. The display is what this machine can show; a fixed
     * rate is what the game is being written for, which on a fast dev monitor is the kinder and more
     * honest of the two.
     */
    public float budgetMillis() {
        return budgetFps == 0 ? 1000f / displayRefreshRate() : 1000f / budgetFps;
    }

    /** @param fps the rate to measure against, or 0 to follow the display */
    public void setBudgetFps(int fps) {
        budgetFps = Math.max(0, fps);
    }

    public int budgetFps() {
        return budgetFps;
    }

    /** What the measured window's display runs at, once a frame has been seen; its own guess until then. */
    public int displayRefreshRate() {
        int measured = FrameProfiler.refreshRate();
        return measured >= 30 && measured <= 480 ? measured : displayRefreshRate;
    }

    /** Megabytes handed to the heap every second: what decides how often a collection interrupts a frame. */
    public float allocationRate() {
        return allocationRate;
    }

    public float collectionsPerSecond() {
        return gcPerSecond;
    }

    public float collectionMillisPerSecond() {
        return gcMillisPerSecond;
    }

    public long heapCommittedMb() {
        return (long) (Runtime.getRuntime().totalMemory() * BYTES_TO_MB);
    }

    public long heapMaxMb() {
        return (long) (Runtime.getRuntime().maxMemory() * BYTES_TO_MB);
    }

    public int entityCount() {
        return entityCount;
    }

    public int bodyCount() {
        return bodyCount;
    }

    public int contactCount() {
        return contactCount;
    }

    public int jointCount() {
        return jointCount;
    }

    public int lightCount() {
        return lightCount;
    }

    public int softLightCount() {
        return softLightCount;
    }

    public SystemProfiler systemProfiler() {
        return systemProfiler;
    }

    private void add(int seriesId, float runtime, float editor, float both) {
        series(seriesId, Scope.RUNTIME).add(runtime);
        series(seriesId, Scope.EDITOR).add(editor);
        series(seriesId, Scope.BOTH).add(both);
    }

    private float sumEditorSlots() {
        float sum = 0f;
        for (int slot = 1; slot < FrameProfiler.slotCount(); slot++) {
            sum += FrameProfiler.slotMillis(slot, 0);
        }
        return sum;
    }

    private float sumEditorSlots(int counter) {
        float sum = 0f;
        for (int slot = 1; slot < FrameProfiler.slotCount(); slot++) {
            sum += FrameProfiler.counter(slot, counter, 0);
        }
        return sum;
    }

    private void sampleMemory() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        heapUsed.add(used * BYTES_TO_MB);

        //what the heap grew by between two collections is what the frames in between allocated
        if (previousHeapUsed > 0 && used > previousHeapUsed) allocatedInWindow += used - previousHeapUsed;
        previousHeapUsed = used;

        long now = System.currentTimeMillis();
        if (allocationWindowStart == 0) allocationWindowStart = now;
        if (now - allocationWindowStart >= 1000) {
            allocationRate = allocatedInWindow * BYTES_TO_MB * 1000f / (now - allocationWindowStart);
            allocatedInWindow = 0;
            allocationWindowStart = now;
        }

        long collections = 0, collectionMillis = 0;
        for (int i = 0; i < collectors.size(); i++) {
            GarbageCollectorMXBean collector = collectors.get(i);
            long count = collector.getCollectionCount();
            long time = collector.getCollectionTime();
            if (count > 0) collections += count;
            if (time > 0) collectionMillis += time;
        }

        if (gcWindowStart == 0) {
            gcWindowStart = now;
            previousGcCount = collections;
            previousGcTime = collectionMillis;
        } else if (now - gcWindowStart >= 1000) {
            float seconds = (now - gcWindowStart) / 1000f;
            gcPerSecond = (collections - previousGcCount) / seconds;
            gcMillisPerSecond = (collectionMillis - previousGcTime) / seconds;
            previousGcCount = collections;
            previousGcTime = collectionMillis;
            gcWindowStart = now;
        }
    }

    private void sampleScene() {
        //counts move slowly and cost a call each: a few times a second is plenty
        if (sinceSceneSample-- > 0) return;
        sinceSceneSample = 15;

        entityCount = allEntities == null ? 0 : allEntities.getEntities().size();

        if (sceneLoader == null) {
            bodyCount = contactCount = jointCount = lightCount = softLightCount = 0;
            return;
        }

        if (sceneLoader.getWorld() != null) {
            bodyCount = sceneLoader.getWorld().getBodyCount();
            contactCount = sceneLoader.getWorld().getContactCount();
            jointCount = sceneLoader.getWorld().getJointCount();
        }

        RayHandler rayHandler = sceneLoader.getRayHandler();
        if (rayHandler != null) {
            lightCount = rayHandler.getLightCount();
            softLightCount = rayHandler.getSoftLightCount();
        }
    }

    private void attachSystemProfiler() {
        HyperLap2dInvocationStrategy strategy = strategy();
        if (strategy == null) return;

        if (systemProfiler == null) systemProfiler = new SystemProfiler();
        strategy.setProfiler(systemProfiler);
    }

    private void detachSystemProfiler() {
        HyperLap2dInvocationStrategy strategy = strategy();
        if (strategy != null) strategy.setProfiler(null);
    }

    private HyperLap2dInvocationStrategy strategy() {
        if (engine == null) return null;

        SystemInvocationStrategy strategy = engine.getInvocationStrategy();
        return strategy instanceof HyperLap2dInvocationStrategy ? (HyperLap2dInvocationStrategy) strategy : null;
    }
}
