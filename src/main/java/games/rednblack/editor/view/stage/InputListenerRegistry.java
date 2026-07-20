package games.rednblack.editor.view.stage;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.view.stage.input.InputListener;

/**
 * Owns the editor's global input-listener collection, extracted from
 * {@link Sandbox} (Phase 2 decomposition). {@code Sandbox} holds an instance
 * and delegates to it, preserving the existing {@code addListener/removeListener/...}
 * API while shrinking the god-class; later phases can inject this directly
 * instead of reaching it through the Sandbox singleton.
 */
public class InputListenerRegistry {

    private final Array<InputListener> listeners = new Array<>(1);

    public void add(InputListener listener) {
        if (!listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }

    public void remove(InputListener listener) {
        listeners.removeValue(listener, true);
    }

    public void removeAll() {
        listeners.clear();
    }

    public Array<InputListener> getAll() {
        listeners.shrink();
        return listeners;
    }
}