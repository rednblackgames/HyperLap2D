package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.h2d.extension.spine.SpineVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.spine.SpineComponent;

public class UpdateSpineDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private SpineVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        SpineVO vo = (SpineVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        backup = new SpineVO();
        backup.loadFromEntity(entity, sandbox.getEngine(), sandbox.sceneControl.sceneLoader.getEntityFactory());

        SpineComponent spineComponent = SandboxComponentRetriever.get(entity, SpineComponent.class);

        spineComponent.currentAnimationName = vo.currentAnimationName;
        spineComponent.currentSkinName = vo.currentSkinName;
        spineComponent.setAnimation(vo.currentAnimationName);
        spineComponent.setSkin(vo.currentSkinName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        SpineComponent spineComponent = SandboxComponentRetriever.get(entity, SpineComponent.class);

        spineComponent.currentAnimationName = backup.currentAnimationName;
        spineComponent.currentSkinName = backup.currentSkinName;
        spineComponent.setAnimation(backup.currentAnimationName);
        spineComponent.setSkin(backup.currentSkinName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, SpineVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
