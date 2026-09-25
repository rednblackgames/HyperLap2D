package games.rednblack.editor.plugin.performance.widget;

import com.kotcrab.vis.ui.widget.VisLabel;

/**
 * Labels that do not decide how wide the panel is.
 * <p>
 * A label's preferred width is the width of its text, and a table hands every column at least that much.
 * In a panel whose text changes with the numbers - an allocation rate going from "0.0 MB/s" to
 * "725.3 MB/s" - that means the window grows to fit its own readings and the layout drifts out from under
 * whoever is reading it. These ask for nothing, take what the cell gives them, and cut what will not fit.
 */
public final class Labels {

    private Labels() {
    }

    /** One line, truncated with an ellipsis when the cell is too narrow for it. */
    public static VisLabel fitted(String style) {
        VisLabel label = new VisLabel("", style) {
            @Override
            public float getPrefWidth() {
                return 0f;
            }
        };
        label.setEllipsis(true);
        return label;
    }

    /** Wrapped over as many lines as it takes, within whatever width the cell has. */
    public static VisLabel wrapped(CharSequence text, String style) {
        VisLabel label = new VisLabel(text, style) {
            @Override
            public float getPrefWidth() {
                return 0f;
            }
        };
        label.setWrap(true);
        return label;
    }
}
