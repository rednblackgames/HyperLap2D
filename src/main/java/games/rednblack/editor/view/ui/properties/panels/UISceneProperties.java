package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.event.*;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIAbstractProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.TintButton;

import java.util.HashMap;

public class UISceneProperties extends UIAbstractProperties implements RemoteEditablePanel {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UISceneProperties";
    public static final String AMBIENT_COLOR_BUTTON_CLICKED = prefix + ".AMBIENT_COLOR_BUTTON_CLICKED";
    public static final String DIRECTIONAL_COLOR_BUTTON_CLICKED = prefix + ".DIRECTIONAL_COLOR_BUTTON_CLICKED";

    public static final String EDIT_SHADER_BUTTON_CLICKED = prefix + ".EDIT_SHADER_BUTTON_CLICKED";
    public static final String EDIT_SHADER_DONE = prefix + ".EDIT_SHADER_DONE";
    public static final String UNIFORMS_SHADER_BUTTON_CLICKED = prefix + ".UNIFORMS_SHADER_BUTTON_CLICKED";

    final private VisLabel pixelsPerWorldUnitField;
    final private VisLabel worldSizeField;
    private PropertyGrid.CollapsibleRow directionalRow;
    final private VisCheckBox physicsEnabledCheckBox;
    final private VisTextField gravityXTextField;
    final private VisTextField gravityYTextField;
    final private VisTextField sleepVelocityTextField;
    final private VisTextField blurNumTextField;
    final private VisTextField lightMapScaleTextField;
    final private VisCheckBox enableLightsCheckBox;
    final private VisCheckBox enablePseudo3DLightsCheckBox;
    final private TintButton ambientColorComponent;
    final private VisSelectBox<String> lightTypeBox;
    final private Spinner directionalRays;
    final private VisTextField directionalDegreeTextField;
    final private VisTextField directionalHeightTextField;
    final private TintButton directionalLightColor;
    private final VisSelectBox<String> shadersSelector;

    VisTable directionalTable = new VisTable();

    public UISceneProperties() {
        Validators.FloatValidator floatValidator = new Validators.FloatValidator();
        Validators.IntegerValidator integerValidator  = new Validators.IntegerValidator();

        pixelsPerWorldUnitField = PropertyGrid.value("1");
        worldSizeField = PropertyGrid.value("0 x 0");
        physicsEnabledCheckBox = StandardWidgetsFactory.createSwitch();
        gravityXTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        gravityYTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        sleepVelocityTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        blurNumTextField = StandardWidgetsFactory.createValidableTextField(integerValidator);
        lightMapScaleTextField = StandardWidgetsFactory.createValidableTextField(integerValidator);
        enableLightsCheckBox = StandardWidgetsFactory.createSwitch();
        enablePseudo3DLightsCheckBox = StandardWidgetsFactory.createSwitch();
        ambientColorComponent = StandardWidgetsFactory.createTintButton();
        lightTypeBox = StandardWidgetsFactory.createSelectBox(String.class);
        directionalRays = StandardWidgetsFactory.createNumberSelector(4, 4, 5000);
        directionalDegreeTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        directionalHeightTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        directionalLightColor = StandardWidgetsFactory.createTintButton();

        shadersSelector = StandardWidgetsFactory.createSelectBox(String.class);

        lightTypeBox.setItems("DIFFUSE", "DIRECTIONAL", "BRIGHT");

        PropertyGrid grid = PropertyGrid.on(this).padPanel();

        grid.section("Scene");
        grid.rowCompact("Pixels per WU", pixelsPerWorldUnitField);
        grid.rowCompact("World size", worldSizeField);
        grid.row("Shader", shadersSelector);

        grid.section("Physics", physicsEnabledCheckBox);
        grid.pair("Gravity", "X", gravityXTextField, "Y", gravityYTextField);
        grid.row("Sleep velocity", sleepVelocityTextField);

        grid.section("Lights", enableLightsCheckBox);
        grid.row("Ambient color", ambientColorComponent);
        grid.row("Light type", lightTypeBox);
        grid.row("Shadows blur", blurNumTextField);
        grid.row("Scale quality", lightMapScaleTextField);
        grid.toggle("Pseudo 3D", enablePseudo3DLightsCheckBox);

        PropertyGrid directional = PropertyGrid.on(directionalTable, grid);
        directional.section("Directional light");
        directional.row("Color", directionalLightColor);
        directional.rowCompact("Rays", directionalRays);
        directional.row("Degree", directionalDegreeTextField);
        directional.row("Height", directionalHeightTextField);

        // Shown by updateDisabled() only while the light type is DIRECTIONAL.
        directionalRow = grid.collapsibleSubGrid(directionalTable);
        directionalRow.setContentVisible(false);

        setListeners();
    }

    public void setDirectionalDegree(String degree) {
        this.directionalDegreeTextField.setText(degree);
    }
    public String getDirectionalDegree() {
        return directionalDegreeTextField.getText();
    }

