package games.rednblack.editor.proxy;

import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.utils.widget.WidgetStateRecorder;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Proxy;

/**
 * Keeps track of the widget whose state is being edited, if any, and drives the
 * {@link WidgetStateRecorder} for it.
 *
 * At most one widget shows a non default state at a time. Which one, and which state, only changes
 * through {@link games.rednblack.editor.controller.commands.ChangeWidgetStateCommand}, so it follows
 * undo and redo: a command is always reverted while the state it was made in is shown.
 */
public class WidgetEditingProxy extends Proxy {
    private static final String TAG = WidgetEditingProxy.class.getCanonicalName();
    public static final String NAME = TAG;

    private WidgetStateRecorder recorder;
    private Engine recorderEngine;

    /** uniqueId of the widget showing a non default state, null when every widget shows its default one */
    private String editedWidgetId = null;

    public WidgetEditingProxy() {
        super(NAME, null);
    }

    public static WidgetEditingProxy get() {
        return Facade.getInstance().retrieveProxy(NAME);
    }

    private WidgetStateRecorder recorder() {
        Engine engine = PluginUIBridge.get().getSandbox().getEngine();
        if (recorder == null || recorderEngine != engine) {
            recorder = new WidgetStateRecorder(engine);
            recorderEngine = engine;
        }
        return recorder;
    }

    /** To be called after anything that may have edited the scene: a command done, undone or redone. */
    public void recordEdits() {
        if (editedWidgetId == null) return;

        int widget = EntityUtils.getByUniqueId(editedWidgetId);
        // may be deleted for now, undo can still bring it back
        if (widget == -1) return;

        if (recorder().record(widget)) facade.sendNotification(MsgAPI.WIDGET_OVERRIDES_RECORDED, editedWidgetId);
    }

    public void switchState(String widgetId, String state) {
        applyStateSwitch(widgetId, state);

        // The parts now look the way the new state wants them. Panels showing one of them still
        // hold the values of the state left behind, and they write every field back together, so
        // an edit of any single field would push a stale position onto the entity.
        refreshSelectionPanels();
    }

    private void applyStateSwitch(String widgetId, String state) {
        if (editedWidgetId != null && !editedWidgetId.equals(widgetId)) {
            int previous = EntityUtils.getByUniqueId(editedWidgetId);
            if (previous != -1) {
                WidgetComponent previousWidget = SandboxComponentRetriever.get(previous, WidgetComponent.class);
                if (previousWidget != null) recorder().switchState(previous, previousWidget.defaultState);
            }
            String previousId = editedWidgetId;
            editedWidgetId = null;
            facade.sendNotification(MsgAPI.WIDGET_STATE_CHANGED, previousId);
        }

        int widget = EntityUtils.getByUniqueId(widgetId);
        if (widget == -1) return;
        WidgetComponent widgetComponent = SandboxComponentRetriever.get(widget, WidgetComponent.class);
        if (widgetComponent == null) return;

        recorder().switchState(widget, state);
        editedWidgetId = widgetComponent.getState().equals(widgetComponent.defaultState) ? null : widgetId;

        facade.sendNotification(MsgAPI.WIDGET_STATE_CHANGED, widgetId);
    }

    private void refreshSelectionPanels() {
        Sandbox sandbox = PluginUIBridge.get().getSandbox();
        if (sandbox == null || sandbox.getSelector() == null) return;

        for (int entity : sandbox.getSelector().getCurrentSelection()) {
            facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
        }
    }

    /** Forgets everything, the scene has been replaced. */
    public void reset() {
        editedWidgetId = null;
        if (recorder != null) recorder.reset();
    }

    public String getEditedWidgetId() {
        return editedWidgetId;
    }

    /**
     * Whether the widget's look belongs to the state bar rather than to the mouse: it is the one
     * being edited, either because the sandbox is showing it from the inside or because a state of
     * it was picked in the bar.
     *
     * @param mainItem the widget's own main item, holding the id the bar remembers
     */
    public static boolean isBeingEdited(int entity, MainItemComponent mainItem) {
        Sandbox sandbox = PluginUIBridge.get().getSandbox();
        if (sandbox == null) return true;

        // edited from the inside, however deep the view is
        if (findWidget(sandbox.getCurrentViewingEntity()) == entity) return true;

        // or still showing a state picked in the bar (undo can bring one back from outside the widget)
        String editedWidgetId = get().getEditedWidgetId();
        return editedWidgetId != null && mainItem != null && editedWidgetId.equals(mainItem.uniqueId);
    }

    /** @return the entity itself if it is a widget, else its nearest widget ancestor, -1 if none */
    public static int findWidget(int entity) {
        while (entity != -1) {
            if (SandboxComponentRetriever.get(entity, WidgetComponent.class) != null) return entity;
            ParentNodeComponent parentNode = SandboxComponentRetriever.get(entity, ParentNodeComponent.class);
            entity = parentNode == null ? -1 : parentNode.parentEntity;
        }
        return -1;
    }
}
