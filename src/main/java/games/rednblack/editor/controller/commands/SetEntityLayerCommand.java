package games.rednblack.editor.controller.commands;

import games.rednblack.editor.proxy.EntityDataProxy;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;

import java.util.HashSet;
import java.util.Set;

/**
 * Revertible command that moves a single entity onto a different layer (the layer name is local to
 * the entity's parent composite). The new layer's z-index slot is set high so the entity lands at
 * the front of the destination layer; {@code LayerSystem} re-linearizes z-indices on the next
 * frame (and via {@link MsgAPI#ACTION_Z_INDEX_CHANGED}). Used by the MCP {@code set_entity_layer}
 * tool via the RemoteOps bridge.
 *
 * Payload: {@code Object[]{Integer entity, String newLayerName}}
 */
public class SetEntityLayerCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    private String prevLayerName;
    private int prevZIndex;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (Integer) payload[0];
        String newLayerName = (String) payload[1];

        ZIndexComponent zIndexComponent = EntityDataProxy.get().get(entity, ZIndexComponent.class);
        entityId = EntityUtils.getEntityId(entity);
        prevLayerName = zIndexComponent.getLayerName();
        prevZIndex = zIndexComponent.getZIndex();

        zIndexComponent.setLayerName(newLayerName);
        // Place at the front of the destination layer; autoIndexing re-linearizes.
        zIndexComponent.setZIndex(Integer.MAX_VALUE);

        Set<Integer> selection = new HashSet<>();
        selection.add(entity);
        facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, selection);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        if (entity == -1) return;
        ZIndexComponent zIndexComponent = EntityDataProxy.get().get(entity, ZIndexComponent.class);
        zIndexComponent.setLayerName(prevLayerName);
        zIndexComponent.setZIndex(prevZIndex);

        Set<Integer> selection = new HashSet<>();
        selection.add(entity);
        facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, selection);
    }
}