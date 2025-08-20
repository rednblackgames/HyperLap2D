package games.rednblack.editor.view.ui.validator;

import com.kotcrab.vis.ui.util.InputValidator;

public class FloatInputValidator implements InputValidator {
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