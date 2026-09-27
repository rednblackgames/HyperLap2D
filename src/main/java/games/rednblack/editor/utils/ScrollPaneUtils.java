package games.rednblack.editor.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;

public class ScrollPaneUtils {

    private static final Vector2 tmp = new Vector2();

    /**
     * Scrolls a pane just enough to show the whole of an item: up to its top edge when it is above the
     * view, down to its bottom edge when it is below, not at all when it is already in sight.
     * <p>
     * {@link ScrollPane#scrollTo(float, float, float, float)} is not used because it is one item height off
     * for the bottom left corner it asks for: moving up it stops with the item just above the view, so the
     * first row of a list could never be reached. The item may sit any number of groups inside the pane's
     * actor; its position is worked out from there.
     */
    public static void scrollIntoView(ScrollPane pane, Actor item) {
        Actor content = pane.getActor();
        if (content == null || !item.isDescendantOf(content)) return;
        pane.validate();

        item.localToAscendantCoordinates(content, tmp.set(0, 0));
        //scroll counts from the top, local coordinates from the bottom
        float top = content.getHeight() - tmp.y - item.getHeight();
        float bottom = top + item.getHeight();

        float scroll = pane.getScrollY();
        if (top < scroll) scroll = top;
        else if (bottom > scroll + pane.getScrollHeight()) scroll = bottom - pane.getScrollHeight();
        pane.setScrollY(scroll);
    }
}
