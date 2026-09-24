package games.rednblack.editor.system;

import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.widget.ScrollPaneComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.systems.ScrollPaneSystem;

/**
 * Scroll pane logic for the editor. A pane scrolls in the sandbox like it would in a game, with
 * three exceptions: the widget being edited keeps the state chosen in the state bar, the scrollbars
 * never fade since what is being authored has to stay in sight, and a drag on the content moves the
 * item it started on instead of scrolling - the scrollbars are there to scroll with.
 *
 * None of the three holds while the input is forwarded to the scene: there the pane is the pane a
 * player would meet.
 */
@All({ScrollPaneComponent.class, WidgetComponent.class})
public class EditorScrollPaneSystem extends ScrollPaneSystem {

    protected ComponentMapper<MainItemComponent> mainItemComponentMapper;

    @Override
    protected void updateWidgetState(int entity, WidgetComponent widget, ScrollPaneComponent pane) {
        if (WidgetEditingProxy.isBeingEdited(entity, mainItemComponentMapper.get(entity))) return;

        super.updateWidgetState(entity, widget, pane);
    }

    @Override
    protected void fade(ScrollPaneComponent pane, WidgetComponent widget) {
        if (WidgetEditingProxy.isInputForwarded()) {
            super.fade(pane, widget);
            return;
        }
        pane.fadeAlpha = 1;
    }

    @Override
    protected boolean flickScrolls(WidgetComponent widget) {
        return WidgetEditingProxy.isInputForwarded() && super.flickScrolls(widget);
    }
}
