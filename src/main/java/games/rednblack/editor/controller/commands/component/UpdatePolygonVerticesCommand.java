package games.rednblack.editor.controller.commands.component;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdatePolygonVerticesCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private Vector2[][] polygonizedDataFrom;
    private Vector2[][] polygonizedDataTo;
    private Array<Vector2> dataFrom;
    private Array<Vector2> dataTo;

    private void collectData() {
        Object[] payload = getNotification().getBody();
        entityId = EntityUtils.getEntityId((int) payload[0]);
        polygonizedDataFrom = (Vector2[][]) payload[1];
        dataFrom = (Array<Vector2>) payload[2];
        polygonizedDataTo = (Vector2[][]) payload[3];
        dataTo = (Array<Vector2>) payload[4];

        polygonizedDataFrom = cloneData(polygonizedDataFrom);
        dataFrom = cloneData(dataFrom);
        polygonizedDataTo = cloneData(polygonizedDataTo);
        dataTo = cloneData(dataTo);
    }

    @Override
    public void doAction() {
        collectData();

        int entity = EntityUtils.getByUniqueId(entityId);

        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);
        polygonShapeComponent.vertices = dataTo;
        polygonShapeComponent.polygonizedVertices = polygonizedDataTo;

        EntityUtils.refreshComponents(entity);

        //Force TextureRegionComponent refresh to immediately update follower size and position
        TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);
        if (textureRegionComponent != null)
            textureRegionComponent.executeRefresh(entity);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);

    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);
        polygonShapeComponent.vertices = dataFrom;
        polygonShapeComponent.polygonizedVertices = polygonizedDataFrom;

        EntityUtils.refreshComponents(entity);

        //Force TextureRegionComponent refresh to immediately update follower size and position
        TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);
        if (textureRegionComponent != null)
            textureRegionComponent.executeRefresh(entity);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object[] payloadInitialState(int entity) {
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);
        Object[] payload = new Object[5];
        payload[0] = entity;
        payload[1] = cloneData(polygonShapeComponent.polygonizedVertices);
        payload[2] = cloneData(polygonShapeComponent.vertices);
        return payload;
    }

    public static Object[] payload(Object[] payload, Array<Vector2> vertices, Vector2[][] polygonizedVertices) {
        payload[3] = cloneData(polygonizedVertices);
        payload[4] = cloneData(vertices);
        return payload;
    }

    public static Array<Vector2> cloneData(Array<Vector2> data) {
        Array<Vector2> clone = new Array<>(true, data.size, Vector2.class);
        for (Vector2 vector2 : data) {
            clone.add(vector2.cpy());
        }
        return clone;
    }
    public static Vector2[][] cloneData(Vector2[][] data) {
        Vector2[][] newData = new Vector2[data.length][];
        for(int i = 0; i < data.length; i++) {
            newData[i] = new Vector2[data[i].length];
            for(int j = 0; j < data[i].length; j++) {
                newData[i][j] = data[i][j].cpy();
            }
        }

        return newData;
    }
}
