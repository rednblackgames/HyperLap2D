package games.rednblack.editor.system;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.additional.ButtonComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.systems.ButtonSystem;

/**
 * Button logic for the editor. Buttons react to the mouse in the sandbox like they do in a game, so
 * their states can be tried out on the spot, with one exception: the widget being edited keeps the
 * state chosen in the state bar, whatever the mouse does over it.
 *
 * Trying a state out never touches the scene data: value objects always describe the base look.
 *
 * The exception is lifted while the input is forwarded to the scene: there every button reacts.
 */
@All(ButtonComponent.class)
public class EditorButtonSystem extends ButtonSystem {

    @Override
    protected void updateWidgetState(int entity, WidgetComponent widget, ButtonComponent buttonComponent) {
        if (isBeingEdited(entity)) return;

        super.updateWidgetState(entity, widget, buttonComponent);
    }

    private boolean isBeingEdited(int entity) {
        return WidgetEditingProxy.isBeingEdited(entity, mainItemComponentMapper.get(entity));
    }
}
