package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdateLightBodyDataCommand;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.data.LightBodyDataVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.puremvc.java.interfaces.INotification;

public class UILightBodyPropertiesMediator extends UIItemPropertiesMediator<UILightBodyProperties> {

    private static final String TAG = UILightBodyPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private LightBodyComponent lightComponent;

    public UILightBodyPropertiesMediator() {
        super(NAME, new UILightBodyProperties());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UILightBodyProperties.CLOSE_CLICKED,
                UILightBodyProperties.LIGHT_COLOR_BUTTON_CLICKED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UILightBodyProperties.CLOSE_CLICKED:
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, LightBodyComponent.class));
                break;
            case UILightBodyProperties.LIGHT_COLOR_BUTTON_CLICKED:
                Color prevColor = viewComponent.getLightColor().cpy();
                ColorPicker picker = new HyperLapColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void finished(Color newColor) {
                        lightComponent = SandboxComponentRetriever.get(observableReference, LightBodyComponent.class);
                        lightComponent.color[0] = prevColor.r;
                        lightComponent.color[1] = prevColor.g;
                        lightComponent.color[2] = prevColor.b;
                        lightComponent.color[3] = prevColor.a;

                        viewComponent.setLightColor(newColor);
                        facade.sendNotification(viewComponent.getUpdateEventName());
                    }
                    @Override
                    public void changed(Color newColor) {
                        lightComponent = SandboxComponentRetriever.get(observableReference, LightBodyComponent.class);
                        lightComponent.color[0] = newColor.r;
                        lightComponent.color[1] = newColor.g;
                        lightComponent.color[2] = newColor.b;
                        lightComponent.color[3] = newColor.a;
                    }
                });

                if (notification.getBody() != null) {
                    viewComponent.setLightColor(notification.getBody());
                }
                picker.setColor(viewComponent.getLightColor());
                Sandbox.getInstance().getUIStage().addActor(picker.fadeIn());
                break;
        }
    }

    @Override
    protected void translateObservableDataToView(int item) {
        lightComponent = SandboxComponentRetriever.get(item, LightBodyComponent.class);
        viewComponent.setDirection(lightComponent.rayDirection);
        viewComponent.setDistance(lightComponent.distance + "");
        viewComponent.setLightIntensity(lightComponent.intensity + "");
        viewComponent.setRays(lightComponent.rays + "");
        viewComponent.setSoft(lightComponent.isSoft);
        viewComponent.setSoftnessLength(lightComponent.softnessLength + "");
        viewComponent.setLightHeight(lightComponent.height + "");
        viewComponent.setStatic(lightComponent.isStatic);
        viewComponent.setXRay(lightComponent.isXRay);
        viewComponent.setActive(lightComponent.isActive);
        viewComponent.setLightColor(new Color(lightComponent.color[0], lightComponent.color[1], lightComponent.color[2], lightComponent.color[3]));
        viewComponent.setFalloff(lightComponent.falloff);
    }

    @Override
    protected void translateViewToItemData() {
        lightComponent = SandboxComponentRetriever.get(observableReference, LightBodyComponent.class);
        LightBodyDataVO oldPayloadVo = new LightBodyDataVO();
        oldPayloadVo.loadFromComponent(lightComponent);

        LightBodyDataVO payloadVo = new LightBodyDataVO();
        payloadVo.loadFromComponent(lightComponent);

        payloadVo.rayDirection = viewComponent.getDirection();
        payloadVo.distance = NumberUtils.toFloat(viewComponent.getDistance());
        payloadVo.intensity = NumberUtils.toFloat(viewComponent.getLightIntensity());
        payloadVo.softnessLength = NumberUtils.toFloat(viewComponent.getSoftnessLength());
        payloadVo.height = NumberUtils.toFloat(viewComponent.getLightHeight());
        payloadVo.rays = NumberUtils.toInt(viewComponent.getRays());
        payloadVo.isSoft = viewComponent.isSoft();
        payloadVo.isStatic = viewComponent.isStatic();
        payloadVo.isXRay = viewComponent.isXRay();
        payloadVo.falloff.set(viewComponent.getFalloff());
        Color color = viewComponent.getLightColor();
        payloadVo.color[0] = color.r;
        payloadVo.color[1] = color.g;
        payloadVo.color[2] = color.b;
        payloadVo.color[3] = color.a;
        payloadVo.isActive = viewComponent.isActive();

        if (!oldPayloadVo.equals(payloadVo)) {
            Object payload = UpdateLightBodyDataCommand.payload(observableReference, payloadVo);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_BODY_LIGHT_DATA, payload);
        }
    }
}
