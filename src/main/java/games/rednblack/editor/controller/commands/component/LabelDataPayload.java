package games.rednblack.editor.controller.commands.component;

/**
 * Typed payload for {@link UpdateLabelDataCommand}, replacing the positional
 * {@code Object[9]} (Phase 1 typed payloads).
 */
public record LabelDataPayload(int entity, String fontName, int fontSize, int align,
                               String text, String prevText, boolean wrap, boolean mono,
                               String bitmapFont) {
}