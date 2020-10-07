package games.rednblack.h2d.extention.spine;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.esotericsoftware.spine.Bone;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.SpineDataComponent;
import games.rednblack.editor.renderer.components.TransformComponent;

public class SpineSystem extends IteratingSystem {

    private ComponentMapper<SpineObjectComponent> spineObjectComponentMapper = ComponentMapper.getFor(SpineObjectComponent.class);
    private ComponentMapper<TransformComponent> transformComponentMapper = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<DimensionsComponent> dimensionsComponentMapper = ComponentMapper.getFor(DimensionsComponent.class);

    public SpineSystem() {
        super(Family.all(SpineDataComponent.class).get());
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SpineObjectComponent spineObjectComponent = spineObjectComponentMapper.get(entity);
        DimensionsComponent dimensionsComponent = dimensionsComponentMapper.get(entity);
        TransformComponent curTransform = transformComponentMapper.get(entity);

        ParentNodeComponent parentNodeComponent = entity.getComponent(ParentNodeComponent.class);
        Entity parentEntity = parentNodeComponent.parentEntity;
        TransformComponent parentTransformComponent = transformComponentMapper.get(parentEntity);
        float offsetX = 0;
        float offsetY = 0;

        if (parentTransformComponent.scaleX == 1 && parentTransformComponent.scaleY == 1 && parentTransformComponent.rotation == 0) {
            offsetX = parentTransformComponent.x;
            offsetY = parentTransformComponent.y;

            while (true) {
                parentNodeComponent = parentEntity.getComponent(ParentNodeComponent.class);
                if (parentNodeComponent == null) {
                    break;
                }

                parentEntity = parentNodeComponent.parentEntity;
                if (parentEntity == null) {
                    break;
                }

                parentTransformComponent = transformComponentMapper.get(parentEntity);
                offsetX += parentTransformComponent.x;
                offsetY += parentTransformComponent.y;
            }
        }

        Bone rootBone = spineObjectComponent.skeleton.getRootBone();

        rootBone.setScale(curTransform.scaleX * spineObjectComponent.worldMultiplier, curTransform.scaleY * spineObjectComponent.worldMultiplier);
        rootBone.setRotation(curTransform.rotation);
        rootBone.setPosition(curTransform.x + dimensionsComponent.width / 2 + offsetX, curTransform.y + offsetY);

        spineObjectComponent.skeleton.updateWorldTransform(); //
        spineObjectComponent.state.update(deltaTime); // Update the animation time.
        spineObjectComponent.state.apply(spineObjectComponent.skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.
    }
}
