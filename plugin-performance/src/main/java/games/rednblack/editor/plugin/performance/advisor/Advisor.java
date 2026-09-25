package games.rednblack.editor.plugin.performance.advisor;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.plugin.performance.data.Fmt;
import games.rednblack.editor.plugin.performance.data.Metric;
import games.rednblack.editor.plugin.performance.data.PerformanceModel;
import games.rednblack.editor.plugin.performance.data.Scope;
import games.rednblack.editor.renderer.utils.profiling.FrameProfiler;
import games.rednblack.editor.renderer.utils.profiling.SystemProfiler;

/**
 * Reads the window and says what is worth knowing about it.
 * <p>
 * A number on its own rarely tells anyone what to do: four hundred draw calls is only a problem once you know
 * it means the batch is being broken, and eighteen megabytes a second only matters as the collection it will
 * cause. Each rule here turns one measurement into the sentence behind it.
 */
public class Advisor {

    /** Enough of the window filled that an average means something. */
    private static final int MINIMUM_SAMPLES = 30;
    private static final int MAX_HINTS = 5;

    private final Array<Hint> pool = new Array<>();
    private int count;
    private int revision;
    private int signature;

    public void evaluate(PerformanceModel model) {
        count = 0;

        Metric runtime = model.series(PerformanceModel.SERIES_MS, Scope.RUNTIME);
        if (runtime.count() < MINIMUM_SAMPLES) {
            updateSignature();
            return;
        }

        float budget = model.budgetMillis();
        Metric editor = model.series(PerformanceModel.SERIES_MS, Scope.EDITOR);
        float sceneMillis = runtime.average();

        if (sceneMillis > budget) {
            Hint hint = next(Hint.BAD);
            hint.text.append("The scene alone takes ");
            Fmt.number(hint.text, sceneMillis, 1);
            hint.text.append(" ms of a ");
            Fmt.number(hint.text, budget, 1);
            hint.text.append(" ms frame: it cannot hold the target rate on this machine.");
        } else if (sceneMillis > budget * 0.6f) {
            Hint hint = next(Hint.WARN);
            hint.text.append("The scene is using ");
            Fmt.number(hint.text, sceneMillis / budget * 100f, 0);
            hint.text.append("% of the frame budget, leaving little for game logic.");
        }

        if (runtime.worstPercent() > budget && runtime.worstPercent() > sceneMillis * 2.5f) {
            Hint hint = next(Hint.WARN);
            hint.text.append("The worst 1% of frames cost ");
            Fmt.number(hint.text, runtime.worstPercent(), 1);
            hint.text.append(" ms against an average of ");
            Fmt.number(hint.text, sceneMillis, 1);
            hint.text.append(" ms: something spikes rather than runs slow.");
        }

        //what is left of the frame once the render and the frame rate limiter's sleep are taken out. The
        //sleep is the loop waiting on purpose, and counting it as overhead calls a healthy frame slow
        float frame = model.series(PerformanceModel.SERIES_MS, Scope.BOTH).average();
        float busyOutside = model.loopMillis().average()
                - FrameProfiler.loopPhaseMillis(FrameProfiler.LOOP_WAIT)
                - frame;
        if (busyOutside > 2f && busyOutside > frame * 0.5f) {
            Hint hint = next(Hint.WARN);
            Fmt.number(hint.text, busyOutside, 1);
            hint.text.append(" ms of every frame goes somewhere other than the editor's render, which is ");
            Fmt.number(hint.text, frame, 1);
            hint.text.append(" ms. That is this window drawing, the buffer swaps, or the desktop compositor "
                    + "pacing them - the loop line says which.");
        }

        float drawCalls = model.series(PerformanceModel.SERIES_DRAW_CALLS, Scope.RUNTIME).average();
        if (drawCalls > 50f) {
            Hint hint = next(drawCalls > 150f ? Hint.BAD : Hint.WARN);
            Fmt.number(hint.text, drawCalls, 0);
            hint.text.append(" draw calls for the scene. Images that come from different atlases, or that sit between "
                    + "items using another texture, break the batch and start a new call.");
        }

        //these two are only counted while the GL profiler is on, and it is off unless asked for
        float shaderSwitches = model.isGLDetail() ? model.series(PerformanceModel.SERIES_SHADER_SWITCHES, Scope.RUNTIME).average() : 0f;
        if (shaderSwitches > 15f) {
            Hint hint = next(Hint.WARN);
            Fmt.number(hint.text, shaderSwitches, 0);
            hint.text.append(" shader switches a frame. Each one flushes whatever the batch was holding.");
        }

        float bindings = model.isGLDetail() ? model.series(PerformanceModel.SERIES_TEXTURE_BINDINGS, Scope.RUNTIME).average() : 0f;
        if (bindings > 30f) {
            Hint hint = next(Hint.INFO);
            Fmt.number(hint.text, bindings, 0);
            hint.text.append(" texture bindings a frame: fewer, larger atlases would cut them down.");
        }

        float allocation = model.allocationRate();
        if (allocation > 4f) {
            Hint hint = next(allocation > 16f ? Hint.BAD : Hint.WARN);
            hint.text.append("Allocating ");
            Fmt.number(hint.text, allocation, 1);
            hint.text.append(" MB/s. Per-frame garbage is what turns a smooth scene into a stuttering one once "
                    + "the collector catches up.");
        }

        if (model.collectionMillisPerSecond() > 3f) {
            Hint hint = next(Hint.WARN);
            hint.text.append("Garbage collection took ");
            Fmt.number(hint.text, model.collectionMillisPerSecond(), 1);
            hint.text.append(" ms in the last second.");
        }

        if (model.lightCount() > 6) {
            Hint hint = next(Hint.INFO);
            hint.text.append(model.lightCount());
            hint.text.append(" lights are on");
            if (model.softLightCount() > 0) {
                hint.text.append(", ");
                hint.text.append(model.softLightCount());
                hint.text.append(" of them soft");
            }
            hint.text.append(". Lights render into their own buffer before the scene is composed.");
        }

        if (model.contactCount() > 200) {
            Hint hint = next(Hint.INFO);
            hint.text.append(model.contactCount());
            hint.text.append(" physics contacts are being solved each step.");
        }

        if (model.entityCount() > 1200) {
            Hint hint = next(Hint.INFO);
            hint.text.append(model.entityCount());
            hint.text.append(" entities in the scene: every system walks its own share of them each frame.");
        }

        hottestSystem(model, sceneMillis);

        if (editor.average() > sceneMillis * 2f && sceneMillis < budget * 0.5f) {
            Hint hint = next(Hint.INFO);
            hint.text.append("Most of this frame is the editor itself. The scene, which is what ships, costs ");
            Fmt.number(hint.text, sceneMillis, 2);
            hint.text.append(" ms.");
        }

        if (count == 0) {
            Hint hint = next(Hint.INFO);
            hint.text.append("Nothing stands out. The scene fits the budget with room left over.");
        }

        sortBySeverity();
        updateSignature();
    }

