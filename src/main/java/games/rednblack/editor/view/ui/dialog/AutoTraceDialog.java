package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisSlider;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.event.ButtonToNotificationListener;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class AutoTraceDialog extends H2DDialog {

    private static final String prefix = "games.rednblack.editor.view.ui.dialog.AutoTraceDialog";
    public static final String OPEN_DIALOG = prefix + ".OPEN_DIALOG";
    public static final String AUTO_TRACE_BUTTON_CLICKED = prefix + ".AUTO_TRACE_BUTTON_CLICKED";

    private final VisSlider hullSlider;
    private final VisLabel hullValue;

    private final VisSlider alphaSlider;
    private final VisLabel alphaValue;

    private final VisCheckBox multiPartDetection;
    private final VisCheckBox holeDetection;

    private final VisTextButton autoTraceButton, resetButton;

    public AutoTraceDialog() {
        super("Auto Trace");
        addCloseButton();
        hullSlider = StandardWidgetsFactory.createSlider(0.9f, 50f, 0.1f);
        hullValue = StandardWidgetsFactory.createLabel("2.5", "default", Align.center);

        alphaSlider = StandardWidgetsFactory.createSlider(0, 255, 1);
        alphaValue = StandardWidgetsFactory.createLabel("128", "default", Align.center);

        multiPartDetection = StandardWidgetsFactory.createCheckBox("Multi Part Detection");
        holeDetection = StandardWidgetsFactory.createCheckBox("Hole Detection");

        resetButton = StandardWidgetsFactory.createTextButton("Reset");
        autoTraceButton = StandardWidgetsFactory.createTextButton("Auto Trace");

        getContentTable().add("Hull Tolerance : ").left();
        getContentTable().add(hullSlider).left();
        getContentTable().add(hullValue).width(25);

        getContentTable().row().padTop(10);

        getContentTable().add("Alpha Tolerance : ").left();
        getContentTable().add(alphaSlider).left();
        getContentTable().add(alphaValue).width(25);

        getContentTable().row().padTop(10);

        getContentTable().add(multiPartDetection);
        getContentTable().add(holeDetection);

        getContentTable().row().padTop(10);

        getButtonsTable().add(resetButton).padRight(5);
        getButtonsTable().add(autoTraceButton);

        setListeners();
        reset();
    }

    public float getHullTolerance() {
        return hullSlider.getValue();
    }

    public int getAlphaTolerance() {
        return (int) alphaSlider.getValue();
    }

    public boolean isHoleDetection() {
        return holeDetection.isChecked();
    }

    public boolean isMultiPartDetection() {
        return multiPartDetection.isChecked();
    }

    private void setListeners() {
        hullSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hullValue.setText(RoundUtils.round(hullSlider.getValue(), 1) +"");
            }
        });

        alphaSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                alphaValue.setText((int)(alphaSlider.getValue())+"");
            }
        });

        autoTraceButton.addListener(new ButtonToNotificationListener(AUTO_TRACE_BUTTON_CLICKED));

        resetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                reset();
            }
        });
    }

    public void reset() {
        hullSlider.setValue(2.5f);
        alphaSlider.setValue(128);

        holeDetection.setChecked(false);
        multiPartDetection.setChecked(false);
    }
}
