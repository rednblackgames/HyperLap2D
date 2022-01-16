package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdatePolygonDataCommand extends EntityModifyRevertibleCommand {

    private boolean openPathBackup;
    private int entityId;

    @Override
    public void doAction() {
        Object[] payload = notification.getBody();
        int entity = (int) payload[0];
        boolean openPath = (boolean) payload[1];

        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);

        entityId = EntityUtils.getEntityId(entity);
        openPathBackup = polygonShapeComponent.openEnded;

        polygonShapeComponent.openEnded = openPath;

        checkPolygon(polygonShapeComponent);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);

        polygonShapeComponent.openEnded = openPathBackup;

        checkPolygon(polygonShapeComponent);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
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
