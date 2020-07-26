package games.rednblack.editor.controller.commands;

import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import games.rednblack.editor.view.ui.RulersUI;

public class ChangeRulerPositionCommand extends RevertibleCommand {

    private float previousPosition;

    @Override
    public void doAction() {
        previousPosition = RulersUI.getPreviousGuide().pos;
        String direction = RulersUI.getPreviousGuide().isVertical ? "X" : "Y";

        Dialogs.showInputDialog(sandbox.getUIStage(), "Manual " + direction + " Position", direction + " : ", false, new MyInputValidator(), new InputDialogListener() {
            @Override
            public void finished(String input) {
                RulersUI.updateGuideManually(Float.parseFloat(input));
            }

            @Override
            public void canceled() {

            }
        }).setText(String.valueOf(previousPosition), true);
    }

    @Override
    public void undoAction() {
        RulersUI.updateGuideManually(previousPosition);
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
