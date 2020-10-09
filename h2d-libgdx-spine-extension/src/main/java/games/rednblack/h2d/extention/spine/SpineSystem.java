package games.rednblack.h2d.extention.spine;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
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

        Bone rootBone = spineObjectComponent.skeleton.getRootBone();

        if (parentTransformComponent.scaleX == 1 && parentTransformComponent.scaleY == 1 && parentTransformComponent.rotation == 0) {
            rootBone.setScale(spineObjectComponent.worldMultiplier, spineObjectComponent.worldMultiplier);
            rootBone.setRotation(0);
            rootBone.setPosition(spineObjectComponent.rootBonePosition.x, spineObjectComponent.rootBonePosition.y);
        } else {
            rootBone.setScale(curTransform.scaleX * spineObjectComponent.worldMultiplier, curTransform.scaleY * spineObjectComponent.worldMultiplier);
            rootBone.setRotation(curTransform.rotation);
            rootBone.setPosition(curTransform.x + dimensionsComponent.width / 2, curTransform.y);
        }

        spineObjectComponent.skeleton.updateWorldTransform(); //
        spineObjectComponent.state.update(deltaTime); // Update the animation time.
        spineObjectComponent.state.apply(spineObjectComponent.skeleton); // Poses skeleton using current animations. This sets the bones' local SRT.
    }
}
