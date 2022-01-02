package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.editor.controller.commands.component.UpdatePolygonVerticesCommand;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;

public class DeletePolygonVertexCommand extends SandboxCommand {

    @Override
    public void execute(INotification notification) {
        super.execute(notification);

        Dialogs.showConfirmDialog(sandbox.getUIStage(), "Delete Vertex",
                "Are you sure you want to delete this vertex?", new String[]{"No", "Yes"}, new Integer[]{0, 1}, r -> {
            if (r == 1) {
                callDoAction(notification);
            }
        }).padBottom(20).pack();
    }

    private void callDoAction(INotification notification) {
        Object[] payload = notification.getBody();
        PolygonFollower follower = (PolygonFollower) payload[0];
        int anchor = (int) payload[1];

        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(follower.getEntity(), PolygonShapeComponent.class);
        if(polygonShapeComponent == null || polygonShapeComponent.vertices == null || polygonShapeComponent.vertices.size == 0) return;
        if(polygonShapeComponent.vertices.size <= 3) return;

        Vector2[][] poligonyzedBackup = UpdatePolygonVerticesCommand.cloneData(polygonShapeComponent.polygonizedVertices);
        Array<Vector2> verticesBackup = UpdatePolygonVerticesCommand.cloneData(polygonShapeComponent.vertices);

        Object[] currentCommandPayload = UpdatePolygonVerticesCommand.payloadInitialState(follower.getEntity());

        polygonShapeComponent.vertices.removeIndex(anchor);
        follower.setSelectedAnchor(0);
        polygonShapeComponent.polygonizedVertices = PolygonUtils.polygonize(polygonShapeComponent.vertices.toArray());

        if(polygonShapeComponent.polygonizedVertices == null) {
            // restore from backup
            polygonShapeComponent.vertices = verticesBackup;
            polygonShapeComponent.polygonizedVertices = poligonyzedBackup;
            follower.update();
        }

        UpdatePolygonVerticesCommand.payload(currentCommandPayload, polygonShapeComponent.vertices, polygonShapeComponent.polygonizedVertices);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);

        follower.update();
    }
}
