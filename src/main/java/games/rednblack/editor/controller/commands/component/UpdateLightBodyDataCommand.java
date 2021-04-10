package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.data.LightBodyDataVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateLightBodyDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private LightBodyDataVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        LightBodyDataVO vo = (LightBodyDataVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        LightBodyComponent lightComponent = ComponentRetriever.get(entity, LightBodyComponent.class);

        backup = new LightBodyDataVO();
        backup.loadFromComponent(lightComponent);

        lightComponent.rayDirection = vo.rayDirection;
        lightComponent.distance = vo.distance;
        lightComponent.softnessLength = vo.softnessLength;
        lightComponent.rays = vo.rays;
        lightComponent.isSoft = vo.isSoft;
        lightComponent.isStatic = vo.isStatic;
        lightComponent.isXRay = vo.isXRay;
        lightComponent.color[0] = vo.color[0];
        lightComponent.color[1] = vo.color[1];
        lightComponent.color[2] = vo.color[2];
        lightComponent.color[3] = vo.color[3];
        lightComponent.isActive = vo.isActive;

        lightComponent.scheduleRefresh();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);
        LightBodyComponent lightComponent = ComponentRetriever.get(entity, LightBodyComponent.class);

        lightComponent.rayDirection = backup.rayDirection;
        lightComponent.distance = backup.distance;
        lightComponent.softnessLength = backup.softnessLength;
        lightComponent.rays = backup.rays;
        lightComponent.isSoft = backup.isSoft;
        lightComponent.isStatic = backup.isStatic;
        lightComponent.isXRay = backup.isXRay;
        lightComponent.color[0] = backup.color[0];
        lightComponent.color[1] = backup.color[1];
        lightComponent.color[2] = backup.color[2];
        lightComponent.color[3] = backup.color[3];
        lightComponent.isActive = backup.isActive;

        lightComponent.scheduleRefresh();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(Entity entity, LightBodyDataVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
