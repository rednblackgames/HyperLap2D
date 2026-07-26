package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

public class CreateNoiseDialog extends H2DDialog  {
    private static final String prefix = "games.rednblack.editor.view.ui.dialog.CreateNoiseDialog";
    public static final String ADD_NEW_PLACEHOLDER = prefix + ".ADD_NEW_PLACEHOLDER";

    private final VisValidatableTextField width, height, name;
    private final VisTextButton generateButton;

    private final Facade facade;

    private final VisSlider minSlider;
    private final VisLabel minValue;

    private final VisSlider maxSlider;
    private final VisLabel maxValue;

    private static final int MIN_WIDTH = 420;
    /** What the two sliders do: the noise is generated between these grey levels. */
    private static final String RANGE_TOOLTIP =
            "The darkest and brightest grey the noise is generated between, from 0 to 255.";

    public CreateNoiseDialog() {
        super("Create Perlin Noise");
        addCloseButton();
        closeOnEscape();

        facade = Facade.getInstance();

        Validators.IntegerValidator validator = new Validators.IntegerValidator();
        name = StandardWidgetsFactory.createValidableTextField(new StringNameValidator());
        width = StandardWidgetsFactory.createValidableTextField(validator);
        height = StandardWidgetsFactory.createValidableTextField(validator);

        minSlider = StandardWidgetsFactory.createSlider(0, 255, 1);
        maxSlider = StandardWidgetsFactory.createSlider(0, 255, 1);

        VisTable body = new VisTable();
        getContentTable().add(body).growX();
        PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();

        minValue = grid.valueLabel("0");
        maxValue = grid.valueLabel("255");

        grid.section("Image");
        grid.row("Name", name);
        grid.rowUnit("Width", width, "px");
        grid.rowUnit("Height", height, "px");

        grid.section("Grey range");
        grid.sliderRow("Min", minSlider, minValue);
        grid.tooltipLastRow(RANGE_TOOLTIP);
        grid.sliderRow("Max", maxSlider, maxValue);
        grid.tooltipLastRow(RANGE_TOOLTIP);

        generateButton = StandardWidgetsFactory.createTextButton("Generate", "accent");
        getButtonsTable().add(generateButton).pad(2);
        getCell(getButtonsTable()).right();

        setListeners();

        maxSlider.setValue(255);
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

    public int getMin() {
        return (int) minSlider.getValue();
    }

    public int getMax() {
        return (int) maxSlider.getValue();
    }

    private void setListeners() {
        maxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                maxValue.setText((int)(maxSlider.getValue())+"");
            }
        });

        minSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                minValue.setText((int)(minSlider.getValue())+"");
            }
        });

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
