package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.data.PhysicsBodyDataVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdatePhysicsDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private PhysicsBodyDataVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        PhysicsBodyDataVO vo = (PhysicsBodyDataVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        PhysicsBodyComponent physicsComponent = SandboxComponentRetriever.get(entity, PhysicsBodyComponent.class);

        backup = new PhysicsBodyDataVO();
        backup.loadFromComponent(physicsComponent);

        physicsComponent.bodyType = vo.bodyType;
        physicsComponent.mass = vo.mass;

        physicsComponent.centerOfMass.set(vo.centerOfMass);

        physicsComponent.rotationalInertia = vo.rotationalInertia;
        physicsComponent.damping = vo.damping;
        physicsComponent.angularDamping = vo.angularDamping;
        physicsComponent.gravityScale = vo.gravityScale;
        physicsComponent.density = vo.density;
        physicsComponent.friction = vo.friction;
        physicsComponent.restitution = vo.restitution;

        physicsComponent.allowSleep = vo.allowSleep;
        physicsComponent.awake = vo.awake;
        physicsComponent.bullet = vo.bullet;
        physicsComponent.sensor = vo.sensor;
        physicsComponent.fixedRotation = vo.fixedRotation;
        physicsComponent.shapeType = vo.shapeType;

        physicsComponent.height = vo.height;

        physicsComponent.scheduleRefresh();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        PhysicsBodyComponent physicsComponent = SandboxComponentRetriever.get(entity, PhysicsBodyComponent.class);

        physicsComponent.bodyType = backup.bodyType;
        physicsComponent.mass = backup.mass;

        physicsComponent.centerOfMass.set(backup.centerOfMass);

        physicsComponent.rotationalInertia = backup.rotationalInertia;
        physicsComponent.damping = backup.damping;
        physicsComponent.angularDamping = backup.angularDamping;
        physicsComponent.gravityScale = backup.gravityScale;
        physicsComponent.density = backup.density;
        physicsComponent.friction = backup.friction;
        physicsComponent.restitution = backup.restitution;

        physicsComponent.allowSleep = backup.allowSleep;
        physicsComponent.awake = backup.awake;
        physicsComponent.bullet = backup.bullet;
        physicsComponent.sensor = backup.sensor;
        physicsComponent.fixedRotation = backup.fixedRotation;
        physicsComponent.shapeType = backup.shapeType;

        physicsComponent.height = backup.height;

        physicsComponent.scheduleRefresh();

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, PhysicsBodyDataVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
