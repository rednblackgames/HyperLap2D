package games.rednblack.editor.view.stage;

import java.util.HashMap;

/**
 * Owns the editor's local (in-memory) clipboard, extracted from {@link Sandbox}
 * (Phase 2 decomposition). {@code Sandbox} holds an instance and delegates
 * {@code copyToLocalClipboard/retrieveFromLocalClipboard} to it.
 */
public class ClipboardService {

    private final HashMap<String, Object> localClipboard = new HashMap<>();

    public void copyToLocalClipboard(String key, Object data) {
        localClipboard.put(key, data);
    }

    public Object retrieveFromLocalClipboard(String key) {
        return localClipboard.get(key);
    }
}