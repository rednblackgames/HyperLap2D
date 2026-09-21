package games.rednblack.editor.view.ui.settings;
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.editor.renderer.systems.strategy.HyperLap2dInvocationStrategy;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;
import games.rednblack.h2d.common.view.ui.widget.TintButton;
import games.rednblack.h2d.common.vo.EditorConfigVO;
import games.rednblack.puremvc.Facade;

import java.util.function.Supplier;

public class SandboxSettings extends SettingsNodeValue<EditorConfigVO> {

    /** Three times the default swatch, and fixed: a settings page has room, but nothing to fill. */
    private static final int SWATCH_WIDTH = 29 * 3;
    private static final int SWATCH_HEIGHT = 21;

    private final VisCheckBox disableAmbientComposite, showBoundBoxes, clickableTypingLabels;
    private final TintButton tintButton;
    private final VisSlider scrollVelocity, timeScale;

    public SandboxSettings(Facade facade) {
        super("Sandbox", facade);

        disableAmbientComposite = StandardWidgetsFactory.createSwitch();
        showBoundBoxes = StandardWidgetsFactory.createSwitch();
        clickableTypingLabels = StandardWidgetsFactory.createSwitch();
        scrollVelocity = StandardWidgetsFactory.createSlider(30, 400, 1);
        timeScale = StandardWidgetsFactory.createSlider(0.1f, 2f, 0.1f);
        tintButton = StandardWidgetsFactory.createTintButton(SWATCH_WIDTH, SWATCH_HEIGHT);

        timeScale.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                HyperLap2dInvocationStrategy.setTimeScale(getTimeScale());
            }
        });

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
                PluginUIBridge.get().getSandbox().getUIStage().addActor(picker.fadeIn());
            }
        });

        VisTextButton resetButton = StandardWidgetsFactory.createTextButton("Reset");
        resetButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getSettings().backgroundColor.set(0.15f, 0.15f, 0.15f, 1.0f);
                tintButton.setColorValue(getSettings().backgroundColor);
            }
        });

        VisTable colorTable = new VisTable();
        colorTable.add(tintButton).left();
        colorTable.add(resetButton).height(PropertyGrid.FIELD_HEIGHT).padLeft(PropertyGrid.BUTTON_GAP);

        PropertyGrid grid = PropertyGrid.on(getContentTable()).dialogScale();

        grid.section("Behavior");
        grid.toggleWide("Disable ambient light when viewing composites", disableAmbientComposite);
        grid.toggleWide("Clickable links in typing labels", clickableTypingLabels);
        grid.sliderRow("Scroll velocity", scrollVelocity,
                sliderValue(grid, scrollVelocity, () -> String.valueOf(getScrollVelocity())));

        grid.section("Debug");
        grid.toggleWide("Show bounding boxes outline", showBoundBoxes);
        grid.sliderRow("Time scale", timeScale, sliderValue(grid, timeScale, () -> String.valueOf(getTimeScale())));

        grid.section("Background");
        grid.rowCompact("Color", colorTable);
    }

    /** The live value of a slider row, kept in sync while the slider is dragged. */
    private VisLabel sliderValue(PropertyGrid grid, VisSlider slider, Supplier<String> text) {
        VisLabel label = grid.valueLabel(text.get());
        slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                label.setText(text.get());
            }
        });
        return label;
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
        clickableTypingLabels.setChecked(getSettings().clickableTypingLabels);
        tintButton.setColorValue(getSettings().backgroundColor);
        scrollVelocity.setValue(getSettings().scrollVelocity);
        timeScale.setValue(1);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().disableAmbientComposite = disableAmbientComposite.isChecked();
        getSettings().showBoundingBoxes = showBoundBoxes.isChecked();
        getSettings().clickableTypingLabels = clickableTypingLabels.isChecked();
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
