package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;
import com.esotericsoftware.spine.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.SpineDataComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extention.spine.SpineObjectComponent;

public class ReplaceSpineCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private String backupAnimName;
    private SkeletonJson backupSkeletonJson;
    private Skeleton backupSkeleton;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        String animName = (String) payload[1];
        SkeletonJson skeletonJson = (SkeletonJson) payload[2];
        Skeleton skeleton = (Skeleton) payload[3];
        SkeletonData skeletonData = skeleton.getData();

        entityId = EntityUtils.getEntityId(entity);

        SpineDataComponent spineDataComponent = ComponentRetriever.get(entity, SpineDataComponent.class);
        SpineObjectComponent spineObjectComponent = ComponentRetriever.get(entity, SpineObjectComponent.class);
        DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);

        backupAnimName = spineDataComponent.animationName;
        backupSkeletonJson = spineObjectComponent.skeletonJson;
        backupSkeleton = spineObjectComponent.skeleton;

        spineDataComponent.animationName = animName;
        spineObjectComponent.skeletonJson = skeletonJson;
        spineObjectComponent.skeletonData = skeletonData;
        spineObjectComponent.skeleton = skeleton;

        AnimationStateData stateData = new AnimationStateData(skeletonData);
        spineObjectComponent.state = new AnimationState(stateData);

        spineObjectComponent.computeBoundBox(dimensionsComponent);
        dimensionsComponent.width *= spineObjectComponent.worldMultiplier;
        dimensionsComponent.height *= spineObjectComponent.worldMultiplier;

        transformComponent.originX = dimensionsComponent.width / 2f;
        transformComponent.originY = dimensionsComponent.height / 2f;

        String currentAnimName = skeletonData.getAnimations().get(0).getName();
        spineDataComponent.currentAnimationName = currentAnimName;
        spineObjectComponent.setAnimation(currentAnimName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);

        SpineDataComponent spineDataComponent = ComponentRetriever.get(entity, SpineDataComponent.class);
        SpineObjectComponent spineObjectComponent = ComponentRetriever.get(entity, SpineObjectComponent.class);
        DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);

        spineDataComponent.animationName = backupAnimName;
        spineObjectComponent.skeletonJson = backupSkeletonJson;
        spineObjectComponent.skeletonData = backupSkeleton.getData();
        spineObjectComponent.skeleton = backupSkeleton;

        AnimationStateData stateData = new AnimationStateData(spineObjectComponent.skeletonData);
        spineObjectComponent.state = new AnimationState(stateData);

        spineObjectComponent.computeBoundBox(dimensionsComponent);
        dimensionsComponent.width *= spineObjectComponent.worldMultiplier;
        dimensionsComponent.height *= spineObjectComponent.worldMultiplier;

        transformComponent.originX = dimensionsComponent.width / 2f;
        transformComponent.originY = dimensionsComponent.height / 2f;

        String currentAnimName = spineObjectComponent.skeletonData.getAnimations().get(0).getName();
        spineDataComponent.currentAnimationName = currentAnimName;
        spineObjectComponent.setAnimation(currentAnimName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
