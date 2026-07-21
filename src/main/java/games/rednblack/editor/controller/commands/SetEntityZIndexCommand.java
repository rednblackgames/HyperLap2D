package games.rednblack.editor.controller.commands;

import games.rednblack.editor.proxy.EntityDataProxy;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;

import java.util.HashSet;
import java.util.Set;

/**
 * Revertible command that sets a single entity's z-index to an absolute integer (local to its
 * layer; the runtime auto-adjusts z-indices into a linear progression). Used by the MCP
 * {@code set_z_index} tool via the RemoteOps bridge.
 *
 * Payload: {@code Object[]{Integer entity, Integer zIndex}}
 */
public class SetEntityZIndexCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    private int prevZIndex;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (Integer) payload[0];
        int target = (Integer) payload[1];

        ZIndexComponent zIndexComponent = EntityDataProxy.get().get(entity, ZIndexComponent.class);
        entityId = EntityUtils.getEntityId(entity);
        prevZIndex = zIndexComponent.getZIndex();
        zIndexComponent.setZIndex(target);

        Set<Integer> selection = new HashSet<>();
        selection.add(entity);
        facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, selection);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        if (entity == -1) return;
        ZIndexComponent zIndexComponent = EntityDataProxy.get().get(entity, ZIndexComponent.class);
        zIndexComponent.setZIndex(prevZIndex);

        Set<Integer> selection = new HashSet<>();
        selection.add(entity);
        facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, selection);
    }
}