    public int size() {
        return Math.min(count, MAX_HINTS);
    }

    public Hint get(int index) {
        return pool.get(index);
    }

    /** Changes whenever the advice does, so the strip is rebuilt only when there is something new to read. */
    public int revision() {
        return revision;
    }

    private void hottestSystem(PerformanceModel model, float sceneMillis) {
        SystemProfiler systems = model.systemProfiler();
        if (systems == null || systems.systemCount() == 0 || sceneMillis <= 0.01f) return;

        int hottest = -1;
        float worst = 0f;
        for (int i = 0; i < systems.systemCount(); i++) {
            if (systems.averageMillis(i) > worst) {
                worst = systems.averageMillis(i);
                hottest = i;
            }
        }

        float total = systems.totalAverageMillis();
        if (hottest < 0 || total <= 0.01f || worst < total * 0.35f) return;

        Hint hint = next(Hint.INFO);
        hint.text.append(systems.name(hottest));
        hint.text.append(" is ");
        Fmt.number(hint.text, worst / total * 100f, 0);
        hint.text.append("% of the scene's time, at ");
        Fmt.number(hint.text, worst, 2);
        hint.text.append(" ms.");
    }

    private Hint next(int severity) {
        while (pool.size <= count) pool.add(new Hint());
        return pool.get(count++).set(severity);
    }

    private void sortBySeverity() {
        for (int i = 0; i < count - 1; i++) {
            int best = i;
            for (int j = i + 1; j < count; j++) {
                if (pool.get(j).severity > pool.get(best).severity) best = j;
            }
            if (best != i) pool.swap(i, best);
        }
    }

    private void updateSignature() {
        int hash = count;
        for (int i = 0; i < size(); i++) {
            Hint hint = pool.get(i);
            hash = hash * 31 + hint.severity;
            for (int c = 0; c < hint.text.length(); c++) {
                hash = hash * 31 + hint.text.charAt(c);
            }
        }

        if (hash != signature) {
            signature = hash;
            revision++;
        }
    }
}
