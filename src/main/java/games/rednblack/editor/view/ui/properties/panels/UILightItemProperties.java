package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.math.Vector3;
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
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.apache.commons.lang3.math.NumberUtils;

public class UILightItemProperties extends UIItemCollapsibleProperties implements RemoteEditablePanel {

    @Override
    public void setFieldValue(String key, Object value) {
        if (value == null) throw new IllegalArgumentException("null value for field: " + key);
        switch (key) {
            case "type":
                LightObjectComponent.LightType lt;
                try { lt = LightObjectComponent.LightType.valueOf(value.toString()); }
                catch (Exception e) { throw new IllegalArgumentException("type '" + value + "' not valid; allowed: POINT, CONE"); }
                setType(lt); break;
            case "rayCount": setRayCount(RemoteEditableSupport.toInt(value)); break;
            case "radius": setRadius(RemoteEditableSupport.numberToString(value)); break;
            case "angle": setAngle(RemoteEditableSupport.numberToString(value)); break;
            case "distance": setDistance(RemoteEditableSupport.numberToString(value)); break;
            case "direction": setDirection(RemoteEditableSupport.numberToString(value)); break;
            case "intensity": setLightIntensity(RemoteEditableSupport.numberToString(value)); break;
            case "height": setLightHeight(RemoteEditableSupport.numberToString(value)); break;
            case "softnessLength": setSoftnessLength(RemoteEditableSupport.numberToString(value)); break;
            case "falloff": setFalloff(RemoteEditableSupport.toVector3(value)); break;
            case "isStatic": setStatic(RemoteEditableSupport.toBool(value)); break;
            case "isXRay": setXRay(RemoteEditableSupport.toBool(value)); break;
            case "isSoft": setSoft(RemoteEditableSupport.toBool(value)); break;
            case "isActive": setActive(RemoteEditableSupport.toBool(value)); break;
            default: throw new IllegalArgumentException("Unknown field: " + key + " (supported: type, rayCount, radius, angle, distance, direction, intensity, height, softnessLength, falloff, isStatic, isXRay, isSoft, isActive)");
        }
    }
    @Override
    public java.util.List<String> validateFieldValues() {
        java.util.List<String> errors = new java.util.ArrayList<>();
        RemoteEditableSupport.checkValid("radius", pointLightRadiusField, errors);
        RemoteEditableSupport.checkValid("angle", coneInnerAngleField, errors);
        RemoteEditableSupport.checkValid("distance", coneDistanceField, errors);
        RemoteEditableSupport.checkValid("direction", coneDirectionField, errors);
        RemoteEditableSupport.checkValid("height", heightField, errors);
        RemoteEditableSupport.checkValid("intensity", intensityField, errors);
        RemoteEditableSupport.checkValid("constantFalloff", constantFalloffField, errors);
        RemoteEditableSupport.checkValid("linearFalloff", linearFalloffField, errors);
        RemoteEditableSupport.checkValid("quadraticFalloff", quadraticFalloffField, errors);
        RemoteEditableSupport.checkValid("softnessLength", softnessLengthField, errors);
        return errors;
    }
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
    private PropertyGrid grid;

    public UILightItemProperties() {
        super("Light");
        Validators.FloatValidator floatValidator = new Validators.FloatValidator();

        isStaticCheckBox = StandardWidgetsFactory.createSwitch();
        isXRayCheckBox = StandardWidgetsFactory.createSwitch();
        isSoftCheckBox = StandardWidgetsFactory.createSwitch();
        isActiveCheckBox = StandardWidgetsFactory.createSwitch();
        rayCountSelector = StandardWidgetsFactory.createNumberSelector(4, 4, 5000);
        lightTypeLabel = PropertyGrid.value("");
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

        grid = PropertyGrid.on(mainTable);
        grid.rowCompact("Type", lightTypeLabel);
        grid.row("Ray count", rayCountSelector);
        grid.row("Softness length", softnessLengthField);
        grid.row("Height", heightField);
        grid.row("Intensity", intensityField);
        grid.triple("Falloff", constantFalloffField, linearFalloffField, quadraticFalloffField);
        grid.subGrid(secondaryTable);
        grid.toggle("Static", isStaticCheckBox);
        grid.toggle("X-Ray", isXRayCheckBox);
        grid.toggle("Soft", isSoftCheckBox);
        grid.toggle("Active", isActiveCheckBox);

        setListeners();
    }

    public void initPointFields() {
        secondaryTable.clear();
        PropertyGrid.on(secondaryTable, grid).row("Radius", pointLightRadiusField);
    }

    public void initConeFields() {
        secondaryTable.clear();

        PropertyGrid cone = PropertyGrid.on(secondaryTable, grid);
        cone.row("Distance", coneDistanceField);
        cone.row("Angle", coneInnerAngleField);
        cone.row("Direction", coneDirectionField);
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
