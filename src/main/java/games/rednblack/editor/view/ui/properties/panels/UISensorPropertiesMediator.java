package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdateSensorDataCommand;
import games.rednblack.editor.renderer.components.physics.SensorComponent;
import games.rednblack.editor.renderer.data.SensorDataVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * The properties mediator for the sensors.
 * 
 * @author Jan-Thierry Wegener
 */
public class UISensorPropertiesMediator extends UIItemPropertiesMediator<UISensorProperties> {

	private static final String TAG = UISensorPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;
    
    private SensorComponent sensorComponent;

    public UISensorPropertiesMediator() {
        super(NAME, new UISensorProperties());
	}

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UISensorProperties.CLOSE_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UISensorProperties.CLOSE_CLICKED:
                Facade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, SensorComponent.class));
                break;
        }
    }

	@Override
	protected void translateObservableDataToView(int item) {
		sensorComponent = SandboxComponentRetriever.get(item, SensorComponent.class);

        viewComponent.getSensorBottomBox().setChecked(sensorComponent.bottom);
        viewComponent.getSensorLeftBox().setChecked(sensorComponent.left);
        viewComponent.getSensorRightBox().setChecked(sensorComponent.right);
        viewComponent.getSensorTopBox().setChecked(sensorComponent.top);

        viewComponent.getSensorSpanPercentBottomTextfield().setText(Float.toString(sensorComponent.bottomSpanPercent));
        viewComponent.getSensorSpanPercentLeftTextfield().setText(Float.toString(sensorComponent.leftSpanPercent));
        viewComponent.getSensorSpanPercentRightTextfield().setText(Float.toString(sensorComponent.rightSpanPercent));
        viewComponent.getSensorSpanPercentTopTextfield().setText(Float.toString(sensorComponent.topSpanPercent));

        viewComponent.getSensorHeightPercentBottomTextfield().setText(Float.toString(sensorComponent.bottomHeightPercent));
        viewComponent.getSensorWidthPercentLeftTextfield().setText(Float.toString(sensorComponent.leftWidthPercent));
        viewComponent.getSensorWidthPercentRightTextfield().setText(Float.toString(sensorComponent.rightWidthPercent));
        viewComponent.getSensorHeightPercentTopTextfield().setText(Float.toString(sensorComponent.topHeightPercent));
	}

	@Override
	protected void translateViewToItemData() {
        sensorComponent = SandboxComponentRetriever.get(observableReference, SensorComponent.class);

        SensorDataVO oldPayloadVo = new SensorDataVO();
        oldPayloadVo.loadFromComponent(sensorComponent);
        
        SensorDataVO payloadVo = new SensorDataVO();
        
        payloadVo.bottom = viewComponent.getSensorBottomBox().isChecked();
        payloadVo.left = viewComponent.getSensorLeftBox().isChecked();
        payloadVo.right = viewComponent.getSensorRightBox().isChecked();
        payloadVo.top = viewComponent.getSensorTopBox().isChecked();

        payloadVo.bottomSpanPercent = NumberUtils.toFloat(viewComponent.getSensorSpanPercentBottomTextfield().getText());
        payloadVo.leftSpanPercent = NumberUtils.toFloat(viewComponent.getSensorSpanPercentLeftTextfield().getText());
        payloadVo.rightSpanPercent = NumberUtils.toFloat(viewComponent.getSensorSpanPercentRightTextfield().getText());
        payloadVo.topSpanPercent = NumberUtils.toFloat(viewComponent.getSensorSpanPercentTopTextfield().getText());

        payloadVo.bottomHeightPercent = NumberUtils.toFloat(viewComponent.getSensorHeightPercentBottomTextfield().getText());
        payloadVo.leftWidthPercent = NumberUtils.toFloat(viewComponent.getSensorWidthPercentLeftTextfield().getText());
        payloadVo.rightWidthPercent = NumberUtils.toFloat(viewComponent.getSensorWidthPercentRightTextfield().getText());
        payloadVo.topHeightPercent = NumberUtils.toFloat(viewComponent.getSensorHeightPercentTopTextfield().getText());

        if (!oldPayloadVo.equals(payloadVo)) {
            Object payload = UpdateSensorDataCommand.payload(observableReference, payloadVo);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_SENSOR_DATA, payload);
        }
	}
    
    
    
}
