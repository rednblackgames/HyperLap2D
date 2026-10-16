package games.rednblack.editor.system;

import com.badlogic.gdx.utils.IntMap;
import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WindowComponent;
import games.rednblack.editor.renderer.ecs.annotations.All;
import games.rednblack.editor.renderer.ecs.utils.IntBag;
import games.rednblack.editor.renderer.systems.WindowSystem;

/**
 * Window logic for the editor. Outside preview mode a window is something being designed: it always
 * shows, never moves, closes or comes to the front, and its backdrop stays hidden so the scene can be
 * seen. The widget being edited keeps the state chosen in the state bar.
 *
 * In preview mode it works as in a game. Dragging it and bringing it to the front change its real
 * position and z-index, which are saved with the scene, so they are noted when preview mode starts
 * and put back when it ends, together with whether it was open.
 */
@All({WindowComponent.class, WidgetComponent.class})
public class EditorWindowSystem extends WindowSystem {


    private static class Snapshot {
        float x, y;
        int zIndex;
        boolean open;
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

    /** Notes where every window is, so trying the scene out leaves nothing behind. */
    private void remember() {
        snapshots.clear();
        IntBag windows = subscription.getEntities();
        for (int i = 0; i < windows.size(); i++) {
            int entity = windows.get(i);
            TransformComponent transform = transformCM.get(entity);
            ZIndexComponent zIndex = zIndexCM.get(entity);
            if (transform == null || zIndex == null) continue;

            Snapshot snapshot = new Snapshot();
            snapshot.x = transform.x;
            snapshot.y = transform.y;
            snapshot.zIndex = zIndex.getZIndex();
            snapshot.open = windowCM.get(entity).isOpen;
            snapshots.put(entity, snapshot);
        }
    }

    private void restore() {
        for (IntMap.Entry<Snapshot> entry : snapshots) {
            int entity = entry.key;
            WindowComponent window = windowCM.get(entity);
            TransformComponent transform = transformCM.get(entity);
            ZIndexComponent zIndex = zIndexCM.get(entity);
            if (window == null || transform == null || zIndex == null) continue;

            transform.x = entry.value.x;
            transform.y = entry.value.y;
            if (zIndex.getZIndex() != entry.value.zIndex) zIndex.setZIndex(entry.value.zIndex);
            window.isOpen = entry.value.open;
            //putting things back is no news for the listeners
            window.notified = false;
        }
        snapshots.clear();
    }

    @Override
    protected boolean interactive(int entity) {
        return WidgetEditingProxy.isInputForwarded();
    }

    @Override
    protected void updateWidgetState(int entity, WidgetComponent widget, WindowComponent window) {
        if (WidgetEditingProxy.isBeingEdited(entity, mainItemCM.get(entity))) return;

        super.updateWidgetState(entity, widget, window);
    }
}
