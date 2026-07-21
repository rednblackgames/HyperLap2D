package games.rednblack.editor.view.ui.properties;

import java.util.List;

/**
 * A properties panel that can be driven programmatically (off-stage) by the MCP RemoteOps
 * path, reusing the panel's own widgets and validators so value validation is never bypassed.
 *
 * <p>{@code setFieldValue} maps a string key to the panel's widget setter; the value type
 * depends on the field (Number for numerics, Boolean for flags, String for text, etc.).
 * {@code validateFieldValues} runs the same VisUI validators the user-typing path runs and
 * returns the error messages for any currently-invalid field (empty list = all valid).</p>
 */
public interface RemoteEditablePanel {
    /** Set a field by key. Throws {@link IllegalArgumentException} for unknown keys. */
    void setFieldValue(String key, Object value);

    /** Validate the currently-set field values; return error messages (empty list = all valid). */
    List<String> validateFieldValues();

    /**
     * Suppress (or restore) change-event firing on the panel's select boxes during an off-stage
     * remote edit, so the live UI's mediators are not triggered mid-drive. No-op for panels with
     * no select boxes. Default no-op.
     */
    default void setProgrammaticSelectBoxes(boolean programmatic) {}

    /**
     * Recompute field disabled-state from the current select-box values (e.g. directional fields
     * enabled only for DIRECTIONAL lightType), since change listeners were suppressed. Default no-op.
     */
    default void refreshDisabledState() {}
}