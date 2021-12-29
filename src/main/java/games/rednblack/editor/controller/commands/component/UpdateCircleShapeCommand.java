package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.shape.CircleShapeComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateCircleShapeCommand extends EntityModifyRevertibleCommand {
    private int entityId;
    private float radiusBackup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        float radius = (float) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        CircleShapeComponent circleShapeComponent = SandboxComponentRetriever.get(entity, CircleShapeComponent.class);

        radiusBackup = circleShapeComponent.radius;

        circleShapeComponent.radius = radius;

        PhysicsBodyComponent physicsBodyComponent = SandboxComponentRetriever.get(entity, PhysicsBodyComponent.class);
        if (physicsBodyComponent != null)
            physicsBodyComponent.scheduleRefresh();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        CircleShapeComponent circleShapeComponent = SandboxComponentRetriever.get(entity, CircleShapeComponent.class);
        circleShapeComponent.radius = radiusBackup;

        PhysicsBodyComponent physicsBodyComponent = SandboxComponentRetriever.get(entity, PhysicsBodyComponent.class);
        if (physicsBodyComponent != null)
            physicsBodyComponent.scheduleRefresh();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, float radius) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = radius;

        return payload;
    }
}
