package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

public class CreatePlaceholderDialog extends H2DDialog {

    private static final String prefix = "games.rednblack.editor.view.ui.dialog.CreatePlaceholderDialog";
    public static final String ADD_NEW_PLACEHOLDER = prefix + ".ADD_NEW_PLACEHOLDER";

    private final VisValidatableTextField width, height, name;
    private final VisTextButton generateButton;

    private final Facade facade;

    private static final int MIN_WIDTH = 360;

    public CreatePlaceholderDialog() {
        super("Create Placeholder");
        addCloseButton();
        closeOnEscape();

        facade = Facade.getInstance();

        Validators.IntegerValidator validator = new Validators.IntegerValidator();
        name = StandardWidgetsFactory.createValidableTextField(new StringNameValidator());
        width = StandardWidgetsFactory.createValidableTextField(validator);
        height = StandardWidgetsFactory.createValidableTextField(validator);

        VisTable body = new VisTable();
        getContentTable().add(body).growX();
        PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();
        grid.section("Placeholder");
        grid.row("Name", name);
        grid.rowUnit("Width", width, "px");
        grid.rowUnit("Height", height, "px");

        generateButton = StandardWidgetsFactory.createTextButton("Generate", "accent");
        getButtonsTable().add(generateButton).pad(2);
        getCell(getButtonsTable()).right();

        setListeners();
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
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