    public void setDirectionalHeight(String degree) {
        this.directionalHeightTextField.setText(degree);
    }
    public String getDirectionalHeight() {
        return directionalHeightTextField.getText();
    }

    public String getDirectionalRays() {
        return directionalRays.getTextField().getText();
    }
    public void setDirectionalRays(String rays) {
        this.directionalRays.getTextField().setText(rays);
    }

    public void setLightType(String type) {
        lightTypeBox.setSelected(type);
        updateDisabled();
    }

    public void updateDisabled() {
        boolean isDirectional = lightTypeBox.getSelected().equals("DIRECTIONAL");
        directionalRays.setDisabled(!isDirectional);
        directionalDegreeTextField.setDisabled(!isDirectional);
        directionalRow.setContentVisible(isDirectional);
    }

    public String getLightType() {
        return lightTypeBox.getSelected();
    }

    public boolean isPhysicsEnabled() {
        return physicsEnabledCheckBox.isChecked();
    }

    public void setPhysicsEnable(boolean isPhysicsEnabled) {
        this.physicsEnabledCheckBox.setChecked(isPhysicsEnabled);
    }

    public String getGravityXValue() {
        return gravityXTextField.getText();
    }

    public void setGravityXValue(String gravityXValue) {
        this.gravityXTextField.setText(gravityXValue);
    }

    public String getGravityYValue() {
        return gravityYTextField.getText();
    }

    public void setGravityYValue(String gravityYValue) {
        this.gravityYTextField.setText(gravityYValue);
    }

    public String getSleepVelocityValue() {
        return sleepVelocityTextField.getText();
    }

    public void setSleepVelocityValue(String sleepVelocityValue) {
        this.sleepVelocityTextField.setText(sleepVelocityValue);
    }

    public void setBlurNum(String blurNum) {
        this.blurNumTextField.setText(blurNum);
    }

    public void setLightMapScale(String scale) {
        this.lightMapScaleTextField.setText(scale);
    }

    public String getBlurNumValue() {
        return blurNumTextField.getText();
    }

    public String getLightMapScaleValue() {
        return lightMapScaleTextField.getText();
    }

    public boolean isLightsEnabled() {
        return enableLightsCheckBox.isChecked();
    }

    public void setLightsEnabled(boolean isLightsEnabled) {
        this.enableLightsCheckBox.setChecked(isLightsEnabled);
    }

    public boolean isPseudo3DLightsEnabled() {
        return enablePseudo3DLightsCheckBox.isChecked();
    }

    public void setPseudo3DLightsEnabled(boolean isLightsEnabled) {
        this.enablePseudo3DLightsCheckBox.setChecked(isLightsEnabled);
    }

    public Color getAmbientColor() {
        return ambientColorComponent.getColorValue();
    }

    public void setAmbientColor(Color tintColor) {
        ambientColorComponent.setColorValue(tintColor);
    }

    public Color getDirectionalColor() {
        return directionalLightColor.getColorValue();
    }

    public void setDirectionalColor(Color tintColor) {
        directionalLightColor.setColorValue(tintColor);
    }

    public int getPixelsPerWorldUnit() {
        return Integer.parseInt(pixelsPerWorldUnitField.getText().toString());
    }

    public void setPixelsPerWorldUnit(int value, int resolutionWidth, int resolutionHeight) {
        pixelsPerWorldUnitField.setText(value+"");
        worldSizeField.setText((resolutionWidth / value) + " x " + (resolutionHeight / value));
    }

    public void initShader(HashMap<String, ShaderProgram> shaders) {
        Array<String> shaderNames = new Array<>();
        shaderNames.add("Default");
        shaders.keySet().forEach(shaderNames::add);

        shadersSelector.setItems(shaderNames);
    }

    public String getShader() {
        return shadersSelector.getSelected();
    }

    public void setSelectedShader(String currShaderName) {
        shadersSelector.setSelected(currShaderName);
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }

