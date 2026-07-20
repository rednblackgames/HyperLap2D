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
 * Revertible command for z-index up/down (item bring-forward / send-backward).
 * Previously {@code ItemControlMediator.itemZIndexChange} mutated {@code ZIndexComponent}
 * directly with no undo path; this command captures the previous z-index per entity and
 * restores it on undo.
 *
 * Payload: {@code Object[]{Set<Integer> selection, Boolean isUp}}
 */
public class SetZIndexCommand extends EntityModifyRevertibleCommand {

    private Array<String> entityIds;
    private IntArray prevZIndices;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Set<Integer> selection = (Set<Integer>) payload[0];
        boolean isUp = (Boolean) payload[1];

        entityIds = new Array<>();
        prevZIndices = new IntArray();

        for (Integer item : selection) {
            ZIndexComponent zIndexComponent = EntityDataProxy.get().get(item, ZIndexComponent.class);
            int amount = isUp ? 1 : -1;
            int setting = zIndexComponent.getZIndex() + amount;
            if (setting < 0) setting = 0;

            prevZIndices.add(zIndexComponent.getZIndex());
            entityIds.add(EntityUtils.getEntityId(item));
            zIndexComponent.setZIndex(setting);
        }

        facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, selection);
    }

    @Override
    public void undoAction() {
        Set<Integer> selection = new HashSet<>();
        for (int i = 0; i < entityIds.size; i++) {
            int entity = EntityUtils.getByUniqueId(entityIds.get(i));
            if (entity == -1) continue;
            ZIndexComponent zIndexComponent = EntityDataProxy.get().get(entity, ZIndexComponent.class);
            zIndexComponent.setZIndex(prevZIndices.get(i));
            selection.add(entity);
        }
        facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, selection);
    }
}