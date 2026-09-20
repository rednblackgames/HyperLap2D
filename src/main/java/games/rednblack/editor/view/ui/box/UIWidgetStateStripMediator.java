package games.rednblack.editor.view.ui.box;

import games.rednblack.editor.controller.commands.ChangeWidgetStateCommand;
import games.rednblack.editor.controller.commands.CompositeCameraChangeCommand;
import games.rednblack.editor.proxy.PluginUIBridge;
import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

/**
 * Widget editing mode: while the composite being viewed is a widget (or sits inside one) the state
 * strip is shown for it. Leaving the widget brings it back to its default state.
 */
public class UIWidgetStateStripMediator extends Mediator<UIWidgetStateStrip> {
    private static final String TAG = UIWidgetStateStripMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    /** uniqueId of the widget the strip is shown for, null while hidden */
    private String shownWidgetId = null;

    public UIWidgetStateStripMediator() {
        super(NAME, new UIWidgetStateStrip());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.SCENE_LOADED,
                CompositeCameraChangeCommand.DONE,
                MsgAPI.WIDGET_STATE_CHANGED,
                MsgAPI.ITEM_DATA_UPDATED);
        interests.add(UIWidgetStateStrip.STATE_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                WidgetEditingProxy.get().reset();
                showFor(-1);
                break;
            case CompositeCameraChangeCommand.DONE:
                String viewedId = notification.getBody();
                onViewChanged(EntityUtils.getByUniqueId(viewedId));
                break;
            case MsgAPI.WIDGET_STATE_CHANGED:
            case MsgAPI.ITEM_DATA_UPDATED:
                if (shownWidgetId != null) showFor(EntityUtils.getByUniqueId(shownWidgetId));
                break;
            case UIWidgetStateStrip.STATE_CLICKED:
                if (shownWidgetId != null) {
                    String state = notification.getBody();
                    facade.sendNotification(MsgAPI.ACTION_CHANGE_WIDGET_STATE,
                            ChangeWidgetStateCommand.payload(shownWidgetId, state));
                    // whatever came out of it, the strip shows the state the widget is really in
                    if (shownWidgetId != null) showFor(EntityUtils.getByUniqueId(shownWidgetId));
                }
                break;
            default:
                break;
        }
    }

    private void onViewChanged(int viewedEntity) {
        int widget = WidgetEditingProxy.findWidget(viewedEntity);
        String widgetId = widget == -1 ? null : EntityUtils.getEntityId(widget);

        // a widget only shows a non default state while it is being edited from the inside
        String editedId = WidgetEditingProxy.get().getEditedWidgetId();
        if (editedId != null && !editedId.equals(widgetId)) {
            int edited = EntityUtils.getByUniqueId(editedId);
            WidgetComponent editedWidget = edited == -1 ? null : SandboxComponentRetriever.get(edited, WidgetComponent.class);
            if (editedWidget != null) {
                facade.sendNotification(MsgAPI.ACTION_CHANGE_WIDGET_STATE,
                        ChangeWidgetStateCommand.payload(editedId, editedWidget.defaultState));
            }
        }

        // The widget may be entered while the mouse was pressing or hovering it: editing starts from
        // the default state, never from a state the mouse left behind.
        if (widget != -1 && !widgetId.equals(WidgetEditingProxy.get().getEditedWidgetId())) {
            SandboxComponentRetriever.get(widget, WidgetComponent.class).currentState = null;
        }

        showFor(widget);
    }

    private void showFor(int widget) {
        WidgetComponent widgetComponent = widget == -1 ? null : SandboxComponentRetriever.get(widget, WidgetComponent.class);
        if (widgetComponent == null) {
            shownWidgetId = null;
            viewComponent.setStates(null, null, null);
            return;
        }

        shownWidgetId = EntityUtils.getEntityId(widget);
        viewComponent.setStates(widgetComponent.widgetType, widgetComponent.states, widgetComponent.getState());
    }
}
