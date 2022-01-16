package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntSet;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.editor.controller.commands.component.UpdatePolygonVerticesCommand;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
import games.rednblack.editor.view.ui.validator.FloatInputValidator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.dialog.MultipleInputDialog;
import games.rednblack.h2d.common.view.ui.listener.MultipleInputDialogListener;
import org.puremvc.java.interfaces.INotification;

public class ChangePolygonVertexPositionCommand extends SandboxCommand {

    private Object[] currentCommandPayload;
    private final IntSet intersectionProblems = new IntSet();

    @Override
    public void execute(INotification notification) {
        super.execute(notification);

        Object[] payload = notification.getBody();
        PolygonFollower follower = (PolygonFollower) payload[0];
        int anchor = (int) payload[1];

        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(follower.getEntity(), PolygonShapeComponent.class);
        Array<Vector2> points = polygonShapeComponent.vertices;
        Vector2 backup = points.get(anchor).cpy();
        currentCommandPayload = UpdatePolygonVerticesCommand.payloadInitialState(follower.getEntity());

        MultipleInputDialog dialog = new MultipleInputDialog("Vertex Position", new String[]{"X : ", "Y : "}, false, new FloatInputValidator(), new MultipleInputDialogListener() {
            @Override
            public void finished(String[] input) {
                Array<Vector2> points = polygonShapeComponent.vertices;
                points.get(anchor).set(Float.parseFloat(input[0]), Float.parseFloat(input[1]));

                if (polygonShapeComponent.openEnded) {
                    UpdatePolygonVerticesCommand.payload(currentCommandPayload, polygonShapeComponent.vertices, polygonShapeComponent.polygonizedVertices);
                    HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);
                    return;
                }

                // check if any of near lines intersect
                Vector2[] pointsArray = points.toArray();
                IntSet intersections = PolygonUtils.checkForIntersection(anchor, points, intersectionProblems);
                if(intersections == null) {
                    if(PolygonUtils.isPolygonCCW(pointsArray)){
                        points.reverse();
                    }
                    polygonShapeComponent.polygonizedVertices = PolygonUtils.polygonize(polygonShapeComponent.vertices.toArray());

                    UpdatePolygonVerticesCommand.payload(currentCommandPayload, polygonShapeComponent.vertices, polygonShapeComponent.polygonizedVertices);
                    HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);
                } else {
                    points.get(anchor).set(backup);
                }
            }

            @Override
            public void canceled() {
            }
        });
        dialog.setText(new String[]{points.get(anchor).x+"", points.get(anchor).y+""});
        sandbox.getUIStage().addActor(dialog.fadeIn());
    }
}
