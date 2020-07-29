package games.rednblack.editor.controller.commands.component;

import com.kotcrab.vis.ui.util.InputValidator;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
import games.rednblack.h2d.common.view.ui.dialog.MultipleInputDialog;
import games.rednblack.h2d.common.view.ui.listener.MultipleInputDialogListener;

public class UpdatePolygonVertexPositionCommand extends EntityModifyRevertibleCommand {
    @Override
    public void doAction() {
        Object[] payload = notification.getBody();
        PolygonFollower follower = (PolygonFollower) payload[0];
        int anchorId = (int) payload[1];

        MultipleInputDialog dialog = new MultipleInputDialog("Vertex Position", new String[]{"X : ", "Y : "}, false, new MyInputValidator(), new MultipleInputDialogListener() {
            @Override
            public void finished(String[] input) {

            }

            @Override
            public void canceled() {

            }
        });
        sandbox.getUIStage().addActor(dialog.fadeIn());
    }

    @Override
    public void undoAction() {

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
