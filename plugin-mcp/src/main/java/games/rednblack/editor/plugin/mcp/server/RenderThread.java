package games.rednblack.editor.plugin.mcp.server;

import com.badlogic.gdx.Gdx;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs a supplier on the libGDX render thread and synchronously returns its result.
 * MCP HTTP handler threads use this so reads of the live ECS/engine happen on the
 * render thread (consistent snapshot, no concurrent modification of gdx arrays).
 */
public final class RenderThread {
    private RenderThread() {}

    public static <T> T run(Callable<T> supplier, long timeoutMs) {
        final Object[] box = new Object[1];
        final Throwable[] err = new Throwable[1];
        CountDownLatch latch = new CountDownLatch(1);
        Gdx.app.postRunnable(() -> {
            try {
                box[0] = supplier.call();
            } catch (Throwable t) {
                err[0] = t;
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException(new TimeoutException("render-thread op timed out"));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted waiting for render thread", e);
        }
        if (err[0] != null) {
            throw new RuntimeException("render-thread op failed: " + err[0].getMessage(), err[0]);
        }
        @SuppressWarnings("unchecked")
        T result = (T) box[0];
        return result;
    }
}