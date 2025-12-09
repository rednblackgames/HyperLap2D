package games.rednblack.editor.view.ui.settings;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.editor.renderer.systems.strategy.HyperLap2dInvocationStrategy;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;
import games.rednblack.h2d.common.view.ui.widget.TintButton;
import games.rednblack.h2d.common.vo.EditorConfigVO;
import games.rednblack.puremvc.Facade;

public class SandboxSettings extends SettingsNodeValue<EditorConfigVO> {

    private final VisCheckBox disableAmbientComposite, showBoundBoxes;
    private final TintButton tintButton;
    private VisSlider scrollVelocity, timeScale;

    public SandboxSettings() {
        super("Sandbox", Facade.getInstance());

        getContentTable().add("Behavior").left().row();
        getContentTable().addSeparator();
        disableAmbientComposite = StandardWidgetsFactory.createCheckBox("Disable Ambient light when viewing Composites");
        getContentTable().add(disableAmbientComposite).left().padTop(5).padLeft(8).row();

        getContentTable().add(getScrollVelocityTable()).left().padTop(10).row();

        getContentTable().add("Debug").left().padTop(10).row();
        getContentTable().addSeparator();
        showBoundBoxes = StandardWidgetsFactory.createCheckBox("Show bounding boxes outline");
        getContentTable().add(showBoundBoxes).left().padTop(5).padLeft(8).row();

        getContentTable().add(getTimeScaleTable()).left().padTop(10).row();

        getContentTable().add("Background").left().padTop(10).row();
        getContentTable().addSeparator();
        VisTable tintTable = new VisTable();
        tintTable.add("Color:").padRight(5).left();
        tintButton = StandardWidgetsFactory.createTintButton();
        tintTable.add(tintButton).left().padRight(5);

        tintButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                HyperLapColorPicker picker = new HyperLapColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void finished(Color newColor) {
                        tintButton.setColorValue(newColor);
                        getSettings().backgroundColor.set(newColor);
                    }

                    @Override
                    public void canceled(Color oldColor) {
                        tintButton.setColorValue(oldColor);
                        getSettings().backgroundColor.set(oldColor);
                    }

                    @Override
                    public void reset(Color previousColor, Color newColor) {
                        tintButton.setColorValue(previousColor);
                        getSettings().backgroundColor.set(previousColor);
                    }
                });

                picker.setColor(getSettings().backgroundColor);
                Sandbox.getInstance().getUIStage().addActor(picker.fadeIn());
            }
        });

        VisTextButton resetButton = StandardWidgetsFactory.createTextButton("Reset");
        resetButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getSettings().backgroundColor.set(0.15f, 0.15f, 0.15f, 1.0f);
                tintButton.setColorValue(getSettings().backgroundColor);
            }
        });
        tintTable.add(resetButton);

        getContentTable().add(tintTable).padLeft(8).left().row();
    }

    private Actor getScrollVelocityTable() {
        VisTable scaleTable = new VisTable();

        scaleTable.add("Scroll Velocity:").padLeft(8);
        scrollVelocity = StandardWidgetsFactory.createSlider(30, 400, 1);
        scaleTable.add(scrollVelocity).padLeft(8);
        VisLabel labelFactor = StandardWidgetsFactory.createLabel("", "default", Align.left);
        scaleTable.add(labelFactor).padLeft(8);
        labelFactor.setText(String.valueOf(getScrollVelocity()));
        scrollVelocity.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                labelFactor.setText(String.valueOf(getScrollVelocity()));
            }
        });

        return scaleTable;
    }

    private Actor getTimeScaleTable() {
        VisTable scaleTable = new VisTable();

        scaleTable.add("Time Scale:").padLeft(8);
        timeScale = StandardWidgetsFactory.createSlider(0.1f, 2f, 0.1f);
        scaleTable.add(timeScale).padLeft(8);
        VisLabel labelFactor = StandardWidgetsFactory.createLabel("", "default", Align.left);
        scaleTable.add(labelFactor).padLeft(8);
        labelFactor.setText(String.valueOf(getTimeScale()));
        timeScale.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                labelFactor.setText(String.valueOf(getTimeScale()));
                HyperLap2dInvocationStrategy.setTimeScale(getTimeScale());
            }
        });

        return scaleTable;
    }

    private float getScrollVelocity() {
        return RoundUtils.round(scrollVelocity.getValue(), 0);
    }

    private float getTimeScale() {
        return RoundUtils.round(timeScale.getValue(), 1);
    }

    @Override
    public void translateSettingsToView() {
        disableAmbientComposite.setChecked(getSettings().disableAmbientComposite);
        showBoundBoxes.setChecked(getSettings().showBoundingBoxes);
        tintButton.setColorValue(getSettings().backgroundColor);
        scrollVelocity.setValue(getSettings().scrollVelocity);
        timeScale.setValue(1);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().disableAmbientComposite = disableAmbientComposite.isChecked();
        getSettings().showBoundingBoxes = showBoundBoxes.isChecked();
        getSettings().scrollVelocity = getScrollVelocity();
        facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
    }

    @Override
    public boolean validateSettings() {
        return true;
    }

    @Override
    public boolean requireRestart() {
        return false;
    }
}
