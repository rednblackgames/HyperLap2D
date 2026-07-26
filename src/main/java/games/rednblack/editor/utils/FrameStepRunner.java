package games.rednblack.editor.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Runs a sequence of named steps on the render thread, one per frame, so the editor keeps drawing
 * between them and a loading dialog can report what is going on. Each step gets announced one frame
 * before it runs, otherwise its label would only appear once the work it describes is already done.
 */
public class FrameStepRunner {

    public interface Listener {
        /** Called a frame before {@code name} runs; {@code progress} is {@code 0..1}. */
        void onStepStarted(String name, float progress);
    }

    private final Array<Step> steps = new Array<>();

    private Listener listener;
    private Runnable onComplete;
    private int index;

    public FrameStepRunner add(String name, Runnable action) {
        steps.add(new Step(name, action));
        return this;
    }

    public void run(Listener listener, Runnable onComplete) {
        this.listener = listener;
        this.onComplete = onComplete;
        this.index = 0;
        tick();
    }

    private void tick() {
        if (index == steps.size) {
            onComplete.run();
            return;
        }

        Step step = steps.get(index);
        if (listener != null) listener.onStepStarted(step.name, (float) index / steps.size);

        Gdx.app.postRunnable(() -> {
            try {
                step.action.run();
            } catch (Throwable t) {
                // A step that blows up would otherwise take the render thread down with it and leave
                // the sequence — and whoever is waiting on its completion — hanging.
                t.printStackTrace();
            }
            index++;
            tick();
        });
    }

    private static class Step {
        final String name;
        final Runnable action;

        Step(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }
    }
}
