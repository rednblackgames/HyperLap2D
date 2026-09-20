package games.rednblack.editor.controller.commands;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;

/**
 * Makes a widget show another of its states in the editor.
 *
 * Nothing of the scene is modified, but it sits in the history on purpose: edits made while a state
 * is shown belong to that state, so they have to be undone and redone while it is shown too.
 */
public class ChangeWidgetStateCommand extends RevertibleCommand {

    private String widgetId;
    private String state;
    private String previousState;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        widgetId = (String) payload[0];
        state = (String) payload[1];

        int widget = EntityUtils.getByUniqueId(widgetId);
        WidgetComponent widgetComponent = widget == -1 ? null : SandboxComponentRetriever.get(widget, WidgetComponent.class);
        if (widgetComponent == null || !widgetComponent.hasState(state)) {
            cancel();
            return;
        }

        if (previousState == null) previousState = widgetComponent.getState();
        if (previousState.equals(state)) {
            cancel();
            return;
        }

        WidgetEditingProxy.get().switchState(widgetId, state);
    }

    @Override
    public void undoAction() {
        WidgetEditingProxy.get().switchState(widgetId, previousState);
    }

    public static Object[] payload(String widgetId, String state) {
        return new Object[]{widgetId, state};
    }
}
