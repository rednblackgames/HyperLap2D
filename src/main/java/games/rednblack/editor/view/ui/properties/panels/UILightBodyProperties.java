package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.NumberSelectorOverlapListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.widget.TintButton;

import java.util.HashMap;

public class UILightBodyProperties extends UIRemovableProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UILightBodyProperties";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";
    public static final String LIGHT_COLOR_BUTTON_CLICKED = prefix + ".LIGHT_COLOR_BUTTON_CLICKED";

    private HashMap<Integer, String> directionTypes = new HashMap<>();

    private Spinner raysTextSelector;
    private VisTextField distanceTextField, intensityField;
    private TintButton lightColor;
    private VisValidatableTextField softnessLengthField;
    private VisCheckBox isStaticCheckBox;
    private VisCheckBox isXRayCheckBox;
    private VisCheckBox isSoftCheckBox;
    private VisCheckBox isActiveCheckBox;
    private VisSelectBox<String> directionBox;

    public UILightBodyProperties() {
        super("Light");

        directionTypes.put(1, "LEFT");
        directionTypes.put(-1, "RIGHT");

        initView();
        initListeners();
    }

    public void initView() {
        directionBox = StandardWidgetsFactory.createSelectBox(String.class);
        Array<String> types = new Array<>();
        directionTypes.values().forEach(types::add);
        directionBox.setItems(types);

        Validators.FloatValidator floatValidator = new Validators.FloatValidator();
        Validators.IntegerValidator integerValidator = new Validators.IntegerValidator();

        lightColor = StandardWidgetsFactory.createTintButton();
        raysTextSelector = StandardWidgetsFactory.createNumberSelector(4, 4, 5000);
        distanceTextField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        intensityField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        softnessLengthField = new VisValidatableTextField(floatValidator);
        isStaticCheckBox = StandardWidgetsFactory.createCheckBox("Static");
        isXRayCheckBox = StandardWidgetsFactory.createCheckBox("X-Ray");
        isSoftCheckBox = StandardWidgetsFactory.createCheckBox("Soft");
        isActiveCheckBox = StandardWidgetsFactory.createCheckBox("Active");

        mainTable.add(new VisLabel("Direction:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(directionBox).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Color:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(lightColor).left();
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Ray Count:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(raysTextSelector).left().colspan(2);
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Distance:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(distanceTextField).width(100).colspan(2);
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Intensity:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(intensityField).width(100).colspan(2);
        mainTable.row().padTop(5);
        mainTable.add(new VisLabel("Softness length: ", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(softnessLengthField).width(100).colspan(2);
        mainTable.row().padTop(5);

        VisTable bottomTable = new VisTable();
        //bottomTable.add(isStaticCheckBox).padRight(5);//TODO Figure out why static lights does not change position
        bottomTable.add(isXRayCheckBox).padRight(5);
        bottomTable.add(isSoftCheckBox).padRight(5);
        bottomTable.add(isActiveCheckBox).padRight(5);

        mainTable.add(bottomTable).padBottom(5).colspan(4);
        mainTable.row().padTop(5);
    }

    public void setRays(String rays) {
        raysTextSelector.getTextField().setText(rays);
    }

    public String getRays() {
        return raysTextSelector.getTextField().getText();
    }

    public void setDistance(String rays) {
        distanceTextField.setText(rays);
    }

    public String getDistance() {
        return distanceTextField.getText();
    }

    public void setLightIntensity(String intensity) {
        intensityField.setText(intensity);
    }

    public String getLightIntensity() {
        return intensityField.getText();
    }

    public void setSoftnessLength(String rays) {
        softnessLengthField.setText(rays);
    }

    public String getSoftnessLength() {
        return softnessLengthField.getText();
    }

    public int getDirection() {
        for(Integer key: directionTypes.keySet()) {
            if(directionTypes.get(key).equals(directionBox.getSelected())) {
                return key;
            }
        }
        return 1;
    }

    public void setDirection(int direction) {
        directionBox.setSelected(directionTypes.get(direction));
    }

    public boolean isStatic() {
        return isStaticCheckBox.isChecked();
    }
    public boolean isXRay() {
        return isXRayCheckBox.isChecked();
    }
    public boolean isSoft() {
        return isSoftCheckBox.isChecked();
    }
    public boolean isActive() {
        return isActiveCheckBox.isChecked();
    }

    public void setStatic(boolean bool) {
        isStaticCheckBox.setChecked(bool);
    }
    public void setXRay(boolean bool) {
        isXRayCheckBox.setChecked(bool);
    }
    public void setSoft(boolean bool) {
        isSoftCheckBox.setChecked(bool);
    }
    public void setActive(boolean bool) {
        isActiveCheckBox.setChecked(bool);
    }

    public Color getLightColor() {
        return lightColor.getColorValue();
    }

    public void setLightColor(Color tintColor) {
        lightColor.setColorValue(tintColor);
    }

    private void initListeners() {
        directionBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));

        isStaticCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        isXRayCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        isSoftCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        isActiveCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));

        raysTextSelector.addListener(new NumberSelectorOverlapListener(getUpdateEventName()));
        distanceTextField.addListener(new KeyboardListener(getUpdateEventName()));
        intensityField.addListener(new KeyboardListener(getUpdateEventName()));
        softnessLengthField.addListener(new KeyboardListener(getUpdateEventName()));

        lightColor.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                facade.sendNotification(LIGHT_COLOR_BUTTON_CLICKED, lightColor.getColorValue(), null);
            }
        });
    }

    @Override
    public void onRemove() {
        HyperLap2DFacade.getInstance().sendNotification(CLOSE_CLICKED);
    }
}
