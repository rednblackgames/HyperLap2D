package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

public class UpdatePolygonDataCommand extends EntityModifyRevertibleCommand {

    private boolean openPathBackup;
    private String entityId;

    @Override
    public void doAction() {
        Object[] payload = notification.getBody();
        if (entityId == null)
            entityId = EntityUtils.getEntityId((int) payload[0]);
        int entity = EntityUtils.getByUniqueId(entityId);
        boolean openPath = (boolean) payload[1];

        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);


        openPathBackup = polygonShapeComponent.openEnded;

        polygonShapeComponent.openEnded = openPath;

        checkPolygon(polygonShapeComponent);

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);

        polygonShapeComponent.openEnded = openPathBackup;

        checkPolygon(polygonShapeComponent);

        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    private void checkPolygon(PolygonShapeComponent polygonShapeComponent) {
        if (!polygonShapeComponent.openEnded) {
            Array<Vector2> points = polygonShapeComponent.vertices;
            if(PolygonUtils.isPolygonCCW(points.toArray())){
                points.reverse();
            }
            polygonShapeComponent.polygonizedVertices = PolygonUtils.polygonize(points.toArray());
        }
    }
}
