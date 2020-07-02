package games.rednblack.editor.view.ui.validator;

import com.kotcrab.vis.ui.util.form.FormInputValidator;
import org.apache.commons.lang3.StringUtils;

public class StringNameValidator extends FormInputValidator {

    public StringNameValidator() {
        super("Please input a valid name");
    }

    @Override
    protected boolean validate(String input) {
        return !StringUtils.isEmpty(input) && !StringUtils.endsWith(input, " ") && !StringUtils.startsWith(input, " ");
    }
}
