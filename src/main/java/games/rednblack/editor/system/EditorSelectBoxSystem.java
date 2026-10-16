package games.rednblack.editor.system;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.widget.SelectBoxComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.systems.SelectBoxSystem;

/**
 * Select box logic for the editor. Outside preview mode a select box is something being designed:
 * it never opens, its row part shows where it was drawn and no copies are made, so nothing but what
 * was authored can ever be saved. The widget being edited keeps the state chosen in the state bar,
 * which is how its list is seen: on the open tab.
 *
 * In preview mode it works as in a game: the list opens and the rows are made from the items, and
 * they are gone again as soon as preview mode ends.
 */
@All({SelectBoxComponent.class, WidgetComponent.class})
public class EditorSelectBoxSystem extends SelectBoxSystem {

    protected ComponentMapper<MainItemComponent> mainItemComponentMapper;

    @Override
    protected boolean interactive(int entity) {
        return WidgetEditingProxy.isInputForwarded();
    }

    @Override
    protected boolean buildsRows(int entity) {
        return WidgetEditingProxy.isInputForwarded();
    }

    @Override
    protected void updateWidgetState(int entity, WidgetComponent widget, SelectBoxComponent selectBox) {
        if (WidgetEditingProxy.isBeingEdited(entity, mainItemComponentMapper.get(entity))) return;

        super.updateWidgetState(entity, widget, selectBox);
    }
}
