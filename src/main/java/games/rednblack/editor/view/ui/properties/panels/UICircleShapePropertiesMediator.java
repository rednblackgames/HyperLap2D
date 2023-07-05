package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.controller.commands.component.UpdateCircleShapeCommand;
import games.rednblack.editor.renderer.components.shape.CircleShapeComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import org.apache.commons.lang3.ArrayUtils;

public class UICircleShapePropertiesMediator extends UIItemPropertiesMediator<UICircleShapeProperties> {
    private static final String TAG = UICircleShapePropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UICircleShapePropertiesMediator() {
        super(NAME, new UICircleShapeProperties());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(UICircleShapeProperties.CLOSE_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UICircleShapeProperties.CLOSE_CLICKED:
                Facade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, CircleShapeComponent.class));
                break;
        }
    }

    @Override
    protected void translateObservableDataToView(int item) {
        CircleShapeComponent component = SandboxComponentRetriever.get(item, CircleShapeComponent.class);
        viewComponent.setRadius(component.radius);
    }

    @Override
    protected void translateViewToItemData() {
        CircleShapeComponent component = SandboxComponentRetriever.get(observableReference, CircleShapeComponent.class);

        float radius = Float.parseFloat(viewComponent.getRadius());
        if (component.radius != radius) {
            Object payload = UpdateCircleShapeCommand.payload(observableReference, radius);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_CIRCLE_SHAPE, payload);
        }
    }
}
