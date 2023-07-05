package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

public class CreatePlaceholderDialog extends H2DDialog {

    private static final String prefix = "games.rednblack.editor.view.ui.dialog.CreatePlaceholderDialog";
    public static final String ADD_NEW_PLACEHOLDER = prefix + ".ADD_NEW_PLACEHOLDER";

    private final VisValidatableTextField width, height, name;
    private final VisTextButton generateButton;

    private final Facade facade;

    public CreatePlaceholderDialog() {
        super("Create Placeholder");
        addCloseButton();

        facade = Facade.getInstance();

        name = StandardWidgetsFactory.createValidableTextField(new StringNameValidator());
        getContentTable().add("Name:");
        getContentTable().add(name).colspan(5).growX().row();

        Validators.IntegerValidator validator = new Validators.IntegerValidator();

        width = StandardWidgetsFactory.createValidableTextField(validator);
        height = StandardWidgetsFactory.createValidableTextField(validator);

        getContentTable().add("Width:").padRight(3);
        getContentTable().add(width).width(60);
        getContentTable().add("px").padRight(5);
        getContentTable().add("Height:").padRight(3);
        getContentTable().add(height).width(60);
        getContentTable().add("px");

        generateButton = StandardWidgetsFactory.createTextButton("Generate");
        getButtonsTable().add(generateButton);

        setListeners();
    }

    public int placeholderWidth() {
        return Integer.parseInt(width.getText());
    }

    public int placeholderHeight() {
        return Integer.parseInt(height.getText());
    }
    public String getName() {
        return name.getText();
    }

    private void setListeners() {
        generateButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (width.isInputValid() && height.isInputValid() && name.isInputValid()) {
                    facade.sendNotification(ADD_NEW_PLACEHOLDER);
                }
            }
        });
    }
}
