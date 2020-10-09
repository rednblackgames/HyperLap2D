package games.rednblack.h2d.extention.spine;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.esotericsoftware.spine.SkeletonRenderer;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.systems.render.logic.Drawable;

public class SpineDrawableLogic implements Drawable {
    private final ComponentMapper<SpineObjectComponent> spineObjectComponentMapper = ComponentMapper.getFor(SpineObjectComponent.class);
    private final ComponentMapper<TransformComponent> transformComponentMapper = ComponentMapper.getFor(TransformComponent.class);
    private final ComponentMapper<SpineObjectComponent> spineMapper;
    private final SkeletonRenderer skeletonRenderer;

    public SpineDrawableLogic() {
        spineMapper = ComponentMapper.getFor(SpineObjectComponent.class);
        skeletonRenderer = new SkeletonRenderer();
    }

    @Override
    public void draw(Batch batch, Entity entity, float parentAlpha) {
        SpineObjectComponent spineObjectComponent = spineMapper.get(entity);

        ParentNodeComponent parentNodeComponent = entity.getComponent(ParentNodeComponent.class);
        Entity parentEntity = parentNodeComponent.parentEntity;
        TransformComponent parentTransformComponent = transformComponentMapper.get(parentEntity);

        if (parentTransformComponent.scaleX == 1 && parentTransformComponent.scaleY == 1 && parentTransformComponent.rotation == 0) {
            computeTransform(entity);
            applyTransform(entity, batch);
            skeletonRenderer.draw((PolygonSpriteBatch)batch, spineObjectComponent.skeleton);
            resetTransform(entity, batch);
        } else {
            skeletonRenderer.draw((PolygonSpriteBatch)batch, spineObjectComponent.skeleton);
        }
        //TODO parent alpha thing
    }

    protected Matrix4 computeTransform (Entity rootEntity) {
        SpineObjectComponent spineObjectComponent = spineObjectComponentMapper.get(rootEntity);
        TransformComponent curTransform = transformComponentMapper.get(rootEntity);

        Affine2 worldTransform = spineObjectComponent.worldTransform;

        float originX = curTransform.originX;
        float originY = curTransform.originY;
        float x = curTransform.x;
        float y = curTransform.y;
        float rotation = curTransform.rotation;
        float scaleX = curTransform.scaleX;
        float scaleY = curTransform.scaleY;

        worldTransform.setToTrnRotScl(x + originX , y + originY, rotation, scaleX, scaleY);
        if (originX != 0 || originY != 0) worldTransform.translate(-originX, -originY);
        worldTransform.translate(-spineObjectComponent.minX, -spineObjectComponent.minY);

        spineObjectComponent.computedTransform.set(worldTransform);

        return spineObjectComponent.computedTransform;
    }

    protected void applyTransform (Entity rootEntity, Batch batch) {
        SpineObjectComponent spineObjectComponent = spineObjectComponentMapper.get(rootEntity);
        spineObjectComponent.oldTransform.set(batch.getTransformMatrix());
        batch.setTransformMatrix(spineObjectComponent.computedTransform);
    }

    protected void resetTransform (Entity rootEntity, Batch batch) {
        SpineObjectComponent spineObjectComponent = spineObjectComponentMapper.get(rootEntity);
        batch.setTransformMatrix(spineObjectComponent.oldTransform);
    }
}