package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.particle.TalosDataComponent;
import games.rednblack.editor.renderer.data.TalosVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.talos.TalosComponent;

public class UpdateTalosDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private TalosVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        TalosVO vo = (TalosVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        backup = new TalosVO();
        backup.loadFromEntity(entity);

        TalosDataComponent dataComponent = ComponentRetriever.get(entity, TalosDataComponent.class);
        dataComponent.transform = vo.transform;

        TalosComponent talosComponent = ComponentRetriever.get(entity, TalosComponent.class);
        talosComponent.effect.setPosition(0, 0);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);

        TalosDataComponent particleComponent = ComponentRetriever.get(entity, TalosDataComponent.class);
        particleComponent.transform = backup.transform;

        TalosComponent talosComponent = ComponentRetriever.get(entity, TalosComponent.class);
        talosComponent.effect.setPosition(0, 0);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(Entity entity, TalosVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
