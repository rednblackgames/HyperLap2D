package games.rednblack.h2d.extention.spine;

import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import games.rednblack.editor.renderer.components.BaseComponent;
import games.rednblack.editor.renderer.components.DimensionsComponent;

public class SpineObjectComponent implements BaseComponent {
	public SkeletonData skeletonData;
	public Skeleton skeleton;
	public SkeletonJson skeletonJson;
    public AnimationState state;
    public float minX;
    public float minY;
    public float worldMultiplier;
    private final FloatArray temp = new FloatArray();
    public final Affine2 worldTransform = new Affine2();
    public final Matrix4 computedTransform = new Matrix4();
    public final Matrix4 oldTransform = new Matrix4();

    public Array<Animation> getAnimations() {
        return skeletonData.getAnimations();
    }

    public void setAnimation(String animName) {
        state.setAnimation(0, animName, true);
    }

    public AnimationState getState() {
        return state;
    }

    public void computeBoundBox(DimensionsComponent dimensionsComponent) {
        skeleton.updateWorldTransform();
        Array<Slot> drawOrder = skeleton.getDrawOrder();
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        float maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (int i = 0, n = drawOrder.size; i < n; i++) {
            Slot slot = drawOrder.get(i);
            if (!slot.getBone().isActive()) continue;
            int verticesLength = 0;
            float[] vertices = null;
            Attachment attachment = slot.getAttachment();
            if (attachment instanceof RegionAttachment) {
                verticesLength = 8;
                vertices = temp.setSize(8);
                ((RegionAttachment)attachment).computeWorldVertices(slot.getBone(), vertices, 0, 2);
            } else if (attachment instanceof MeshAttachment) {
                MeshAttachment mesh = (MeshAttachment)attachment;
                verticesLength = mesh.getWorldVerticesLength();
                vertices = temp.setSize(verticesLength);
                mesh.computeWorldVertices(slot, 0, verticesLength, vertices, 0, 2);
            }
            if (vertices != null) {
                for (int ii = 0; ii < verticesLength; ii += 2) {
                    float x = vertices[ii], y = vertices[ii + 1];
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }

        dimensionsComponent.width = (maxX - minX);
        dimensionsComponent.height = (maxY - minY);
    }

    @Override
    public void reset() {
        skeletonData = null;
        skeleton = null;
        skeletonJson = null;
        state = null;

        minX = 0;
        minY = 0;

        worldMultiplier = 0;

        temp.clear();
        worldTransform.idt();
        computedTransform.idt();
        oldTransform.idt();
    }
}
