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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.NumberSelectorOverlapListener;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Created by azakhary on 4/28/2015.
 */
public class UILightItemProperties extends UIItemCollapsibleProperties {
    private final Vector3 tmp = new Vector3();

    private VisCheckBox isStaticCheckBox;
    private VisCheckBox isXRayCheckBox;
    private Spinner rayCountSelector;

    private VisValidatableTextField pointLightRadiusField;
    private VisValidatableTextField coneInnerAngleField;
    private VisValidatableTextField heightField;
    private VisValidatableTextField intensityField;
    private VisValidatableTextField constantFalloffField, linearFalloffField, quadraticFalloffField;
    private VisValidatableTextField coneDistanceField;
    private VisValidatableTextField coneDirectionField;
    private VisValidatableTextField softnessLengthField;
    private VisCheckBox isActiveCheckBox;
    private VisCheckBox isSoftCheckBox;

    private VisLabel lightTypeLabel;

    private VisTable secondaryTable;

    public UILightItemProperties() {
        super("Light");
        Validators.FloatValidator floatValidator = new Validators.FloatValidator();

        isStaticCheckBox = StandardWidgetsFactory.createCheckBox("Static");
        isXRayCheckBox = StandardWidgetsFactory.createCheckBox("X-Ray");
        isSoftCheckBox = StandardWidgetsFactory.createCheckBox("Soft");
        isActiveCheckBox = StandardWidgetsFactory.createCheckBox("Active");
        rayCountSelector = StandardWidgetsFactory.createNumberSelector(4, 4, 5000);
        lightTypeLabel = new VisLabel();
        pointLightRadiusField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        coneInnerAngleField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        coneDistanceField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        softnessLengthField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        coneDirectionField =  StandardWidgetsFactory.createValidableTextField(floatValidator);
        heightField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        intensityField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        constantFalloffField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        linearFalloffField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        quadraticFalloffField = StandardWidgetsFactory.createValidableTextField(floatValidator);

        secondaryTable = new VisTable();

        mainTable.add(new VisLabel("Type: ", Align.right)).padRight(5).width(75).right();
        mainTable.add(lightTypeLabel).left();
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Ray Count: ", Align.right)).padRight(5).width(75).right();
        mainTable.add(rayCountSelector).left();
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Softness length: ", Align.right)).padRight(5).width(110).right();
        mainTable.add(softnessLengthField).width(92).left();
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Height: ", Align.right)).padRight(5).width(110).right();
        mainTable.add(heightField).width(92).left();
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Intensity: ", Align.right)).padRight(5).width(110).right();
        mainTable.add(intensityField).width(92).left();
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Falloff:", Align.right)).padRight(5).width(110).right();
        Table falloffTable = new Table();
        falloffTable.add(constantFalloffField).width(30).padRight(1);
        falloffTable.add(linearFalloffField).width(30).padRight(1);
        falloffTable.add(quadraticFalloffField).width(30);
        mainTable.add(falloffTable).width(100).width(92).left();
        mainTable.row().padTop(5);

        mainTable.add(secondaryTable).colspan(2);
        mainTable.row().padTop(5);

        VisTable bottomTable = new VisTable();
        bottomTable.add(isStaticCheckBox).left().padRight(5);
        bottomTable.add(isXRayCheckBox).padRight(5);
        bottomTable.add(isSoftCheckBox).padRight(5);
        bottomTable.row();
        bottomTable.add(isActiveCheckBox).padRight(5);
        mainTable.add(bottomTable).padBottom(5).colspan(4);
        mainTable.row().padTop(5);

        setListeners();
    }

    public void initPointFields() {
        secondaryTable.clear();

        secondaryTable.add(new VisLabel("Radius: ", Align.right)).padRight(5).width(110).right();
        secondaryTable.add(pointLightRadiusField).width(70).left();
        secondaryTable.row().padTop(5);
    }

    public void initConeFields() {
        secondaryTable.clear();

        secondaryTable.add(new VisLabel("Distance: ", Align.right)).padRight(5).width(110).right();
        secondaryTable.add(coneDistanceField).width(92).left();
        secondaryTable.row().padTop(5);
        secondaryTable.add(new VisLabel("Angle: ", Align.right)).padRight(5).width(110).right();
        secondaryTable.add(coneInnerAngleField).width(92).left();
        secondaryTable.row().padTop(5);
        secondaryTable.add(new VisLabel("Direction: ", Align.right)).padRight(5).width(110).right();
        secondaryTable.add(coneDirectionField).width(92).left();
        secondaryTable.row().padTop(5);
    }

