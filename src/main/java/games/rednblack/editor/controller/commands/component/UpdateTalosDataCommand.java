package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.h2d.extension.talos.TalosVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.talos.TalosComponent;

public class UpdateTalosDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private TalosVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        TalosVO vo = (TalosVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        backup = new TalosVO();
        backup.loadFromEntity(entity, sandbox.getEngine(), sandbox.sceneControl.sceneLoader.getEntityFactory());

        TalosComponent talosComponent = SandboxComponentRetriever.get(entity, TalosComponent.class);
        talosComponent.transform = vo.transform;
        talosComponent.effect.setPosition(0, 0);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        TalosComponent talosComponent = SandboxComponentRetriever.get(entity, TalosComponent.class);
        talosComponent.effect.setPosition(0, 0);
        talosComponent.transform = backup.transform;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, TalosVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
