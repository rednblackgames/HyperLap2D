package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.command.TransformData;
import games.rednblack.h2d.common.command.TransformPayload;
import games.rednblack.puremvc.Facade;

/**
 * Created by Osman on 01.08.2015.
 */
public class ItemTransformCommand extends EntityModifyRevertibleCommand {

    private TransformPayload payload;

    private String entityId;

    @Override
    public void doAction() {
        payload = getNotification().getBody();
        int entity = payload.entity();
        entityId = EntityUtils.getEntityId(entity);

        TransformData newData = payload.current();

        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

        if (newData.pos != null) { transformComponent.x = newData.pos.x; transformComponent.y = newData.pos.y; }
        if (newData.size != null) { dimensionsComponent.width = newData.size.x; dimensionsComponent.height = newData.size.y; }
        if (newData.scale != null) { transformComponent.scaleX = newData.scale.x; transformComponent.scaleY = newData.scale.y; }
        if (newData.rotation != null) transformComponent.rotation = newData.rotation;
        if (newData.origin != null) { transformComponent.originX = newData.origin.x; transformComponent.originY = newData.origin.y; }

        EntityUtils.refreshComponents(entity);

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        TransformData prevData = payload.prev();

        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

        transformComponent.x = prevData.pos.x;
        transformComponent.y = prevData.pos.y;
        dimensionsComponent.width = prevData.size.x;
        dimensionsComponent.height = prevData.size.y;
        if (dimensionsComponent.boundBox != null) {
            dimensionsComponent.boundBox.width = dimensionsComponent.width;
            dimensionsComponent.boundBox.height = dimensionsComponent.height;
        }
        transformComponent.scaleX = prevData.scale.x;
        transformComponent.scaleY = prevData.scale.y;
        transformComponent.rotation = prevData.rotation;
        transformComponent.originX = prevData.origin.x;
        transformComponent.originY = prevData.origin.y;

        EntityUtils.refreshComponents(entity);

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}