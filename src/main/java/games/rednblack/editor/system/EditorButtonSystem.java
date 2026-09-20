package games.rednblack.editor.system;

import games.rednblack.editor.proxy.PluginUIBridge;
import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.additional.ButtonComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.systems.ButtonSystem;
import games.rednblack.editor.view.stage.Sandbox;

/**
 * Button logic for the editor. Buttons react to the mouse in the sandbox like they do in a game, so
 * their states can be tried out on the spot, with one exception: the widget being edited keeps the
 * state chosen in the state bar, whatever the mouse does over it.
 *
 * Trying a state out never touches the scene data: value objects always describe the base look.
 */
@All(ButtonComponent.class)
public class EditorButtonSystem extends ButtonSystem {

    @Override
    protected void updateWidgetState(int entity, WidgetComponent widget, ButtonComponent buttonComponent) {
        if (isBeingEdited(entity)) return;

        super.updateWidgetState(entity, widget, buttonComponent);
    }

    private boolean isBeingEdited(int entity) {
        Sandbox sandbox = PluginUIBridge.get().getSandbox();
        if (sandbox == null) return true;

        // edited from the inside, however deep the view is
        if (WidgetEditingProxy.findWidget(sandbox.getCurrentViewingEntity()) == entity) return true;

        // or still showing a state picked in the bar (undo can bring one back from outside the widget)
        String editedWidgetId = WidgetEditingProxy.get().getEditedWidgetId();
        if (editedWidgetId == null) return false;
        MainItemComponent mainItem = mainItemComponentMapper.get(entity);
        return mainItem != null && editedWidgetId.equals(mainItem.uniqueId);
    }
}
