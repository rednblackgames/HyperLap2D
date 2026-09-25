package games.rednblack.editor.view.ui.settings;
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.SettingsNodeValue;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;
import games.rednblack.h2d.common.view.ui.widget.TintButton;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.puremvc.Facade;

public class LivePreviewSettings extends SettingsNodeValue<ProjectVO> {

    /** Three times the default swatch, and fixed: a settings page has room, but nothing to fill. */
    private static final int SWATCH_WIDTH = 29 * 3;
    private static final int SWATCH_HEIGHT = 21;

    private final TintButton tintButton;
    private final VisCheckBox box2dDebug;

    public LivePreviewSettings(Facade facade) {
        super("Live Preview", facade);

        box2dDebug = StandardWidgetsFactory.createSwitch();
        tintButton = StandardWidgetsFactory.createTintButton(SWATCH_WIDTH, SWATCH_HEIGHT);

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
                getSettings().backgroundColor.set(0, 0, 0, 1.0f);
                tintButton.setColorValue(getSettings().backgroundColor);
            }
        });

        VisTable colorTable = new VisTable();
        colorTable.add(tintButton).left();
        colorTable.add(resetButton).height(PropertyGrid.FIELD_HEIGHT).padLeft(PropertyGrid.BUTTON_GAP);

        PropertyGrid grid = PropertyGrid.on(getContentTable()).dialogScale().sectionPad(SECTION_PAD_TOP, SECTION_PAD_BOTTOM);

        grid.section("Render");
        grid.toggleWide("Box2D debug render", box2dDebug);

        grid.section("Background");
        grid.rowCompact("Color", colorTable);
    }

    @Override
    public void translateSettingsToView() {
        tintButton.setColorValue(getSettings().backgroundColor);
        box2dDebug.setChecked(getSettings().box2dDebugRender);
    }

    @Override
    public void translateViewToSettings() {
        getSettings().box2dDebugRender = box2dDebug.isChecked();
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
