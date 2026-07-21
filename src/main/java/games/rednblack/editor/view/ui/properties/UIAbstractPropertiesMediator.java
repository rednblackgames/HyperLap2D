package games.rednblack.editor.view.ui.properties;
import games.rednblack.editor.proxy.PluginUIBridge;

import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public abstract class UIAbstractPropertiesMediator<T, V extends UIAbstractProperties> extends Mediator<V> {
    protected Sandbox sandbox;

    protected T observableReference;

    protected boolean lockUpdates = true;

    public UIAbstractPropertiesMediator(String mediatorName, V viewComponent) {
        super(mediatorName, viewComponent);

        sandbox = PluginUIBridge.get().getSandbox();
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.ITEM_DATA_UPDATED,
                viewComponent.getUpdateEventName());
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);


        if(notification.getName().equals(viewComponent.getUpdateEventName())) {
            if(!lockUpdates) {
                translateViewToItemData();
            }
        }

        switch (notification.getName()) {
            case MsgAPI.ITEM_DATA_UPDATED:
                onItemDataUpdate();
                break;
            default:
                break;
        }
    }

    public void setItem(T item) {
        observableReference = item;
        lockUpdates = true;
        translateObservableDataToView(observableReference);
        lockUpdates = false;
    }

    public void onItemDataUpdate() {
        lockUpdates = true;
        translateObservableDataToView(observableReference);
        lockUpdates = false;
    }

    /**
     * Programmatic entry point that runs the same view-to-data translation the user-typing
     * path runs (reading widget values and sending the update command). Used by the MCP
     * RemoteOps path after setting widget values directly on a transient, unregistered
     * panel instance — no notification/event is fired, so there is no conflict with the
     * live UI's mediators.
     */
    public void applyViewToItemData() {
        translateViewToItemData();
    }

    protected abstract void translateObservableDataToView(T item);

    protected abstract void translateViewToItemData();
}