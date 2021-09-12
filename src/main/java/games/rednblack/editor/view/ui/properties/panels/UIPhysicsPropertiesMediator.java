package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdatePhysicsDataCommand;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.data.PhysicsBodyDataVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.puremvc.java.interfaces.INotification;

public class UIPhysicsPropertiesMediator extends UIItemPropertiesMediator<UIPhysicsProperties> {

    private static final String TAG = UIPhysicsPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private PhysicsBodyComponent physicsComponent;

    public UIPhysicsPropertiesMediator() {
        super(NAME, new UIPhysicsProperties());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UIPhysicsProperties.CLOSE_CLICKED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UIPhysicsProperties.CLOSE_CLICKED:
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, PhysicsBodyComponent.class));
                break;
        }
    }

    @Override
    protected void translateObservableDataToView(int item) {
        physicsComponent = SandboxComponentRetriever.get(item, PhysicsBodyComponent.class);
        viewComponent.setBodyType(physicsComponent.bodyType);
        viewComponent.getMassField().setText(physicsComponent.mass + "");
        viewComponent.getCenterOfMassXField().setText(physicsComponent.centerOfMass.x + "");
        viewComponent.getCenterOfMassYField().setText(physicsComponent.centerOfMass.y + "");
        viewComponent.getRotationalInertiaField().setText(physicsComponent.rotationalInertia + "");
        viewComponent.getDumpingField().setText(physicsComponent.damping + "");
        viewComponent.getAngularDampingField().setText(physicsComponent.angularDamping + "");
        viewComponent.getGravityScaleField().setText(physicsComponent.gravityScale + "");
        viewComponent.getDensityField().setText(physicsComponent.density + "");
        viewComponent.getFrictionField().setText(physicsComponent.friction + "");
        viewComponent.getRestitutionField().setText(physicsComponent.restitution + "");
        viewComponent.getHeightField().setText(physicsComponent.height + "");
        viewComponent.getAllowSleepBox().setChecked(physicsComponent.allowSleep);
        viewComponent.getAwakeBox().setChecked(physicsComponent.awake);
        viewComponent.getBulletBox().setChecked(physicsComponent.bullet);
        viewComponent.getSensorBox().setChecked(physicsComponent.sensor);
        viewComponent.getFixedRotationBox().setChecked(physicsComponent.fixedRotation);
        viewComponent.setShapeType(physicsComponent.shapeType);
    }

    @Override
    protected void translateViewToItemData() {
        physicsComponent = SandboxComponentRetriever.get(observableReference, PhysicsBodyComponent.class);

        PhysicsBodyDataVO oldPayloadVo = new PhysicsBodyDataVO();
        oldPayloadVo.loadFromComponent(physicsComponent);

        PhysicsBodyDataVO payloadVo = new PhysicsBodyDataVO();

        payloadVo.bodyType = viewComponent.getBodyType();
        payloadVo.mass = NumberUtils.toFloat(viewComponent.getMassField().getText());

        payloadVo.centerOfMass.set(NumberUtils.toFloat(viewComponent.getCenterOfMassXField().getText()), NumberUtils.toFloat(viewComponent.getCenterOfMassYField().getText()));

        payloadVo.rotationalInertia = NumberUtils.toFloat(viewComponent.getRotationalInertiaField().getText());
        payloadVo.damping = NumberUtils.toFloat(viewComponent.getDumpingField().getText());
        payloadVo.angularDamping = NumberUtils.toFloat(viewComponent.getAngularDampingField().getText());
        payloadVo.gravityScale = NumberUtils.toFloat(viewComponent.getGravityScaleField().getText());
        payloadVo.density = NumberUtils.toFloat(viewComponent.getDensityField().getText());
        payloadVo.friction = NumberUtils.toFloat(viewComponent.getFrictionField().getText());
        payloadVo.restitution = NumberUtils.toFloat(viewComponent.getRestitutionField().getText());
        payloadVo.height = NumberUtils.toFloat(viewComponent.getHeightField().getText());

        payloadVo.allowSleep = viewComponent.getAllowSleepBox().isChecked();
        payloadVo.awake = viewComponent.getAwakeBox().isChecked();
        payloadVo.bullet = viewComponent.getBulletBox().isChecked();
        payloadVo.sensor = viewComponent.getSensorBox().isChecked();
        payloadVo.fixedRotation = viewComponent.getFixedRotationBox().isChecked();
        payloadVo.shapeType = viewComponent.getShapeType();

        if (!oldPayloadVo.equals(payloadVo)) {
            Object payload = UpdatePhysicsDataCommand.payload(observableReference, payloadVo);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_PHYSICS_BODY_DATA, payload);
        }
    }
}
