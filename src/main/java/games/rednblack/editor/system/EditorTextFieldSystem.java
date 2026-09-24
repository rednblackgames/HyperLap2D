package games.rednblack.editor.system;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.widget.TextFieldComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.systems.TextFieldSystem;

/**
 * Text field logic for the editor. A field reacts in the sandbox like it would in a game, with one
 * exception: the widget being edited keeps the state chosen in the state bar, whatever the pointer
 * does over it.
 *
 * The exception is lifted while the input is forwarded to the scene, which is also the only time
 * the keyboard reaches a field: outside of it the editor keeps its keys.
 */
@All({TextFieldComponent.class, WidgetComponent.class})
public class EditorTextFieldSystem extends TextFieldSystem {

    protected ComponentMapper<MainItemComponent> mainItemComponentMapper;

    @Override
    protected void updateWidgetState(int entity, WidgetComponent widget, TextFieldComponent field) {
        if (WidgetEditingProxy.isBeingEdited(entity, mainItemComponentMapper.get(entity))) return;

        super.updateWidgetState(entity, widget, field);
    }
}
