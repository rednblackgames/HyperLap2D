package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.editor.controller.commands.component.UpdatePolygonDataCommand;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
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

        PolygonComponent polygonComponent = SandboxComponentRetriever.get(follower.getEntity(), PolygonComponent.class);
        if(polygonComponent == null || polygonComponent.vertices == null || polygonComponent.vertices.length == 0) return;
        if(follower.getOriginalPoints().size() <= 3) return;

        Vector2[][] polygonBackup = polygonComponent.vertices.clone();
        Object[] currentCommandPayload = UpdatePolygonDataCommand.payloadInitialState(follower.getEntity());

        follower.getOriginalPoints().remove(anchor);
        follower.getSelectedAnchorId(anchor-1);
        Vector2[] points = follower.getOriginalPoints().toArray(new Vector2[0]);
        polygonComponent.vertices = PolygonUtils.polygonize(points);

        if(polygonComponent.vertices == null) {
            // restore from backup
            polygonComponent.vertices = polygonBackup.clone();
            follower.update();
        }

        UpdatePolygonDataCommand.payload(currentCommandPayload, polygonComponent.vertices);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);

        follower.updateDraw();
    }
}
