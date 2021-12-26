package games.rednblack.editor.controller.commands.component;

import com.esotericsoftware.spine.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.spine.SpineComponent;

public class ReplaceSpineCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private String backupAnimName;
    private SkeletonJson backupSkeletonJson;
    private Skeleton backupSkeleton;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        String animName = (String) payload[1];
        SkeletonJson skeletonJson = (SkeletonJson) payload[2];
        Skeleton skeleton = (Skeleton) payload[3];
        SkeletonData skeletonData = skeleton.getData();

        entityId = EntityUtils.getEntityId(entity);

        SpineComponent spineComponent = SandboxComponentRetriever.get(entity, SpineComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);

        backupAnimName = spineComponent.animationName;
        backupSkeletonJson = spineComponent.skeletonJson;
        backupSkeleton = spineComponent.skeleton;

        spineComponent.animationName = animName;
        spineComponent.skeletonJson = skeletonJson;
        spineComponent.skeletonData = skeletonData;
        spineComponent.skeleton = skeleton;

        AnimationStateData stateData = new AnimationStateData(skeletonData);
        spineComponent.state = new AnimationState(stateData);

        spineComponent.computeBoundBox(dimensionsComponent);
        dimensionsComponent.width *= spineComponent.worldMultiplier;
        dimensionsComponent.height *= spineComponent.worldMultiplier;

        transformComponent.originX = dimensionsComponent.width / 2f;
        transformComponent.originY = dimensionsComponent.height / 2f;

        String currentAnimName = skeletonData.getAnimations().get(0).getName();
        spineComponent.currentAnimationName = currentAnimName;
        spineComponent.setAnimation(currentAnimName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        SpineComponent spineComponent = SandboxComponentRetriever.get(entity, SpineComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);

        spineComponent.animationName = backupAnimName;
        spineComponent.skeletonJson = backupSkeletonJson;
        spineComponent.skeletonData = backupSkeleton.getData();
        spineComponent.skeleton = backupSkeleton;

        AnimationStateData stateData = new AnimationStateData(spineComponent.skeletonData);
        spineComponent.state = new AnimationState(stateData);

        spineComponent.computeBoundBox(dimensionsComponent);
        dimensionsComponent.width *= spineComponent.worldMultiplier;
        dimensionsComponent.height *= spineComponent.worldMultiplier;

        transformComponent.originX = dimensionsComponent.width / 2f;
        transformComponent.originY = dimensionsComponent.height / 2f;

        String currentAnimName = spineComponent.skeletonData.getAnimations().get(0).getName();
        spineComponent.currentAnimationName = currentAnimName;
        spineComponent.setAnimation(currentAnimName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
