package games.rednblack.editor.plugin.tiled.view;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;

public class SpineDrawable extends BaseDrawable {
    private float minX = 0;
    private float minY = 0;

    private Skeleton skeleton;
    private AnimationState animationState;
    private SkeletonRenderer skeletonRenderer;

    public float width, height;
    private FloatArray temp;

    public SpineDrawable(Skeleton skeleton, SkeletonRenderer skeletonRenderer) {
        temp = new FloatArray();
        this.skeletonRenderer = skeletonRenderer;

        this.skeleton = skeleton;
        AnimationStateData animationStateData = new AnimationStateData(skeleton.getData());
        animationState = new AnimationState(animationStateData);

        computeBoundBox();

        float scaleFactor;
        if (this.width > this.height) {
            //scale by width
            scaleFactor = 1.0f / (this.width / 40);
        } else {
            scaleFactor = 1.0f / (this.height / 40);
        }
        skeleton.setScale(scaleFactor, scaleFactor);
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public AnimationState getAnimationState() {
        return animationState;
    }

    private void computeBoundBox() {
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
                RegionAttachment region = (RegionAttachment)attachment;
                verticesLength = 8;
                vertices = temp.setSize(8);
                region.computeWorldVertices(slot, vertices, 0, 2);
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
        width = (maxX - minX);
        height = (maxY - minY);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        skeleton.updateWorldTransform();
        animationState.update(Gdx.graphics.getDeltaTime());
        animationState.apply(skeleton);
        skeleton.setPosition(x, y - 20);

        Color color = skeleton.getColor();

        float oldAlpha = color.a;
        skeleton.getColor().a *= batch.getColor().a;
        skeletonRenderer.draw(batch, skeleton);
        color.a = oldAlpha;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
}

