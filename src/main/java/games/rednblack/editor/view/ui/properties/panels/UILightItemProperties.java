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

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.NumberSelectorOverlapListener;
import games.rednblack.editor.utils.StandardWidgetsFactory;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.editor.renderer.data.LightVO;

/**
 * Created by azakhary on 4/28/2015.
 */
public class UILightItemProperties extends UIItemCollapsibleProperties {

    private VisCheckBox isStaticCheckBox;
    private VisCheckBox isXRayCheckBox;
    private Spinner rayCountSelector;

    private VisValidatableTextField pointLightRadiusField;
    private VisValidatableTextField coneInnerAngleField;
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
        pointLightRadiusField = new VisValidatableTextField(floatValidator);
        coneInnerAngleField = new VisValidatableTextField(floatValidator);
        coneDistanceField = new VisValidatableTextField(floatValidator);
        softnessLengthField = new VisValidatableTextField(floatValidator);
        coneDirectionField =  new VisValidatableTextField(floatValidator);

        secondaryTable = new VisTable();

        mainTable.add(new VisLabel("Type: ", Align.right)).padRight(5).width(75).right();
        mainTable.add(lightTypeLabel).left();
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Ray Count: ", Align.right)).padRight(5).width(75).right();
        mainTable.add(rayCountSelector).left();
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Softness length: ", Align.right)).padRight(5).width(100).right();
        mainTable.add(softnessLengthField).width(70).left();
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

        secondaryTable.add(new VisLabel("Radius: ", Align.right)).padRight(5).width(100).right();
        secondaryTable.add(pointLightRadiusField).width(70).left();
        secondaryTable.row().padTop(5);
    }

    public void initConeFields() {
        secondaryTable.clear();

        secondaryTable.add(new VisLabel("Distance: ", Align.right)).padRight(5).width(100).right();
        secondaryTable.add(coneDistanceField).width(70).left();
        secondaryTable.row().padTop(5);
        secondaryTable.add(new VisLabel("Angle: ", Align.right)).padRight(5).width(100).right();
        secondaryTable.add(coneInnerAngleField).width(70).left();
        secondaryTable.row().padTop(5);
        secondaryTable.add(new VisLabel("Direction: ", Align.right)).padRight(5).width(100).right();
        secondaryTable.add(coneDirectionField).width(70).left();
        secondaryTable.row().padTop(5);
    }

    public void setType(LightVO.LightType type) {
        if (type == LightVO.LightType.POINT) {
            lightTypeLabel.setText("Point Light");
            initPointFields();
        } else if (type == LightVO.LightType.CONE) {
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

    private void setListeners() {
        isStaticCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        isXRayCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        rayCountSelector.addListener(new NumberSelectorOverlapListener(getUpdateEventName()));
        pointLightRadiusField.addListener(new KeyboardListener(getUpdateEventName()));
        coneInnerAngleField.addListener(new KeyboardListener(getUpdateEventName()));
        coneDistanceField.addListener(new KeyboardListener(getUpdateEventName()));
        softnessLengthField.addListener(new KeyboardListener(getUpdateEventName()));
        coneDirectionField.addListener(new KeyboardListener(getUpdateEventName()));
        isSoftCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        isActiveCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
    }
}