    private void setListeners() {
        physicsEnabledCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        gravityXTextField.addListener(new KeyboardListener(getUpdateEventName()));
        gravityYTextField.addListener(new KeyboardListener(getUpdateEventName()));
        sleepVelocityTextField.addListener(new KeyboardListener(getUpdateEventName()));
        enableLightsCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        enablePseudo3DLightsCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        blurNumTextField.addListener(new KeyboardListener(getUpdateEventName()));
        lightMapScaleTextField.addListener(new KeyboardListener(getUpdateEventName()));
        directionalRays.addListener(new NumberSelectorOverlapListener(getUpdateEventName()));
        directionalDegreeTextField.addListener(new KeyboardListener(getUpdateEventName()));
        directionalHeightTextField.addListener(new KeyboardListener(getUpdateEventName()));
        lightTypeBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        lightTypeBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateDisabled();
            }
        });

        ambientColorComponent.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                facade.sendNotification(AMBIENT_COLOR_BUTTON_CLICKED, ambientColorComponent.getColorValue(), null);
            }
        });

        directionalLightColor.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if (lightTypeBox.getSelected().equals("DIRECTIONAL"))
                    facade.sendNotification(DIRECTIONAL_COLOR_BUTTON_CLICKED, directionalLightColor.getColorValue(), null);
            }
        });
        shadersSelector.addListener(new SelectBoxChangeListener(getUpdateEventName()));
    }

    // ---- RemoteEditablePanel ----

    @Override
    public void setFieldValue(String key, Object value) {
        if (value == null) throw new IllegalArgumentException("null value for field: " + key);
        switch (key) {
            case "physicsEnabled": setPhysicsEnable(RemoteEditableSupport.toBool(value)); break;
            case "gravityX": setGravityXValue(RemoteEditableSupport.numberToString(value)); break;
            case "gravityY": setGravityYValue(RemoteEditableSupport.numberToString(value)); break;
            case "sleepVelocity": setSleepVelocityValue(RemoteEditableSupport.numberToString(value)); break;
            case "lightsEnabled": setLightsEnabled(RemoteEditableSupport.toBool(value)); break;
            case "pseudo3d": setPseudo3DLightsEnabled(RemoteEditableSupport.toBool(value)); break;
            case "blurNum": setBlurNum(RemoteEditableSupport.intToString(value)); break;
            case "lightMapScale": setLightMapScale(RemoteEditableSupport.intToString(value)); break;
            case "lightType":
                if (!RemoteEditableSupport.contains(lightTypeBox, value.toString())) {
                    throw new IllegalArgumentException("lightType '" + value + "' not valid; allowed: DIFFUSE, DIRECTIONAL, BRIGHT");
                }
                setLightType(value.toString()); break;
            case "directionalRays":
                if (directionalRays.isDisabled()) throw new IllegalArgumentException("directionalRays only editable when lightType is DIRECTIONAL");
                setDirectionalRays(RemoteEditableSupport.intToString(value)); break;
            case "directionalDegree":
                if (directionalDegreeTextField.isDisabled()) throw new IllegalArgumentException("directionalDegree only editable when lightType is DIRECTIONAL");
                setDirectionalDegree(RemoteEditableSupport.numberToString(value)); break;
            case "directionalHeight":
                if (directionalHeightTextField.isDisabled()) throw new IllegalArgumentException("directionalHeight only editable when lightType is DIRECTIONAL");
                setDirectionalHeight(RemoteEditableSupport.numberToString(value)); break;
            case "shader":
                if (!RemoteEditableSupport.contains(shadersSelector, value.toString())) {
                    throw new IllegalArgumentException("shader '" + value + "' not available (use list_assets 'shader' category, or 'Default')");
                }
                setSelectedShader(value.toString()); break;
            case "ambientColor":
                setAmbientColor(RemoteEditableSupport.toColor(value)); break;
            case "directionalColor":
                setDirectionalColor(RemoteEditableSupport.toColor(value)); break;
            default:
                throw new IllegalArgumentException("Unknown or unsupported field: " + key
                        + " (supported: physicsEnabled, gravityX, gravityY, sleepVelocity, lightsEnabled, pseudo3d, "
                        + "blurNum, lightMapScale, lightType, directionalRays, directionalDegree, directionalHeight, "
                        + "shader, ambientColor, directionalColor)");
        }
    }

    @Override
    public java.util.List<String> validateFieldValues() {
        java.util.List<String> errors = new java.util.ArrayList<>();
        RemoteEditableSupport.checkValid("gravityX", gravityXTextField, errors);
        RemoteEditableSupport.checkValid("gravityY", gravityYTextField, errors);
        RemoteEditableSupport.checkValid("sleepVelocity", sleepVelocityTextField, errors);
        RemoteEditableSupport.checkValid("blurNum", blurNumTextField, errors);
        RemoteEditableSupport.checkValid("lightMapScale", lightMapScaleTextField, errors);
        RemoteEditableSupport.checkValid("directionalDegree", directionalDegreeTextField, errors);
        RemoteEditableSupport.checkValid("directionalHeight", directionalHeightTextField, errors);
        return errors;
    }

    @Override
    public void setProgrammaticSelectBoxes(boolean programmatic) {
        if (!programmatic) return;
        // Off-stage transient instance: clear change listeners on the event-firing widgets so
        // their change events don't trigger the always-registered live scene mediator mid-drive.
        // (The instance is discarded after the edit, so listeners aren't restored.) Text fields use
        // KeyboardListener which only fires on keyTyped (not setText), so they're left alone.
        lightTypeBox.clearListeners();
        shadersSelector.clearListeners();
        physicsEnabledCheckBox.clearListeners();
        enableLightsCheckBox.clearListeners();
        enablePseudo3DLightsCheckBox.clearListeners();
        directionalRays.clearListeners();
    }

    @Override
    public void refreshDisabledState() {
        updateDisabled();
    }
}
