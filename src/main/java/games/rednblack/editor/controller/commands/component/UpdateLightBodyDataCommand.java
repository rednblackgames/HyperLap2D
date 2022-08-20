package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.data.LightBodyDataVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateLightBodyDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private LightBodyDataVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        LightBodyDataVO vo = (LightBodyDataVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        LightBodyComponent lightComponent = SandboxComponentRetriever.get(entity, LightBodyComponent.class);

        backup = new LightBodyDataVO();
        backup.loadFromComponent(lightComponent);

        lightComponent.rayDirection = vo.rayDirection;
        lightComponent.distance = vo.distance;
        lightComponent.softnessLength = vo.softnessLength;
        lightComponent.height = vo.height;
        lightComponent.falloff.set(vo.falloff);
        lightComponent.rays = vo.rays;
        lightComponent.intensity = vo.intensity;
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
        int entity = EntityUtils.getByUniqueId(entityId);
        LightBodyComponent lightComponent = SandboxComponentRetriever.get(entity, LightBodyComponent.class);

        lightComponent.rayDirection = backup.rayDirection;
        lightComponent.distance = backup.distance;
        lightComponent.height = backup.height;
        lightComponent.falloff.set(backup.falloff);
        lightComponent.softnessLength = backup.softnessLength;
        lightComponent.intensity = backup.intensity;
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

    public static Object payload(int entity, LightBodyDataVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
