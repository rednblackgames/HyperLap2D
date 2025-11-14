package games.rednblack.editor.view.ui.validator;

import com.kotcrab.vis.ui.util.InputValidator;

public class GreaterThanIntegerValidator implements InputValidator {
    private int greaterThan;
    private boolean useEquals;

    public GreaterThanIntegerValidator (int greaterThan) {
        this.greaterThan = greaterThan;
    }

    /** @param inputCanBeEqual if true &gt;= comparison will be used, if false &gt; will be used. */
    public GreaterThanIntegerValidator (int greaterThan, boolean inputCanBeEqual) {
        this.greaterThan = greaterThan;
        this.useEquals = inputCanBeEqual;
    }

    @Override
    public boolean validateInput (String input) {
        try {
            int value = Integer.parseInt(input);
            return useEquals ? value >= greaterThan : value > greaterThan;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /*** @param useEquals if true &gt;= comparison will be used, if false &gt; will be used. */
    public void setUseEquals (boolean useEquals) {
        this.useEquals = useEquals;
    }

    public void setGreaterThan (int greaterThan) {
        this.greaterThan = greaterThan;
    }
}
