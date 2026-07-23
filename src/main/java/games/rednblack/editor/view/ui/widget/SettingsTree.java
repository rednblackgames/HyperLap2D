package games.rednblack.editor.view.ui.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.kotcrab.vis.ui.widget.VisTree;
import games.rednblack.editor.view.ui.dialog.SettingsDialog;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Settings tree with roomier spacing and a chevron expand indicator drawn at runtime with
 * {@link ShapeDrawer} instead of the {@code tree-plus}/{@code tree-minus} atlas regions.
 * <p>
 * The style drawables are still required: {@link Tree} queries their {@code getMinWidth()} while
 * laying out rows, so they stay in the skin as invisible spacers and only the <em>drawing</em> is
 * replaced here — {@link #drawExpandIcon} paints a chevron (right when collapsed, down when
 * expanded) sized and centred inside the space the drawable reserved.
 */
public class SettingsTree extends VisTree<SettingsDialog.SettingsNode, SettingsNodeValue<?>> {

    private static final float PADDING_LEFT = 2f;
    private static final float PADDING_RIGHT = 10f;
    private static final float Y_SPACING = 12f;
    private static final float INDENT_SPACING = 12f;
    private static final float ICON_SPACING_LEFT = 2f;
    private static final float ICON_SPACING_RIGHT = 8f;

    /** Chevron arm length, measured from its vertex. */
    private static final float CHEVRON_ARM = 3.5f;
    private static final float CHEVRON_THICKNESS = 1.6f;
    private static final Color CHEVRON_COLOR = new Color(0.72f, 0.73f, 0.74f, 1f);

    private ShapeDrawer shapeDrawer;

    public SettingsTree() {
        setPadding(PADDING_LEFT, PADDING_RIGHT);
        setYSpacing(Y_SPACING);
        setIndentSpacing(INDENT_SPACING);
        setIconSpacing(ICON_SPACING_LEFT, ICON_SPACING_RIGHT);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeDrawer == null)
            shapeDrawer = new ShapeDrawer(batch, WhitePixel.sharedInstance.textureRegion);
        super.draw(batch, parentAlpha);
    }

    @Override
    protected void drawExpandIcon(SettingsDialog.SettingsNode node, Drawable expandIcon, Batch batch, float x, float y) {
        // deliberately not calling super: the atlas plus/minus is replaced by the drawn chevron.
        // x/y is the icon's bottom-left corner, sized to the drawable Tree reserved space for.
        float cx = x + expandIcon.getMinWidth() / 2f;
        float cy = y + expandIcon.getMinHeight() / 2f;

        float prevColor = shapeDrawer.setColor(CHEVRON_COLOR);
        float prevThickness = shapeDrawer.getDefaultLineWidth();
        shapeDrawer.setDefaultLineWidth(CHEVRON_THICKNESS);

        float a = CHEVRON_ARM;
        if (node.isExpanded()) {
            // v : vertex below centre, arms rising outwards
            shapeDrawer.line(cx - a, cy + a / 2f, cx, cy - a / 2f);
            shapeDrawer.line(cx, cy - a / 2f, cx + a, cy + a / 2f);
        } else {
            // > : vertex right of centre, arms opening to the left
            shapeDrawer.line(cx - a / 2f, cy + a, cx + a / 2f, cy);
            shapeDrawer.line(cx + a / 2f, cy, cx - a / 2f, cy - a);
        }

        shapeDrawer.setDefaultLineWidth(prevThickness);
        shapeDrawer.setColor(prevColor);
    }
}
