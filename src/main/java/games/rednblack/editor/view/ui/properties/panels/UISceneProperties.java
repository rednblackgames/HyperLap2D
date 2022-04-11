/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.event.*;
import games.rednblack.editor.view.ui.properties.UIAbstractProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.TintButton;

import java.util.HashMap;

public class UISceneProperties extends UIAbstractProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UISceneProperties";
    public static final String AMBIENT_COLOR_BUTTON_CLICKED = prefix + ".AMBIENT_COLOR_BUTTON_CLICKED";
    public static final String DIRECTIONAL_COLOR_BUTTON_CLICKED = prefix + ".DIRECTIONAL_COLOR_BUTTON_CLICKED";

    public static final String EDIT_SHADER_BUTTON_CLICKED = prefix + ".EDIT_SHADER_BUTTON_CLICKED";
    public static final String EDIT_SHADER_DONE = prefix + ".EDIT_SHADER_DONE";
    public static final String UNIFORMS_SHADER_BUTTON_CLICKED = prefix + ".UNIFORMS_SHADER_BUTTON_CLICKED";

    final private VisLabel pixelsPerWorldUnitField;
    final private VisLabel worldSizeField;
    final private VisCheckBox physicsEnabledCheckBox;
    final private VisTextField gravityXTextField;
    final private VisTextField gravityYTextField;
    final private VisTextField sleepVelocityTextField;
    final private VisTextField blurNumTextField;
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

        pixelsPerWorldUnitField = new VisLabel("1");
        worldSizeField = new VisLabel("0 x 0");
        physicsEnabledCheckBox = StandardWidgetsFactory.createCheckBox();
        gravityXTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        gravityYTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        sleepVelocityTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        blurNumTextField = StandardWidgetsFactory.createValidableTextField(integerValidator);
        enableLightsCheckBox = StandardWidgetsFactory.createCheckBox();
        enablePseudo3DLightsCheckBox = StandardWidgetsFactory.createCheckBox();
        ambientColorComponent = StandardWidgetsFactory.createTintButton();
        lightTypeBox = StandardWidgetsFactory.createSelectBox(String.class);
        directionalRays = StandardWidgetsFactory.createNumberSelector(4, 4, 5000);
        directionalDegreeTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        directionalHeightTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        directionalLightColor = StandardWidgetsFactory.createTintButton();

        lightTypeBox.setItems("DIFFUSE", "DIRECTIONAL", "BRIGHT");

        pad(5);
        add(new VisLabel("Pixels per WU:", Align.right)).padRight(5).width(115);
        add(pixelsPerWorldUnitField).width(30).left().padLeft(7);
        row().padTop(5);
        add(new VisLabel("World size:", Align.right)).padRight(5).width(115);
        add(worldSizeField).width(30).left().padLeft(7);
        row().padTop(5);
        addSeparator().colspan(2).padTop(5).padBottom(5);
        shadersSelector = StandardWidgetsFactory.createSelectBox(String.class);
        add(new VisLabel("Scene Shader: ", Align.right)).padRight(5).width(75).right();
        add(shadersSelector).width(100).left().row();
        //TextButton editButton = StandardWidgetsFactory.createTextButton("Edit");
        //editButton.addListener(new ButtonToNotificationListener(EDIT_SHADER_BUTTON_CLICKED));
        //add(editButton).padTop(5).padRight(3);
        //TextButton uniformsButton = StandardWidgetsFactory.createTextButton("Uniforms");
        //uniformsButton.addListener(new ButtonToNotificationListener(UNIFORMS_SHADER_BUTTON_CLICKED));
        //add(uniformsButton).padTop(5).row();
        addSeparator().colspan(2).padTop(5).padBottom(5);
        add(new VisLabel("Physics enabled:", Align.right)).padRight(5).width(115);
        add(physicsEnabledCheckBox).padLeft(1).left();
        row().padTop(5);
        add(new VisLabel("Gravity X:", Align.right)).padRight(5).width(115);
        add(gravityXTextField).width(100);
        row().padTop(5);
        add(new VisLabel("Gravity Y:", Align.right)).padRight(5).width(115);
        add(gravityYTextField).width(100);
        row().padTop(5);
        add(new VisLabel("Sleep velocity:", Align.right)).padRight(5).width(115);
        add(sleepVelocityTextField).width(100);
        row().padTop(5);
        addSeparator().colspan(2).padTop(5).padBottom(5);
        add(new VisLabel("Enable Lights:", Align.right)).padRight(5).width(115);
        add(enableLightsCheckBox).padLeft(1).left();
        row().padTop(5);
        add(new VisLabel("Enable Pseudo3D:", Align.right)).padRight(5).width(115);
        add(enablePseudo3DLightsCheckBox).padLeft(1).left();
        row().padTop(5);
        add(new VisLabel("Shadows Blur:", Align.right)).padRight(5).width(115);
        add(blurNumTextField).width(100);
        row().padTop(5);
        add(new VisLabel("Ambient Color:", Align.right)).padRight(5).width(115);
        add(ambientColorComponent).padLeft(1).left();
        row().padTop(5);
        add(new VisLabel("Light Type:", Align.right)).padRight(5).width(115);
        add(lightTypeBox).fillX().padLeft(1).left();
        row().padTop(5);

        directionalTable.add(StandardWidgetsFactory.createLabel("Directional Light Settings:", Align.left)).fillX().expandX().colspan(2).padLeft(8);
        directionalTable.row().padTop(6);
        directionalTable.add(new VisLabel("Color:", Align.right)).padRight(5).width(115);
        directionalTable.add(directionalLightColor).left();
        directionalTable.row().padTop(5);
        directionalTable.add(new VisLabel("Rays:", Align.right)).padRight(5).width(115);
        directionalTable.add(directionalRays).left();
        directionalTable.row().padTop(5);
        directionalTable.add(new VisLabel("Degree:", Align.right)).padRight(5).width(115);
        directionalTable.add(directionalDegreeTextField).width(100);
        directionalTable.row().padTop(5);
        directionalTable.add(new VisLabel("Height:", Align.right)).padRight(5).width(115);
        directionalTable.add(directionalHeightTextField).width(100);
        directionalTable.row().padTop(5);

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
        directionalRays.setDisabled(!lightTypeBox.getSelected().equals("DIRECTIONAL"));
        directionalDegreeTextField.setDisabled(!lightTypeBox.getSelected().equals("DIRECTIONAL"));
        if (lightTypeBox.getSelected().equals("DIRECTIONAL")) {
            if (!directionalTable.hasParent()) {
                row();
                add(directionalTable).colspan(2);
            }
        } else {
            directionalTable.remove();
        }
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

    public String getBlurNumValue() {
        return blurNumTextField.getText();
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
}
