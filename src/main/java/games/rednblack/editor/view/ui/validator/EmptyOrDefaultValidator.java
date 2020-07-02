package games.rednblack.editor.view.ui.validator;

import com.kotcrab.vis.ui.util.form.FormInputValidator;

public class EmptyOrDefaultValidator extends FormInputValidator {

    public EmptyOrDefaultValidator() {
        super("Cannot be empty or 'Default'");
    }

    @Override
    public boolean validate (String input) {
        return !input.isEmpty() && !input.equals("Default");
    }
}
