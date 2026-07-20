package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.component.UpdateCircleShapeCommand;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.renderer.components.shape.CircleShapeComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIRemovableComponentPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import org.apache.commons.lang3.ArrayUtils;

public class UICircleShapePropertiesMediator extends UIRemovableComponentPropertiesMediator<UICircleShapeProperties> {
    private static final String TAG = UICircleShapePropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UICircleShapePropertiesMediator() {
        super(NAME, new UICircleShapeProperties());
    }

    @Override
    protected String getCloseClickedEventName() {
        return UICircleShapeProperties.CLOSE_CLICKED;
    }

    @Override
    protected Class<? extends Component> getComponentClass() {
        return CircleShapeComponent.class;
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
