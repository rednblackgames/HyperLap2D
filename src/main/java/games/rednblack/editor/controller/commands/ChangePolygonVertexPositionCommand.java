package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import com.kotcrab.vis.ui.util.InputValidator;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.editor.controller.commands.component.UpdatePolygonDataCommand;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.poly.PolygonUtils;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.dialog.MultipleInputDialog;
import games.rednblack.h2d.common.view.ui.listener.MultipleInputDialogListener;
import org.puremvc.java.interfaces.INotification;

import java.util.Collections;

public class ChangePolygonVertexPositionCommand extends SandboxCommand {

    private Object[] currentCommandPayload;

    @Override
    public void execute(INotification notification) {
        super.execute(notification);

        Object[] payload = notification.getBody();
        PolygonFollower follower = (PolygonFollower) payload[0];
        int anchor = (int) payload[1];

        Vector2[] points = follower.getOriginalPoints().toArray(new Vector2[0]);
        Vector2 backup = points[anchor].cpy();
        currentCommandPayload = UpdatePolygonDataCommand.payloadInitialState(follower.getEntity());

        MultipleInputDialog dialog = new MultipleInputDialog("Vertex Position", new String[]{"X : ", "Y : "}, false, new MyInputValidator(), new MultipleInputDialogListener() {
            @Override
            public void finished(String[] input) {
                Vector2[] points = follower.getOriginalPoints().toArray(new Vector2[0]);
                PolygonComponent polygonComponent = ComponentRetriever.get(follower.getEntity(), PolygonComponent.class);
                points[anchor].set(Float.parseFloat(input[0]), Float.parseFloat(input[1]));

                // check if any of near lines intersect
                int[] intersections = PolygonUtils.checkForIntersection(anchor, points);
                if(intersections == null) {
                    if(PolygonUtils.isPolygonCCW(points)){
                        Collections.reverse(follower.getOriginalPoints());
                        points = follower.getOriginalPoints().toArray(new Vector2[0]);
                    }
                    polygonComponent.vertices = PolygonUtils.polygonize(points);

                    currentCommandPayload = UpdatePolygonDataCommand.payload(currentCommandPayload, polygonComponent.vertices);
                    HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_MESH_DATA, currentCommandPayload);
                } else {
                    points[anchor].set(backup);
                }
            }

            @Override
            public void canceled() {

            }
        });
        dialog.setText(new String[]{points[anchor].x+"", points[anchor].y+""});
        sandbox.getUIStage().addActor(dialog.fadeIn());
    }

    private static class MyInputValidator implements InputValidator {
        @Override
        public boolean validateInput(String input) {
            try {
                Float.parseFloat(input);
                return true;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
    }
}
