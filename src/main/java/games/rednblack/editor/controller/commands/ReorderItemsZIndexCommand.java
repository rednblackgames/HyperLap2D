package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import games.rednblack.editor.proxy.EntityDataProxy;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;

import java.util.HashSet;
import java.util.Set;

/**
 * Revertible command that reorders the z-index of a group of same-layer siblings (used by the
 * Items Tree drag &amp; drop). Because z-index is confined per layer, the siblings occupy a contiguous
 * z block {@code [lo..hi]}; this command reassigns those same values in the requested order and lets
 * {@code LayerSystem} sort/normalize — so the whole group stays inside its layer.
 *
 * Payload: {@code Array<String>} entity ids in the desired z-ascending order.
 */
public class ReorderItemsZIndexCommand extends EntityModifyRevertibleCommand {

    private Array<String> entityIds;
    private IntArray prevZIndices;

    @Override
    public void doAction() {
        Array<String> ordered = getNotification().getBody();

        // Base of the layer's z block: the lowest current z-index among the group.
        int lo = Integer.MAX_VALUE;
        for (String id : ordered) {
            int entity = EntityUtils.getByUniqueId(id);
            if (entity == -1) continue;
            lo = Math.min(lo, EntityDataProxy.get().get(entity, ZIndexComponent.class).getZIndex());
        }
        if (lo == Integer.MAX_VALUE) return;

        entityIds = new Array<>();
        prevZIndices = new IntArray();
        Set<Integer> changed = new HashSet<>();

        int k = 0;
        for (String id : ordered) {
            int entity = EntityUtils.getByUniqueId(id);
            if (entity != -1) {
                ZIndexComponent zIndexComponent = EntityDataProxy.get().get(entity, ZIndexComponent.class);
                entityIds.add(id);
                prevZIndices.add(zIndexComponent.getZIndex());
                zIndexComponent.setZIndex(lo + k);
                changed.add(entity);
            }
            k++;
        }

        facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, changed);
    }

    @Override
    public void undoAction() {
        Set<Integer> changed = new HashSet<>();
        for (int i = 0; i < entityIds.size; i++) {
            int entity = EntityUtils.getByUniqueId(entityIds.get(i));
            if (entity == -1) continue;
            EntityDataProxy.get().get(entity, ZIndexComponent.class).setZIndex(prevZIndices.get(i));
            changed.add(entity);
        }
        facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, changed);
    }
}
