package games.rednblack.editor.view.ui.followers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
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
    private static final Color SIBLING_BOUNDS_COLOR = new Color(0.8f, 0.7f, 0.3f, 0.25f);
    private static final Color LABEL_BG_COLOR = new Color(0f, 0f, 0f, 0.6f);
    private static final float LABEL_FONT_SCALE = 0.7f;
    private static final float LABEL_PADDING = 2f;

    private ShapeDrawer shapeDrawer;
    private final int pixelsPerWU;
    private BitmapFont font;
    private final GlyphLayout glyphLayout = new GlyphLayout();

    // Per-frame drawing state (set in draw(), used by helpers)
    private float drawScale;
    private float entityCosR, entitySinR;
    private float entityScaleX, entityScaleY;
    private float entityOriginX, entityOriginY;
    private float entityTx, entityTy;
    // Entity AABB offsets from (transform.x, transform.y)
    private float entityAABBLeft, entityAABBBottom, entityAABBRight, entityAABBTop;

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

        // Precompute per-frame drawing state
        drawScale = pixelsPerWU / camera.zoom;
        entityTx = transform.x;
        entityTy = transform.y;
        entityScaleX = transform.scaleX * (transform.flipX ? -1 : 1);
        entityScaleY = transform.scaleY * (transform.flipY ? -1 : 1);
        entityOriginX = Float.isNaN(transform.originX) ? 0 : transform.originX;
        entityOriginY = Float.isNaN(transform.originY) ? 0 : transform.originY;
        entityCosR = MathUtils.cosDeg(transform.rotation);
        entitySinR = MathUtils.sinDeg(transform.rotation);

        // Read precomputed AABB from BoundingBoxComponent
        BoundingBoxComponent bb = SandboxComponentRetriever.get(entity, BoundingBoxComponent.class);
        if (bb != null) {
            entityAABBLeft = bb.parentLocalAABB.x;
            entityAABBBottom = bb.parentLocalAABB.y;
            entityAABBRight = entityAABBLeft + bb.parentLocalAABB.width;
            entityAABBTop = entityAABBBottom + bb.parentLocalAABB.height;
        } else {
            entityAABBLeft = 0; entityAABBBottom = 0;
            entityAABBRight = dimensions.width; entityAABBTop = dimensions.height;
        }

        float lineWidth = 1.5f;
        float anchorRadius = 3f;

        shapeDrawer.update();

        drawParentBounds(parentDim);
        drawSiblingBounds(layout);

        drawConstraint(batch, layout.left, transform, parentDim, true, true, lineWidth, anchorRadius);
        drawConstraint(batch, layout.right, transform, parentDim, true, false, lineWidth, anchorRadius);
        drawConstraint(batch, layout.bottom, transform, parentDim, false, true, lineWidth, anchorRadius);
        drawConstraint(batch, layout.top, transform, parentDim, false, false, lineWidth, anchorRadius);
    }

    // ----------------------------------------------------------------
    // Coordinate conversion: parent-world -> SubFollower drawing coords
    // ----------------------------------------------------------------

    /**
     * Converts a parent-world point to SubFollower X drawing coordinate.
     * Applies the inverse of the entity's transform (parentToLocal) then
     * scales to screen pixels, so parent-space geometry appears correctly
     * on screen despite BasicFollower's rotation and scale.
     */
    private float toFollowerX(float wx, float wy) {
        float tox = wx - entityTx - entityOriginX;
        float toy = wy - entityTy - entityOriginY;
        return (tox * entityCosR + toy * entitySinR) * drawScale + entityOriginX * drawScale * entityScaleX;
    }

    private float toFollowerY(float wx, float wy) {
        float tox = wx - entityTx - entityOriginX;
        float toy = wy - entityTy - entityOriginY;
        return (tox * -entitySinR + toy * entityCosR) * drawScale + entityOriginY * drawScale * entityScaleY;
    }

    // ----------------------------------------------------------------
    // Bounds drawing
    // ----------------------------------------------------------------

    private void drawParentBounds(DimensionsComponent parentDim) {
        float x0 = toFollowerX(0, 0), y0 = toFollowerY(0, 0);
        float x1 = toFollowerX(parentDim.width, 0), y1 = toFollowerY(parentDim.width, 0);
        float x2 = toFollowerX(parentDim.width, parentDim.height), y2 = toFollowerY(parentDim.width, parentDim.height);
        float x3 = toFollowerX(0, parentDim.height), y3 = toFollowerY(0, parentDim.height);

        shapeDrawer.setColor(PARENT_BOUNDS_COLOR);
        float bw = 1f;
        drawDashedLine(x0, y0, x1, y1, bw, 4f);
        drawDashedLine(x1, y1, x2, y2, bw, 4f);
        drawDashedLine(x2, y2, x3, y3, bw, 4f);
        drawDashedLine(x3, y3, x0, y0, bw, 4f);
    }

    private void drawSiblingBounds(LayoutComponent layout) {
        LayoutComponent.ConstraintData[] constraints = {layout.left, layout.right, layout.bottom, layout.top};
        int[] drawn = new int[4];
        int drawnCount = 0;

        for (LayoutComponent.ConstraintData data : constraints) {
            if (data == null || data.targetEntity == -1) continue;

            boolean alreadyDrawn = false;
            for (int i = 0; i < drawnCount; i++) {
                if (drawn[i] == data.targetEntity) { alreadyDrawn = true; break; }
            }
            if (alreadyDrawn) continue;
            drawn[drawnCount++] = data.targetEntity;

            TransformComponent sibTransform = SandboxComponentRetriever.get(data.targetEntity, TransformComponent.class);
            if (sibTransform == null) continue;

            // Read sibling's precomputed parent-local AABB
            BoundingBoxComponent sibBB = SandboxComponentRetriever.get(data.targetEntity, BoundingBoxComponent.class);
            DimensionsComponent sibDim = SandboxComponentRetriever.get(data.targetEntity, DimensionsComponent.class);
            if (sibBB == null && sibDim == null) continue;

            float minX, minY, maxX, maxY;
            if (sibBB != null) {
                minX = sibTransform.x + sibBB.parentLocalAABB.x;
                minY = sibTransform.y + sibBB.parentLocalAABB.y;
                maxX = minX + sibBB.parentLocalAABB.width;
                maxY = minY + sibBB.parentLocalAABB.height;
            } else {
                minX = sibTransform.x;
                minY = sibTransform.y;
                maxX = minX + sibDim.width;
                maxY = minY + sibDim.height;
            }

            // Convert AABB corners to SubFollower coords
            float fx0 = toFollowerX(minX, minY), fy0 = toFollowerY(minX, minY);
            float fx1 = toFollowerX(maxX, minY), fy1 = toFollowerY(maxX, minY);
            float fx2 = toFollowerX(maxX, maxY), fy2 = toFollowerY(maxX, maxY);
            float fx3 = toFollowerX(minX, maxY), fy3 = toFollowerY(minX, maxY);

            shapeDrawer.setColor(SIBLING_BOUNDS_COLOR);
            float bw = 1f;
            drawDashedLine(fx0, fy0, fx1, fy1, bw, 4f);
            drawDashedLine(fx1, fy1, fx2, fy2, bw, 4f);
            drawDashedLine(fx2, fy2, fx3, fy3, bw, 4f);
            drawDashedLine(fx3, fy3, fx0, fy0, bw, 4f);
        }
    }

    // ----------------------------------------------------------------
    // Constraint line drawing
    // ----------------------------------------------------------------

    /**
     * Draws a single constraint line from the entity's AABB edge midpoint
     * to the target anchor (parent edge or sibling AABB edge).
     */
    private void drawConstraint(Batch batch, LayoutComponent.ConstraintData data,
                                 TransformComponent transform, DimensionsComponent parentDim,
                                 boolean horizontal, boolean startSide,
                                 float lineWidth, float anchorRadius) {
        if (data == null) return;

        Color color = horizontal ? HORIZONTAL_COLOR : VERTICAL_COLOR;

        // Entity edge midpoint in parent-world coordinates (from entity AABB)
        float entityEdgeX, entityEdgeY;
        if (horizontal) {
            entityEdgeX = transform.x + (startSide ? entityAABBLeft : entityAABBRight);
            entityEdgeY = transform.y + (entityAABBBottom + entityAABBTop) / 2f;
        } else {
            entityEdgeX = transform.x + (entityAABBLeft + entityAABBRight) / 2f;
            entityEdgeY = transform.y + (startSide ? entityAABBBottom : entityAABBTop);
        }

        // Target anchor point in parent-world coordinates
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
            // Sibling: line to the midpoint of the target AABB side
            TransformComponent sibTransform = SandboxComponentRetriever.get(data.targetEntity, TransformComponent.class);
            if (sibTransform == null) return;

            BoundingBoxComponent sibBB = SandboxComponentRetriever.get(data.targetEntity, BoundingBoxComponent.class);
            DimensionsComponent sibDim = SandboxComponentRetriever.get(data.targetEntity, DimensionsComponent.class);
            if (sibBB == null && sibDim == null) return;

            Rectangle aabb;
            if (sibBB != null) {
                aabb = sibBB.parentLocalAABB;
            } else {
                aabb = new Rectangle(0, 0, sibDim.width, sibDim.height);
            }

            targetX = resolveSiblingAABBSideX(data.targetSide, sibTransform, aabb);
            targetY = resolveSiblingAABBSideY(data.targetSide, sibTransform, aabb);
        }

        // Convert to SubFollower drawing coords
        float ex = toFollowerX(entityEdgeX, entityEdgeY);
        float ey = toFollowerY(entityEdgeX, entityEdgeY);
        float tx = toFollowerX(targetX, targetY);
        float ty = toFollowerY(targetX, targetY);

        shapeDrawer.setColor(color);

        float labelX, labelY;

        // Use a cubic bezier for sibling constraints when the endpoints are
        // not axis-aligned — gives a professional "wire" look where the curve
        // leaves each face perpendicularly.  When the cross-axis offset is
        // negligible a straight dashed line is cleaner and cheaper.
        float crossDelta = horizontal ? Math.abs(ey - ty) : Math.abs(ex - tx);
        if (data.targetEntity != -1 && crossDelta > 5f) {
            // Entity edge outward direction (in parent-world axis)
            float eDirX, eDirY;
            if (horizontal) { eDirX = startSide ? -1 : 1; eDirY = 0; }
            else             { eDirX = 0; eDirY = startSide ? -1 : 1; }

            // Target face outward direction
            float tDirX = 0, tDirY = 0;
            if (data.targetSide != null) {
                switch (data.targetSide) {
                    case LEFT:   tDirX = -1; break;
                    case RIGHT:  tDirX =  1; break;
                    case BOTTOM: tDirY = -1; break;
                    case TOP:    tDirY =  1; break;
                }
            }

            // Compute control points in parent-world, then convert to SubFollower
            float dist = (float) Math.sqrt((tx - ex) * (tx - ex) + (ty - ey) * (ty - ey));
            float cpDist = Math.max(dist * 0.35f, 15f);
            float cpDistWorld = cpDist / drawScale;

            float cx1 = toFollowerX(entityEdgeX + eDirX * cpDistWorld, entityEdgeY + eDirY * cpDistWorld);
            float cy1 = toFollowerY(entityEdgeX + eDirX * cpDistWorld, entityEdgeY + eDirY * cpDistWorld);
            float cx2 = toFollowerX(targetX + tDirX * cpDistWorld, targetY + tDirY * cpDistWorld);
            float cy2 = toFollowerY(targetX + tDirX * cpDistWorld, targetY + tDirY * cpDistWorld);

            drawDashedBezier(ex, ey, cx1, cy1, cx2, cy2, tx, ty, lineWidth, 6f, 24);

            // Label at the bezier midpoint (t = 0.5)
            labelX = 0.125f * ex + 0.375f * cx1 + 0.375f * cx2 + 0.125f * tx;
            labelY = 0.125f * ey + 0.375f * cy1 + 0.375f * cy2 + 0.125f * ty;
        } else {
            drawDashedLine(ex, ey, tx, ty, lineWidth, 6f);
            labelX = (ex + tx) / 2f;
            labelY = (ey + ty) / 2f;
        }

        shapeDrawer.setColor(ANCHOR_COLOR);
        shapeDrawer.filledCircle(ex, ey, anchorRadius);
        shapeDrawer.filledCircle(tx, ty, anchorRadius);

        if (data.margin != 0 && font != null) {
            drawMarginLabel(batch, data.margin, labelX, labelY, color);
        }
    }

    // ----------------------------------------------------------------
    // Side resolution helpers
    // ----------------------------------------------------------------

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

    private float resolveSiblingAABBSideX(LayoutComponent.ConstraintSide side,
                                           TransformComponent sibTransform, Rectangle aabb) {
        if (side == null) return 0;
        switch (side) {
            case LEFT: return sibTransform.x + aabb.x;
            case RIGHT: return sibTransform.x + aabb.x + aabb.width;
            case BOTTOM:
            case TOP: return sibTransform.x + aabb.x + aabb.width / 2f;
            default: return 0;
        }
    }

    private float resolveSiblingAABBSideY(LayoutComponent.ConstraintSide side,
                                           TransformComponent sibTransform, Rectangle aabb) {
        if (side == null) return 0;
        switch (side) {
            case BOTTOM: return sibTransform.y + aabb.y;
            case TOP: return sibTransform.y + aabb.y + aabb.height;
            case LEFT:
            case RIGHT: return sibTransform.y + aabb.y + aabb.height / 2f;
            default: return 0;
        }
    }

    // ----------------------------------------------------------------
    // Drawing utilities
    // ----------------------------------------------------------------

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

    /**
     * Draws a dashed cubic bezier curve.  The curve is sampled into straight
     * segments and the dash pattern is advanced along the arc length so
     * dashes stay uniform even around tight bends.
     */
    private void drawDashedBezier(float x1, float y1, float cx1, float cy1,
                                   float cx2, float cy2, float x2, float y2,
                                   float lineWidth, float dashLength, int segments) {
        float prevX = x1, prevY = y1;
        float dashRemaining = dashLength;
        boolean drawing = true;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float u = 1 - t;

            // Cubic bezier
            float px = u * u * u * x1 + 3 * u * u * t * cx1 + 3 * u * t * t * cx2 + t * t * t * x2;
            float py = u * u * u * y1 + 3 * u * u * t * cy1 + 3 * u * t * t * cy2 + t * t * t * y2;

            float dx = px - prevX;
            float dy = py - prevY;
            float segLen = (float) Math.sqrt(dx * dx + dy * dy);

            if (segLen > 0.001f) {
                float consumed = 0;
                while (consumed < segLen) {
                    float step = Math.min(dashRemaining, segLen - consumed);
                    if (drawing) {
                        float t0 = consumed / segLen;
                        float t1 = (consumed + step) / segLen;
                        shapeDrawer.line(
                                prevX + dx * t0, prevY + dy * t0,
                                prevX + dx * t1, prevY + dy * t1,
                                lineWidth);
                    }
                    consumed += step;
                    dashRemaining -= step;
                    if (dashRemaining <= 0.001f) {
                        drawing = !drawing;
                        dashRemaining = dashLength;
                    }
                }
            }

            prevX = px;
            prevY = py;
        }
    }
}
