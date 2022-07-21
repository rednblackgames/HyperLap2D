package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.esotericsoftware.spine.*;
import com.esotericsoftware.spine.attachments.Attachment;
import com.esotericsoftware.spine.attachments.MeshAttachment;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.h2d.extension.spine.SpineDataObject;
import games.rednblack.h2d.extension.spine.SpineItemType;

public class SpineActor extends Actor {

    private String animationName;
    public SkeletonData skeletonData;
    private SkeletonRenderer renderer;
    private Skeleton skeleton;
    private AnimationState state;
    private IResourceRetriever irr;
    private SkeletonJson skeletonJson;
    private float minX = 0;
    private float minY = 0;
    private FloatArray temp;

    public SpineActor(String animationName, IResourceRetriever irr) {
        temp = new FloatArray();
        this.irr = irr;
        this.renderer = new SkeletonRenderer();
        this.animationName = animationName;
        initSkeletonData();
        initSpine();
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
        setWidth(maxX - minX);
        setHeight(maxY - minY);
    }

    private void initSkeletonData() {
        SpineDataObject spineDataObject = (SpineDataObject) irr.getExternalItemType(SpineItemType.SPINE_TYPE, animationName);
        skeletonJson = spineDataObject.skeletonJson;
        skeletonData = spineDataObject.skeletonData;
    }

    private void initSpine() {
        skeleton = new Skeleton(skeletonData);
        skeleton.setScale(getScaleX(), getScaleY());
        AnimationStateData stateData = new AnimationStateData(skeletonData);
        state = new AnimationState(stateData);
        computeBoundBox();
        setAnimation(skeletonData.getAnimations().get(0).getName());
    }

    public Array<Animation> getAnimations() {
        return skeletonData.getAnimations();
    }

    public void setAnimation(String animName) {
        state.setAnimation(0, animName, true);
    }

    public AnimationState getState() {
        return state;
    }

    @Override
    public void setScale(float scale) {
        super.setScale(scale);
        initSpine();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        Color color = skeleton.getColor();

        float oldAlpha = color.a;
        skeleton.getColor().a *= parentAlpha;
        renderer.draw((PolygonSpriteBatch)batch, skeleton);
        color.a = oldAlpha;

        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        super.draw(batch, parentAlpha);
    }

    @Override
    public void act(float delta) {
        skeleton.updateWorldTransform(); //
        state.update(delta);
        state.apply(skeleton);
        skeleton.setPosition(getX() - minX, getY() - minY);
        super.act(delta);
    }
}
