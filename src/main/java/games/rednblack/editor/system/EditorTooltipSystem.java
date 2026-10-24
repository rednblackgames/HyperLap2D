package games.rednblack.editor.system;

import com.badlogic.gdx.utils.IntMap;
import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.widget.TooltipComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.ecs.utils.IntBag;
import games.rednblack.editor.renderer.systems.TooltipSystem;
import games.rednblack.editor.renderer.widget.WidgetTypes;

/**
 * Tooltip logic for the editor. Outside preview mode a tooltip is something being designed: it
 * always shows where it was drawn, its pointer too, and never opens, places itself or closes. The
 * widget being edited keeps the state chosen in the state bar.
 *
 * In preview mode it works as in a game. Placing itself changes its real position and its pointer's,
 * which are saved with the scene, so they are noted when preview mode starts and put back when it ends,
 * together with whether it was open.
 */
@All({TooltipComponent.class, WidgetComponent.class})
public class EditorTooltipSystem extends TooltipSystem {

    private static class Snapshot {
        float x, y;
        int pointer = -1;
        float pointerX, pointerY, pointerScaleY;
        boolean open;
        int target;
    }

    private final IntMap<Snapshot> snapshots = new IntMap<>();
    private boolean previewing = false;

    @Override
    protected void begin() {
        boolean forwarded = WidgetEditingProxy.isInputForwarded();
        if (forwarded == previewing) return;
        previewing = forwarded;

        if (forwarded) remember();
        else restore();
    }

    /** Notes where every tooltip and its pointer are, so trying the scene out leaves nothing behind. */
    private void remember() {
        snapshots.clear();
        IntBag tooltips = subscription.getEntities();
        for (int i = 0; i < tooltips.size(); i++) {
            int entity = tooltips.get(i);
            TransformComponent transform = transformCM.get(entity);
            if (transform == null) continue;

            Snapshot snapshot = new Snapshot();
            snapshot.x = transform.x;
            snapshot.y = transform.y;
            TooltipComponent tooltip = tooltipCM.get(entity);
            snapshot.open = tooltip.isOpen;
            snapshot.target = tooltip.target;
            int pointer = findPart(entity, WidgetTypes.ROLE_POINTER);
            TransformComponent pointerTransform = pointer == -1 ? null : transformCM.get(pointer);
            if (pointerTransform != null) {
                snapshot.pointer = pointer;
                snapshot.pointerX = pointerTransform.x;
                snapshot.pointerY = pointerTransform.y;
                snapshot.pointerScaleY = pointerTransform.scaleY;
            }
            snapshots.put(entity, snapshot);
        }
    }

    private void restore() {
        for (IntMap.Entry<Snapshot> entry : snapshots) {
            int entity = entry.key;
            TooltipComponent tooltip = tooltipCM.get(entity);
            TransformComponent transform = transformCM.get(entity);
            if (tooltip == null || transform == null) continue;

            transform.x = entry.value.x;
            transform.y = entry.value.y;
            TransformComponent pointer = entry.value.pointer == -1 ? null : transformCM.get(entry.value.pointer);
            if (pointer != null) {
                pointer.x = entry.value.pointerX;
                pointer.y = entry.value.pointerY;
                pointer.scaleY = entry.value.pointerScaleY;
            }
            tooltip.isOpen = entry.value.open;
            tooltip.target = entry.value.target;
            //putting things back is no news for the listeners
            tooltip.notified = false;
        }
        snapshots.clear();
    }

    @Override
    protected boolean interactive(int entity) {
        return WidgetEditingProxy.isInputForwarded();
    }

    @Override
    protected void updateWidgetState(int entity, WidgetComponent widget, TooltipComponent tooltip) {
        if (WidgetEditingProxy.isBeingEdited(entity, mainItemCM.get(entity))) return;

        super.updateWidgetState(entity, widget, tooltip);
    }
}
