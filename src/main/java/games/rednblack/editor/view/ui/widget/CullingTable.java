package games.rednblack.editor.view.ui.widget;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Cullable;

/**
 * A scroll pane's content table that hands the culling on to the table inside it.
 * <p>
 * A scroll pane only offers a culling area to a widget that says it is {@link Cullable}, and a group only
 * culls its own direct children. The panels here put one table inside another - the outer one so a
 * placeholder can take the items' place - and the outer one has a single child covering everything, so
 * culling it alone would skip nothing at all. This passes the area down to the table that actually holds
 * the items, shifted into its coordinates, and that is where the rows get skipped. That table may sit
 * several groups down (a VisUI {@code ListView} keeps its items in a table inside a table of its own);
 * the shift is then the sum of the positions along the way.
 * <p>
 * The shift is worked out at draw time rather than when the area arrives: by then the layout is valid, so
 * the inner table's position is the one being drawn with.
 * <p>
 * Only drawing is skipped. Layout, input, drag and drop and anything advanced in {@code act} carry on for
 * items that are out of sight, which is why an animated preview is still in step when it scrolls back in.
 */
public class CullingTable extends Table implements Cullable {

    private final Rectangle targetArea = new Rectangle();
    private Group target;

    /** The table holding the items, which is the one whose children are worth skipping. Any descendant will do. */
    public void setCullingTarget(Group target) {
        this.target = target;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        applyCulling();
        super.draw(batch, parentAlpha);
    }

    /** Separate from the drawing so the one piece of arithmetic here can be tested on its own. */
    void applyCulling() {
        Rectangle area = getCullingArea();
        if (area == null || target == null) return;

        float offsetX = 0, offsetY = 0;
        Group group = target;
        while (group != this) {
            //not while the placeholder is up: the items table is not in the tree then
            if (group == null) return;
            offsetX += group.getX();
            offsetY += group.getY();
            group = group.getParent();
        }

        targetArea.set(area.x - offsetX, area.y - offsetY, area.width, area.height);
        target.setCullingArea(targetArea);
    }
}