    public void setType(LightObjectComponent.LightType type) {
        if (type == LightObjectComponent.LightType.POINT) {
            lightTypeLabel.setText("Point Light");
            initPointFields();
        } else if (type == LightObjectComponent.LightType.CONE) {
            lightTypeLabel.setText("Cone Light");
            initConeFields();
        }
    }

    public int getRayCount() {
        return ((IntSpinnerModel)rayCountSelector.getModel()).getValue();
    }

    public void setRayCount(int count) {
        ((IntSpinnerModel)rayCountSelector.getModel()).setValue(count);
    }

    public boolean isStatic() {
        return isStaticCheckBox.isChecked();
    }

    public void setStatic(boolean isStatic) {
        isStaticCheckBox.setChecked(isStatic);
    }

    public boolean isXRay() {
        return isXRayCheckBox.isChecked();
    }

    public void setXRay(boolean isXRay) {
        isXRayCheckBox.setChecked(isXRay);
    }

    public String getRadius() {
        return pointLightRadiusField.getText();
    }

    public void setRadius(String radius) {
        pointLightRadiusField.setText(radius);
    }

    public String getAngle() {
        return coneInnerAngleField.getText();
    }

    public void setAngle(String angle) {
        coneInnerAngleField.setText(angle);
    }

    public String getDistance() {
        return coneDistanceField.getText();
    }

    public void setDistance(String distance) {
        coneDistanceField.setText(distance);
    }

    public String getDirection() {
        return coneDirectionField.getText();
    }

    public void setDirection(String distance) {
        coneDirectionField.setText(distance);
    }

    public String getLightHeight() {
        return heightField.getText();
    }

    public void setLightIntensity(String intensity) {
        intensityField.setText(intensity);
    }

    public String getLightIntensity() {
        return intensityField.getText();
    }

    public void setLightHeight(String distance) {
        heightField.setText(distance);
    }

    public String getSoftnessLength() {
        return softnessLengthField.getText();
    }
    
    public void setSoftnessLength(String softness) {
        softnessLengthField.setText(softness);
    }

    public boolean isActive() {
        return isActiveCheckBox.isChecked();
    }

    public boolean isSoft() {
        return isSoftCheckBox.isChecked();
    }

    public void setSoft(boolean bool) {
        isSoftCheckBox.setChecked(bool);
    }

    public void setActive(boolean bool) {
        isActiveCheckBox.setChecked(bool);
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }

    public void setFalloff(Vector3 falloff) {
        constantFalloffField.setText(falloff.x + "");
        linearFalloffField.setText(falloff.y + "");
        quadraticFalloffField.setText(falloff.z + "");
    }

    public Vector3 getFalloff() {
        tmp.x = NumberUtils.toFloat(constantFalloffField.getText());
        tmp.y = NumberUtils.toFloat(linearFalloffField.getText());
        tmp.z = NumberUtils.toFloat(quadraticFalloffField.getText());
        return tmp;
    }

    private void setListeners() {
        isStaticCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        isXRayCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        rayCountSelector.addListener(new NumberSelectorOverlapListener(getUpdateEventName()));
        pointLightRadiusField.addListener(new KeyboardListener(getUpdateEventName()));
        coneInnerAngleField.addListener(new KeyboardListener(getUpdateEventName()));
        coneDistanceField.addListener(new KeyboardListener(getUpdateEventName()));
        softnessLengthField.addListener(new KeyboardListener(getUpdateEventName()));
        heightField.addListener(new KeyboardListener(getUpdateEventName()));
        intensityField.addListener(new KeyboardListener(getUpdateEventName()));
        coneDirectionField.addListener(new KeyboardListener(getUpdateEventName()));
        isSoftCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        isActiveCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        constantFalloffField.addListener(new KeyboardListener(getUpdateEventName()));
        linearFalloffField.addListener(new KeyboardListener(getUpdateEventName()));
        quadraticFalloffField.addListener(new KeyboardListener(getUpdateEventName()));
    }
}
