package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

public class CenterOriginPointCommand extends EntityModifyRevertibleCommand {

    private String entityId;
    private final Vector2 backupOrigin = new Vector2();

    @Override
    public void doAction() {
        if (entityId == null) entityId = EntityUtils.getEntityId((int) notification.getBody());
        int entity = EntityUtils.getByUniqueId(entityId);

        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        backupOrigin.set(transformComponent.originX, transformComponent.originY);

        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

        float originX = dimensionsComponent.width * 0.5f;
        float originY = dimensionsComponent.height * 0.5f;
        if (dimensionsComponent.polygon != null) {
            originX = dimensionsComponent.polygon.getBoundingRectangle().width * 0.5f;
            originY = dimensionsComponent.polygon.getBoundingRectangle().height * 0.5f;
        }
        transformComponent.originX = originX;
        transformComponent.originY = originY;

        EntityUtils.refreshComponents(entity);
        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);

        transformComponent.originX = backupOrigin.x;
        transformComponent.originY = backupOrigin.y;

        EntityUtils.refreshComponents(entity);
        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
