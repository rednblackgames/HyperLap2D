package games.rednblack.editor.system;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.widget.ProgressBarComponent;
import games.rednblack.editor.renderer.components.widget.SliderComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.systems.SliderSystem;

/**
 * Slider logic for the editor. A slider can be dragged in the sandbox like it would in a game, so
 * its look can be tried out on the spot, with one exception: the widget being edited keeps the
 * state chosen in the state bar, whatever the pointer does over it.
 *
 * The value is never scene data, so trying it out changes nothing that is saved.
 *
 * The exception is lifted while the input is forwarded to the scene: there every slider reacts.
 */
@All({SliderComponent.class, ProgressBarComponent.class, WidgetComponent.class})
public class EditorSliderSystem extends SliderSystem {

    protected ComponentMapper<MainItemComponent> mainItemComponentMapper;

    @Override
    protected void updateWidgetState(int entity, WidgetComponent widget, SliderComponent slider) {
        if (WidgetEditingProxy.isBeingEdited(entity, mainItemComponentMapper.get(entity))) return;

        super.updateWidgetState(entity, widget, slider);
    }
}
