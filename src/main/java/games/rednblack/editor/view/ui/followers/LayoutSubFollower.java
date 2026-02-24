package games.rednblack.editor.view.ui.followers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class LayoutSubFollower extends SubFollower {

    private static final Color HORIZONTAL_COLOR = new Color(0.3f, 0.5f, 1f, 0.8f);
    private static final Color VERTICAL_COLOR = new Color(0.3f, 0.85f, 0.4f, 0.8f);
    private static final Color ANCHOR_COLOR = new Color(1f, 1f, 1f, 0.9f);
    private static final Color PARENT_BOUNDS_COLOR = new Color(1f, 1f, 1f, 0.25f);
    private static final Color LABEL_BG_COLOR = new Color(0f, 0f, 0f, 0.6f);
    private static final float LABEL_FONT_SCALE = 0.7f;
    private static final float LABEL_PADDING = 2f;

    private ShapeDrawer shapeDrawer;
    private final int pixelsPerWU;
    private BitmapFont font;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public LayoutSubFollower(int entity) {
        super(entity);
        pixelsPerWU = Sandbox.getInstance().getPixelPerWU();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null) {
            shapeDrawer = new ShapeDrawer(stage.getBatch(), WhitePixel.sharedInstance.textureRegion);
            font = VisUI.getSkin().getFont("default-font");
        }
    }

    @Override
    public void create() {
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeDrawer == null) return;

        LayoutComponent layout = SandboxComponentRetriever.get(entity, LayoutComponent.class);
        if (layout == null) return;

        TransformComponent transform = SandboxComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensions = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        ParentNodeComponent parentNode = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
        if (transform == null || dimensions == null || parentNode == null) return;

        int parent = parentNode.parentEntity;
        if (parent == -1) return;
        DimensionsComponent parentDim = SandboxComponentRetriever.get(parent, DimensionsComponent.class);
        if (parentDim == null) return;

        OrthographicCamera camera = Sandbox.getInstance().getCamera();

        // Scale factor to convert world units to screen pixels in the BasicFollower's local space.
        // BasicFollower positions its children using this same factor (see BasicFollower.update()).
        float scale = pixelsPerWU / camera.zoom;

        // The entity's scale (flip-aware), matching BasicFollower.update()
        float scaleX = transform.scaleX * (transform.flipX ? -1 : 1);
        float scaleY = transform.scaleY * (transform.flipY ? -1 : 1);

        // Line width and anchor radius in constant screen pixels
        float lineWidth = 1.5f;
        float anchorRadius = 3f;

        shapeDrawer.update();

        drawParentBounds(transform, parentDim, scale, scaleX, scaleY);

        drawConstraint(batch, layout.left, transform, dimensions, parentDim, true, true, scale, scaleX, scaleY, lineWidth, anchorRadius);
        drawConstraint(batch, layout.right, transform, dimensions, parentDim, true, false, scale, scaleX, scaleY, lineWidth, anchorRadius);
        drawConstraint(batch, layout.bottom, transform, dimensions, parentDim, false, true, scale, scaleX, scaleY, lineWidth, anchorRadius);
        drawConstraint(batch, layout.top, transform, dimensions, parentDim, false, false, scale, scaleX, scaleY, lineWidth, anchorRadius);
    }

    private void drawParentBounds(TransformComponent transform, DimensionsComponent parentDim,
                                   float scale, float scaleX, float scaleY) {
        // Parent bounds corners in parent-local world coords, converted to BasicFollower-local screen coords
        float x0 = (0 - transform.x) * scale * scaleX;
        float y0 = (0 - transform.y) * scale * scaleY;
        float x1 = (parentDim.width - transform.x) * scale * scaleX;
        float y1 = (parentDim.height - transform.y) * scale * scaleY;

        shapeDrawer.setColor(PARENT_BOUNDS_COLOR);
        float bw = 1f;
        drawDashedLine(x0, y0, x1, y0, bw, 4f);
        drawDashedLine(x1, y0, x1, y1, bw, 4f);
        drawDashedLine(x1, y1, x0, y1, bw, 4f);
        drawDashedLine(x0, y1, x0, y0, bw, 4f);
    }

    /**
     * Draws a single constraint line from the entity's edge midpoint to the target anchor.
     *
     * For parent constraints, the line is axis-aligned (straight horizontal/vertical).
     * For sibling constraints, the line goes obliquely from the entity's edge midpoint
     * to the midpoint of the target side on the sibling entity.
     */
    private void drawConstraint(Batch batch, LayoutComponent.ConstraintData data,
                                 TransformComponent transform, DimensionsComponent dimensions,
                                 DimensionsComponent parentDim,
                                 boolean horizontal, boolean startSide,
                                 float scale, float scaleX, float scaleY,
                                 float lineWidth, float anchorRadius) {
        if (data == null) return;

        Color color = horizontal ? HORIZONTAL_COLOR : VERTICAL_COLOR;

        // Entity edge midpoint in parent-local world coordinates
        float entityEdgeX, entityEdgeY;
        if (horizontal) {
            entityEdgeX = startSide ? transform.x : (transform.x + dimensions.width);
            entityEdgeY = transform.y + dimensions.height / 2f;
        } else {
            entityEdgeX = transform.x + dimensions.width / 2f;
            entityEdgeY = startSide ? transform.y : (transform.y + dimensions.height);
        }

        // Target anchor point in parent-local world coordinates
        float targetX, targetY;
        if (data.targetEntity == -1) {
            // Parent: straight horizontal/vertical line to parent edge
            targetX = resolveParentSideX(data.targetSide, parentDim);
            targetY = resolveParentSideY(data.targetSide, parentDim);
            // Align the non-constrained axis for a clean straight line
            if (horizontal) {
                targetY = entityEdgeY;
            } else {
                targetX = entityEdgeX;
            }
        } else {
            // Sibling: oblique line to the midpoint of the target side
            TransformComponent sibTransform = SandboxComponentRetriever.get(data.targetEntity, TransformComponent.class);
            DimensionsComponent sibDim = SandboxComponentRetriever.get(data.targetEntity, DimensionsComponent.class);
            if (sibTransform == null || sibDim == null) return;

            targetX = resolveSiblingSideX(data.targetSide, sibTransform, sibDim);
            targetY = resolveSiblingSideY(data.targetSide, sibTransform, sibDim);
        }

        // Convert from parent-local world coords to BasicFollower-local screen coords.
        // BasicFollower origin = entity's (transform.x, transform.y) in screen space.
        float ex = (entityEdgeX - transform.x) * scale * scaleX;
        float ey = (entityEdgeY - transform.y) * scale * scaleY;
        float tx = (targetX - transform.x) * scale * scaleX;
        float ty = (targetY - transform.y) * scale * scaleY;

        shapeDrawer.setColor(color);
        drawDashedLine(ex, ey, tx, ty, lineWidth, 6f);

        shapeDrawer.setColor(ANCHOR_COLOR);
        shapeDrawer.filledCircle(ex, ey, anchorRadius);
        shapeDrawer.filledCircle(tx, ty, anchorRadius);

        if (data.margin != 0 && font != null) {
            drawMarginLabel(batch, data.margin, (ex + tx) / 2f, (ey + ty) / 2f, color);
        }
    }

    private void drawMarginLabel(Batch batch, float margin, float cx, float cy, Color color) {
        String text = margin == (int) margin ? String.valueOf((int) margin) : String.format("%.1f", margin);

        float oldScaleX = font.getData().scaleX;
        float oldScaleY = font.getData().scaleY;
        font.getData().setScale(LABEL_FONT_SCALE);

        glyphLayout.setText(font, text);
        float textW = glyphLayout.width;
        float textH = glyphLayout.height;

        float bgX = cx - textW / 2f - LABEL_PADDING;
        float bgY = cy - textH / 2f - LABEL_PADDING;
        float bgW = textW + LABEL_PADDING * 2f;
        float bgH = textH + LABEL_PADDING * 2f;

        shapeDrawer.setColor(LABEL_BG_COLOR);
        shapeDrawer.filledRectangle(bgX, bgY, bgW, bgH);

        font.setColor(color);
        font.draw(batch, text, cx - textW / 2f, cy + textH / 2f);

        font.getData().setScale(oldScaleX, oldScaleY);
    }

    private float resolveParentSideX(LayoutComponent.ConstraintSide side, DimensionsComponent parentDim) {
        if (side == null) return 0;
        switch (side) {
            case LEFT: return 0;
            case RIGHT: return parentDim.width;
            case BOTTOM:
            case TOP: return parentDim.width / 2f;
            default: return 0;
        }
    }

    private float resolveParentSideY(LayoutComponent.ConstraintSide side, DimensionsComponent parentDim) {
        if (side == null) return 0;
        switch (side) {
            case BOTTOM: return 0;
            case TOP: return parentDim.height;
            case LEFT:
            case RIGHT: return parentDim.height / 2f;
            default: return 0;
        }
    }

    private float resolveSiblingSideX(LayoutComponent.ConstraintSide side,
                                       TransformComponent sibTransform,
                                       DimensionsComponent sibDim) {
        if (side == null) return 0;
        switch (side) {
            case LEFT: return sibTransform.x;
            case RIGHT: return sibTransform.x + sibDim.width;
            case BOTTOM:
            case TOP: return sibTransform.x + sibDim.width / 2f;
            default: return 0;
        }
    }

    private float resolveSiblingSideY(LayoutComponent.ConstraintSide side,
                                       TransformComponent sibTransform,
                                       DimensionsComponent sibDim) {
        if (side == null) return 0;
        switch (side) {
            case BOTTOM: return sibTransform.y;
            case TOP: return sibTransform.y + sibDim.height;
            case LEFT:
            case RIGHT: return sibTransform.y + sibDim.height / 2f;
            default: return 0;
        }
    }

    private void drawDashedLine(float x1, float y1, float x2, float y2, float lineWidth, float dashLength) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 0.001f) return;

        float nx = dx / dist;
        float ny = dy / dist;

        float drawn = 0;
        boolean drawing = true;
        while (drawn < dist) {
            float segLen = Math.min(dashLength, dist - drawn);
            if (drawing) {
                shapeDrawer.line(
                        x1 + nx * drawn, y1 + ny * drawn,
                        x1 + nx * (drawn + segLen), y1 + ny * (drawn + segLen),
                        lineWidth);
            }
            drawn += segLen;
            drawing = !drawing;
        }
    }
}
