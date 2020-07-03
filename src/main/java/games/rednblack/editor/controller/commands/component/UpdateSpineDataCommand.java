package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.SpineDataComponent;
import games.rednblack.editor.renderer.data.SpineVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extention.spine.SpineObjectComponent;

public class UpdateSpineDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private SpineVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        SpineVO vo = (SpineVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        backup = new SpineVO();
        backup.loadFromEntity(entity);

        SpineDataComponent spineDataComponent = ComponentRetriever.get(entity, SpineDataComponent.class);
        SpineObjectComponent spineObjectComponent = ComponentRetriever.get(entity, SpineObjectComponent.class);

        spineDataComponent.currentAnimationName = vo.currentAnimationName;
        spineObjectComponent.setAnimation(vo.currentAnimationName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);
        SpineDataComponent spineDataComponent = ComponentRetriever.get(entity, SpineDataComponent.class);
        SpineObjectComponent spineObjectComponent = ComponentRetriever.get(entity, SpineObjectComponent.class);

        spineDataComponent.currentAnimationName = backup.currentAnimationName;
        spineObjectComponent.setAnimation(backup.currentAnimationName);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(Entity entity, SpineVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
