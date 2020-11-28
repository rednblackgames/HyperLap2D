package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class CreateNoiseDialog extends H2DDialog  {
    private static final String prefix = "games.rednblack.editor.view.ui.dialog.CreateNoiseDialog";
    public static final String ADD_NEW_PLACEHOLDER = prefix + ".ADD_NEW_PLACEHOLDER";

    private final VisValidatableTextField width, height, name;
    private final VisTextButton generateButton;

    private final HyperLap2DFacade facade;

    private final VisSlider minSlider;
    private final VisLabel minValue;

    private final VisSlider maxSlider;
    private final VisLabel maxValue;

    public CreateNoiseDialog() {
        super("Create Perlin Noise");
        addCloseButton();

        facade = HyperLap2DFacade.getInstance();

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

        getContentTable().row().padTop(10);

        minSlider = StandardWidgetsFactory.createSlider(0, 255, 1);
        minValue = StandardWidgetsFactory.createLabel("0", "default", Align.center);

        maxSlider = StandardWidgetsFactory.createSlider(0, 255, 1);
        maxValue = StandardWidgetsFactory.createLabel("255", "default", Align.center);

        getContentTable().add("Min : ");
        getContentTable().add(minSlider).colspan(4);
        getContentTable().add(minValue).width(25).row();

        getContentTable().add("Max : ");
        getContentTable().add(maxSlider).colspan(4);
        getContentTable().add(maxValue).width(25);

        generateButton = StandardWidgetsFactory.createTextButton("Generate");
        getButtonsTable().add(generateButton);

        setListeners();

        maxSlider.setValue(255);
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
