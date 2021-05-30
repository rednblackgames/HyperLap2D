package games.rednblack.editor.view.ui.properties.panels;

import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import com.badlogic.ashley.core.Entity;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdateSensorDataCommand;
import games.rednblack.editor.renderer.components.physics.SensorComponent;
import games.rednblack.editor.renderer.data.SensorDataVO;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

/**
 * The properties mediator for the sensors.
 * 
 * @author Jan-Thierry Wegener
 */
public class UISensorPropertiesMediator extends UIItemPropertiesMediator<Entity, UISensorProperties> {

	private static final String TAG = UISensorPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;
    
    private SensorComponent sensorComponent;

    public UISensorPropertiesMediator() {
        super(NAME, new UISensorProperties());
	}

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UISensorProperties.CLOSE_CLICKED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UISensorProperties.CLOSE_CLICKED:
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, SensorComponent.class));
                break;
        }
    }

	@Override
	protected void translateObservableDataToView(Entity item) {
		sensorComponent = item.getComponent(SensorComponent.class);

        viewComponent.getSensorBottomBox().setChecked(sensorComponent.bottom);
        viewComponent.getSensorLeftBox().setChecked(sensorComponent.left);
        viewComponent.getSensorRightBox().setChecked(sensorComponent.right);
        viewComponent.getSensorTopBox().setChecked(sensorComponent.top);
	}

	@Override
	protected void translateViewToItemData() {
        sensorComponent = observableReference.getComponent(SensorComponent.class);

        SensorDataVO oldPayloadVo = new SensorDataVO();
        oldPayloadVo.loadFromComponent(sensorComponent);
        
        SensorDataVO payloadVo = new SensorDataVO();
        
        payloadVo.bottom = viewComponent.getSensorBottomBox().isChecked();
        payloadVo.left = viewComponent.getSensorLeftBox().isChecked();
        payloadVo.right = viewComponent.getSensorRightBox().isChecked();
        payloadVo.top = viewComponent.getSensorTopBox().isChecked();

        if (!oldPayloadVo.equals(payloadVo)) {
            Object payload = UpdateSensorDataCommand.payload(observableReference, payloadVo);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_SENSOR_DATA, payload);
        }
	}
    
    
    
}
