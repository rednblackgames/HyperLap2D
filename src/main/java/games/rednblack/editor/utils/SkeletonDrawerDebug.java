package games.rednblack.editor.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.Slot;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.ClippingAttachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.PathAttachment;
import com.esotericsoftware.spine.attachments.PointAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class SkeletonDrawerDebug {
    static public final Color BONE_START_COLOR = new Color(0.2f, 0.8f, 1f, 1f);
    static public final Color BONE_END_COLOR = new Color(0.1f, 0.4f, 0.8f, 1f);
    static public final Color JOINT_COLOR = new Color(1f, 1f, 1f, 0.8f);
    static public final Color JOINT_INNER_COLOR = new Color(0.2f, 0.2f, 0.2f, 1f);

    static public final Color ATTACHMENT_LINE_COLOR = new Color(0.6f, 1f, 0.6f, 0.3f);
    static public final Color MESH_FILL_COLOR = new Color(0.6f, 1f, 0.6f, 0.15f);
    static public final Color AABB_FILL_COLOR = new Color(1f, 0.5f, 0f, 0.1f);
    static public final Color AABB_OUTLINE_COLOR = new Color(1f, 0.6f, 0f, 0.8f);

    private final ShapeDrawer drawer;
    private boolean drawBones = true, drawRegionAttachments = true, drawBoundingBoxes = true, drawPoints = true;
    private boolean drawMeshHull = true, drawMeshTriangles = true, drawPaths = true, drawClipping = true, drawMeshFill = true;

    private boolean drawBoneFill = true;
    private float boneWidth = 6f;
    private float scale = 1;

    private final SkeletonBounds bounds = new SkeletonBounds();
    private final FloatArray vertices = new FloatArray(32);
    private final Vector2 temp1 = new Vector2(), temp2 = new Vector2(), temp3 = new Vector2();
    private final FloatArray curveVertices = new FloatArray(64);

    public SkeletonDrawerDebug(ShapeDrawer drawer) {
        if (drawer == null) throw new IllegalArgumentException("drawer cannot be null.");
        this.drawer = drawer;
    }

    public void draw(Skeleton skeleton) {
        if (skeleton == null) throw new IllegalArgumentException("skeleton cannot be null.");

        Array<Bone> bones = skeleton.getBones();
        Array<Slot> slots = skeleton.getSlots();

        if (drawBones) {
            for (int i = 0, n = bones.size; i < n; i++) {
                Bone bone = bones.get(i);
                if (bone.getParent() == null || !bone.isActive()) continue;

                float length = bone.getData().getLength();

                if (length == 0) {
                    drawJoint(bone.getWorldX(), bone.getWorldY(), 4 * scale);
                    continue;
                }

                float x = length * bone.getA() + bone.getWorldX();
                float y = length * bone.getC() + bone.getWorldY();

                if (drawBoneFill) {
                    drawBoneShape(bone.getWorldX(), bone.getWorldY(), x, y, boneWidth * scale);
                } else {
                    drawer.line(bone.getWorldX(), bone.getWorldY(), x, y, 2f * scale, SkeletonDrawerDebug.BONE_START_COLOR, SkeletonDrawerDebug.BONE_END_COLOR);
                }

                drawJoint(bone.getWorldX(), bone.getWorldY(), 3 * scale);
            }

            drawCrosshair(skeleton.getX(), skeleton.getY(), 6 * scale, Color.YELLOW);
        }

        if (drawRegionAttachments) {
            drawer.setColor(ATTACHMENT_LINE_COLOR);
            float lw = 1.5f * scale;
            for (Slot slot : slots) {
                Attachment attachment = slot.getAttachment();
                if (attachment instanceof RegionAttachment) {
                    RegionAttachment region = (RegionAttachment) attachment;
                    float[] vertices = this.vertices.items;
                    region.computeWorldVertices(slot, vertices, 0, 2);
                    drawQuad(vertices, lw);
                }
            }
        }

        if (drawMeshHull || drawMeshTriangles) {
            float lw = 1.0f * scale;
            for (Slot slot : slots) {
                Attachment attachment = slot.getAttachment();
                if (!(attachment instanceof MeshAttachment)) continue;
                MeshAttachment mesh = (MeshAttachment) attachment;
                float[] vertices = this.vertices.setSize(mesh.getWorldVerticesLength());
                mesh.computeWorldVertices(slot, 0, mesh.getWorldVerticesLength(), vertices, 0, 2);
                short[] triangles = mesh.getTriangles();

                if (drawMeshTriangles) {
                    if (drawMeshFill) {
                        drawer.setColor(MESH_FILL_COLOR);
                        for (int ii = 0, nn = triangles.length; ii < nn; ii += 3) {
                            int v1 = triangles[ii] * 2, v2 = triangles[ii + 1] * 2, v3 = triangles[ii + 2] * 2;
                            drawer.filledTriangle(
                                    vertices[v1], vertices[v1+1],
                                    vertices[v2], vertices[v2+1],
                                    vertices[v3], vertices[v3+1]
                            );
                        }
                    }

                    drawer.setColor(ATTACHMENT_LINE_COLOR);
                    for (int ii = 0, nn = triangles.length; ii < nn; ii += 3) {
                        int v1 = triangles[ii] * 2, v2 = triangles[ii + 1] * 2, v3 = triangles[ii + 2] * 2;
                        drawer.triangle(
                                vertices[v1], vertices[v1+1],
                                vertices[v2], vertices[v2+1],
                                vertices[v3], vertices[v3+1],
                                lw
                        );
                    }
                }
            }
        }

        if (drawBoundingBoxes) {
            float lw = 1.5f * scale;
            SkeletonBounds bounds = this.bounds;
            bounds.update(skeleton, true);

            drawer.filledRectangle(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight(), AABB_FILL_COLOR);
            drawer.rectangle(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight(), AABB_OUTLINE_COLOR, lw);

            Array<FloatArray> polygons = bounds.getPolygons();
            for (FloatArray polygon : polygons) {
                drawer.polygon(polygon.items, 0, polygon.size, lw, JoinType.SMOOTH);
            }
        }

        // Clipping
        if (drawClipping) {
            float lw = 2f * scale;
            for (Slot slot : slots) {
                Attachment attachment = slot.getAttachment();
                if (!(attachment instanceof ClippingAttachment)) continue;
                ClippingAttachment clip = (ClippingAttachment) attachment;
                int nn = clip.getWorldVerticesLength();
                float[] vertices = this.vertices.setSize(nn);
                clip.computeWorldVertices(slot, 0, nn, vertices, 0, 2);
                drawer.setColor(Color.MAGENTA);
                for (int ii = 2; ii < nn; ii += 2)
                    drawer.line(vertices[ii - 2], vertices[ii - 1], vertices[ii], vertices[ii + 1], lw);
                drawer.line(vertices[0], vertices[1], vertices[nn - 2], vertices[nn - 1], lw);
            }
        }

        if (drawPaths) {
            drawPaths(slots, scale);
        }

        if (drawPoints) {
            drawer.setColor(Color.YELLOW);
            for (Slot slot : slots) {
                Attachment attachment = slot.getAttachment();
                if (!(attachment instanceof PointAttachment)) continue;
                PointAttachment point = (PointAttachment) attachment;
                point.computeWorldPosition(slot.getBone(), temp1);
                drawDiamond(temp1.x, temp1.y, 5 * scale);
            }
        }
    }

    private void drawBoneShape(float x1, float y1, float x2, float y2, float width) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx*dx + dy*dy);

        if (len == 0) return;

        float nx = dx / len;
        float ny = dy / len;

        float px = -ny;
        float py = nx;

        float baseScale = width * 0.5f;
        float bx1 = x1 + px * baseScale;
        float by1 = y1 + py * baseScale;
        float bx2 = x1 - px * baseScale;
        float by2 = y1 - py * baseScale;

        float tipScale = width * 0.1f;
        float tx1 = x2 + px * tipScale;
        float ty1 = y2 + py * tipScale;
        float tx2 = x2 - px * tipScale;
        float ty2 = y2 - py * tipScale;

        drawer.filledTriangle(bx1, by1, bx2, by2, tx1, ty1,
                BONE_START_COLOR, BONE_START_COLOR, BONE_END_COLOR);

        drawer.filledTriangle(bx2, by2, tx2, ty2, tx1, ty1,
                BONE_START_COLOR, BONE_END_COLOR, BONE_END_COLOR);
    }

    private void drawJoint(float x, float y, float radius) {
        drawer.filledCircle(x, y, radius, JOINT_COLOR);
        drawer.filledCircle(x, y, radius * 0.6f, JOINT_INNER_COLOR);
    }

    private void drawQuad(float[] v, float lw) {
        drawer.line(v[0], v[1], v[2], v[3], lw);
        drawer.line(v[2], v[3], v[4], v[5], lw);
        drawer.line(v[4], v[5], v[6], v[7], lw);
        drawer.line(v[6], v[7], v[0], v[1], lw);
    }

    private void drawCrosshair(float x, float y, float size, Color color) {
        float half = size;
        drawer.line(x - half, y - half, x + half, y + half, color, 2f);
        drawer.line(x - half, y + half, x + half, y - half, color, 2f);
    }

    private void drawDiamond(float x, float y, float radius) {
        drawer.filledTriangle(x, y + radius, x + radius, y, x, y - radius);
        drawer.filledTriangle(x, y - radius, x - radius, y, x, y + radius);
    }

    private void drawPaths(Array<Slot> slots, float scale) {
        float lw = 2f * scale;
        for (Slot slot : slots) {
            Attachment attachment = slot.getAttachment();
            if (!(attachment instanceof PathAttachment)) continue;
            PathAttachment path = (PathAttachment) attachment;
            int nn = path.getWorldVerticesLength();
            float[] vertices = this.vertices.setSize(nn);
            path.computeWorldVertices(slot, 0, nn, vertices, 0, 2);

            Color c = path.getColor();

            float x1 = vertices[2], y1 = vertices[3], x2 = 0, y2 = 0;
            if (path.getClosed()) {
                drawer.setColor(c);
                float cx1 = vertices[0], cy1 = vertices[1], cx2 = vertices[nn - 2], cy2 = vertices[nn - 1];
                x2 = vertices[nn - 4]; y2 = vertices[nn - 3];
                drawCubicBezier(x1, y1, cx1, cy1, cx2, cy2, x2, y2, lw);
            }
            nn -= 4;
            for (int ii = 4; ii < nn; ii += 6) {
                float cx1 = vertices[ii], cy1 = vertices[ii + 1], cx2 = vertices[ii + 2], cy2 = vertices[ii + 3];
                x2 = vertices[ii + 4]; y2 = vertices[ii + 5];
                drawer.setColor(c);
                drawCubicBezier(x1, y1, cx1, cy1, cx2, cy2, x2, y2, lw);
                x1 = x2; y1 = y2;
            }
        }
    }

    private void drawCubicBezier(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2, float lineWidth) {
        int segments = 24;
        curveVertices.clear();
        for (int i = 0; i <= segments; i++) {
            float t = i / (float)segments;
            float t2 = t * t; float t3 = t2 * t;
            float mt = 1 - t; float mt2 = mt * mt; float mt3 = mt2 * mt;
            float x = (mt3 * x1) + (3 * mt2 * t * cx1) + (3 * mt * t2 * cx2) + (t3 * x2);
            float y = (mt3 * y1) + (3 * mt2 * t * cy1) + (3 * mt * t2 * cy2) + (t3 * y2);
            curveVertices.add(x);
            curveVertices.add(y);
        }
        drawer.path(curveVertices, lineWidth, JoinType.SMOOTH, false);

        drawer.line(x1, y1, cx1, cy1, Color.GRAY, 1f);
        drawer.line(x2, y2, cx2, cy2, Color.GRAY, 1f);
        drawer.filledCircle(cx1, cy1, 2f, Color.GRAY);
        drawer.filledCircle(cx2, cy2, 2f, Color.GRAY);
    }

    public ShapeDrawer getShapeDrawer() { return drawer; }
    public void setBones(boolean bones) { this.drawBones = bones; }
    public void setScale(float scale) { this.scale = scale; }
    public void setRegionAttachments(boolean regionAttachments) { drawRegionAttachments = regionAttachments; }
    public void setBoundingBoxes(boolean boundingBoxes) { drawBoundingBoxes = boundingBoxes; }
    public void setMeshHull(boolean meshHull) { drawMeshHull = meshHull; }
    public void setMeshTriangles(boolean meshTriangles) { drawMeshTriangles = meshTriangles; }
    public void setPaths(boolean paths) { drawPaths = paths; }
    public void setPoints(boolean points) { drawPoints = points; }
    public void setClipping(boolean clipping) { drawClipping = clipping; }

    public void setDrawMeshFill(boolean drawMeshFill) {
        this.drawMeshFill = drawMeshFill;
    }
}
