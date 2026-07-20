package games.rednblack.editor.view.ui.properties;

import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

/**
 * Base mediator for property panels whose component can be removed via a close
 * button (the {@code CLOSE_CLICKED} notification). Centralises the duplicated
 * {@code CLOSE_CLICKED -> ACTION_REMOVE_COMPONENT} handling that was previously
 * copy-pasted across ~9 panel mediators; each subclass only declares which
 * notification name its view fires and which component class to remove.
 *
 * @param <V> the property panel view type
 */
public abstract class UIRemovableComponentPropertiesMediator<V extends UIAbstractProperties>
        extends UIItemPropertiesMediator<V> {

    public UIRemovableComponentPropertiesMediator(String mediatorName, V viewComponent) {
        super(mediatorName, viewComponent);
    }

    /**
     * The notification name fired by the view when its close button is clicked,
     * or {@code null} if this panel is not removable (default). Overridden by
     * removable panels to return their per-class {@code CLOSE_CLICKED} constant.
     */
    protected String getCloseClickedEventName() {
        return null;
    }

    /** The component class to remove from the entity when the close button is clicked. */
    protected abstract Class<? extends Component> getComponentClass();

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        if (getCloseClickedEventName() != null) {
            interests.add(getCloseClickedEventName());
        }
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        String closeEvent = getCloseClickedEventName();
        if (closeEvent != null
                && closeEvent.equals(notification.getName())
                && validReference()) {
            facade.sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT,
                    RemoveComponentFromItemCommand.payload(observableReference, getComponentClass()));
        }
    }
}