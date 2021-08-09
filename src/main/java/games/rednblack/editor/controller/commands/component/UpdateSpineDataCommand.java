package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.SpineDataComponent;
import games.rednblack.editor.renderer.data.SpineVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extention.spine.SpineObjectComponent;

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
        backup.loadFromEntity(entity, sandbox.getEngine());

        SpineDataComponent spineDataComponent = SandboxComponentRetriever.get(entity, SpineDataComponent.class);
        SpineObjectComponent spineObjectComponent = SandboxComponentRetriever.get(entity, SpineObjectComponent.class);

        spineDataComponent.currentAnimationName = vo.currentAnimationName;
        spineObjectComponent.setAnimation(vo.currentAnimationName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        SpineDataComponent spineDataComponent = SandboxComponentRetriever.get(entity, SpineDataComponent.class);
        SpineObjectComponent spineObjectComponent = SandboxComponentRetriever.get(entity, SpineObjectComponent.class);

        spineDataComponent.currentAnimationName = backup.currentAnimationName;
        spineObjectComponent.setAnimation(backup.currentAnimationName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, SpineVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
