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
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.event.ButtonToNotificationListener;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.proxy.EntityMetadata;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIItemProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.TintButton;

/**
 * Created by azakhary on 4/15/2015.
 */
public class UIBasicItemProperties extends UIItemProperties implements RemoteEditablePanel {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UIBasicItemProperties";
    public static final String TINT_COLOR_BUTTON_CLICKED = prefix + ".TINT_COLOR_BUTTON_CLICKED";
    public static final String CUSTOM_VARS_BUTTON_CLICKED = prefix + ".CUSTOM_VARS_BUTTON_CLICKED";
    public static final String TAGS_BUTTON_CLICKED = prefix + ".TAGS_BUTTON_CLICKED";
    public static final String ADD_COMPONENT_BUTTON_CLICKED = prefix + ".ADD_COMPONENT_BUTTON_CLICKED";
    public static final String LINKING_CHANGED = prefix + ".LINKING_CHANGED";

    /** Item type icon and library link icon, matching the editor's 22px icon set. */
    private static final int ICON_SIZE = 22;

    private Image itemTypeIcon;
    private VisLabel itemType;

    private VisImageButton linkImage, linkScaleButton;
    private VisLabel libraryLinkLabel;

    private VisTable linkageContainer;
    private PropertyGrid.CollapsibleRow linkageRow;

    private VisTextField idBox;

    private VisTextField xValue;
    private VisTextField yValue;
    private VisTextField widthValue;
    private VisTextField heightValue;
    private VisTextField scaleXValue;
    private VisTextField scaleYValue;
    private VisCheckBox flipY;
    private VisCheckBox flipX;
    private TintButton tintColorComponent;
    private VisTextField rotationValue;
    private VisTextButton customVarsButton;
    private VisTextButton tagsButton;

    private VisSelectBox<String> nonExistantComponents;
    private VisTextButton addComponentButton;

    public UIBasicItemProperties() {
        Validators.FloatValidator floatValidator = new Validators.FloatValidator();
        Validators.GreaterThanValidator nonNegativeValidator = new Validators.GreaterThanValidator(0, true);

        // Both texts are user data of unbounded length, so they ellipsize instead of widening the panel.
        itemType = PropertyGrid.valueEllipsized("");
        itemTypeIcon = new Image();

        libraryLinkLabel = PropertyGrid.valueEllipsized("");
        linkImage = StandardWidgetsFactory.createImageButton("library-link-button");

        VisTable titleContainer = new VisTable();
        titleContainer.add(itemTypeIcon).size(ICON_SIZE).padRight(PropertyGrid.LABEL_GAP);
        PropertyGrid.elastic(titleContainer.add(itemType)).height(PropertyGrid.FIELD_HEIGHT);

        linkageContainer = new VisTable();
        linkageContainer.add(linkImage).size(ICON_SIZE).padRight(PropertyGrid.SUB_LABEL_GAP);
        PropertyGrid.elastic(linkageContainer.add(libraryLinkLabel));

        idBox = StandardWidgetsFactory.createTextField();
        xValue = StandardWidgetsFactory.createValidableTextField(floatValidator);
        yValue = StandardWidgetsFactory.createValidableTextField(floatValidator);
        widthValue = StandardWidgetsFactory.createValidableTextField(nonNegativeValidator);
        widthValue.setDisabled(true);
        heightValue = StandardWidgetsFactory.createValidableTextField(nonNegativeValidator);
        heightValue.setDisabled(true);
        scaleXValue = StandardWidgetsFactory.createValidableTextField(floatValidator);
        scaleYValue = StandardWidgetsFactory.createValidableTextField(floatValidator);
        linkScaleButton = StandardWidgetsFactory.createImageButton("library-link-button");
        flipY = StandardWidgetsFactory.createSwitch();
        flipX = StandardWidgetsFactory.createSwitch();
        tintColorComponent = StandardWidgetsFactory.createTintButton();
        rotationValue = StandardWidgetsFactory.createValidableTextField(floatValidator);
        customVarsButton = new VisTextButton("Custom Vars");
        tagsButton = new VisTextButton("Tags");

        nonExistantComponents = StandardWidgetsFactory.createSelectBox(String.class);
        addComponentButton = new VisTextButton("Add");

        VisTable componentsTable = new VisTable();
        PropertyGrid.elastic(componentsTable.add(nonExistantComponents)).height(PropertyGrid.FIELD_HEIGHT);
        componentsTable.add(addComponentButton).height(PropertyGrid.FIELD_HEIGHT).padLeft(PropertyGrid.BUTTON_GAP);

        PropertyGrid grid = PropertyGrid.on(this).padPanel();

        grid.wideFill(titleContainer);
        grid.separator();
        grid.row("Identifier", idBox);
        // Only library items are linked, for anything else the row takes no space at all.
        linkageRow = grid.collapsibleField(linkageContainer);
        linkageRow.setContentVisible(false);

        grid.section("Transform");
        grid.pair("Position", "X", xValue, "Y", yValue);
        grid.pair("Size", "W", widthValue, "H", heightValue);
        // The link toggle rides in the label column, so the fields stay on the grid like every other pair.
        grid.pair(PropertyGrid.labelWith(linkScaleButton, "Scale"), "X", scaleXValue, "Y", scaleYValue);
        grid.row("Rotation", rotationValue);

        grid.section("Appearance");
        grid.rowCompact("Tint", tintColorComponent);
        grid.togglePair("Flip", "X", flipX, "Y", flipY);

        grid.section("Metadata");
        grid.buttons(customVarsButton, tagsButton);

        grid.section("Components");
        grid.wideFill(componentsTable);

        setListeners();
    }

    public void setNonExistentComponents(Array<String> componentNames) {
        nonExistantComponents.setItems(componentNames);
    }

    public String getSelectedComponent() {
        return nonExistantComponents.getSelected();
    }

    public void setLinkage(boolean isLinked, String text) {
        linkageRow.setContentVisible(true);
        linkImage.setChecked(isLinked);
        libraryLinkLabel.setText(text);
    }

    public void disableLinkage() {
        linkageRow.setContentVisible(false);
    }

    public void setItemType(int type, String itemUniqueId) {
        itemType.setText(EntityMetadata.itemTypeNameMap.get(type) + " ("+itemUniqueId+")");
        itemTypeIcon.setDrawable(VisUI.getSkin().getDrawable(EntityMetadata.itemTypeIconMap.get(type)));
        itemTypeIcon.setScaling(Scaling.fit);
    }

    public String getIdBoxValue() {
        return idBox.getText();
    }

    public void setIdBoxValue(String idBox) {
        this.idBox.setText(idBox);
    }

    public String getRotationValue() {
        return rotationValue.getText();
    }

    public void setRotationValue(String rotationValue) {
        this.rotationValue.setText(rotationValue);
    }

    public String getXValue() {
        return xValue.getText();
    }

    public void setXValue(String xValue) {
        this.xValue.setText(xValue);
    }

    public String getYValue() {
        return yValue.getText();
    }

    public void setYValue(String yValue) {
        this.yValue.setText(yValue);
    }

    public String getWidthValue() {
        return widthValue.getText();
    }

    public void setWidthValue(String widthValue) {
        this.widthValue.setText(widthValue);
    }

    public void setWidthHeightDisabled(boolean disabled) {
        this.widthValue.setDisabled(disabled);
        this.heightValue.setDisabled(disabled);
    }

    public String getHeightValue() {
        return heightValue.getText();
    }

    public void setHeightValue(String heightValue) {
        this.heightValue.setText(heightValue);
    }

    public String getScaleXValue() {
        return scaleXValue.getText();
    }

    public void setScaleXValue(String scaleXValue) {
        this.scaleXValue.setText(scaleXValue);
    }

    public String getScaleYValue() {
        return scaleYValue.getText();
    }

    public void setScaleYValue(String scaleYValue) {
        this.scaleYValue.setText(scaleYValue);
    }

    public boolean getFlipY() {
        return flipY.isChecked();
    }

    public void setFlipY(boolean flipY) {
        this.flipY.setChecked(flipY);
    }

    public boolean getFlipX() {
        return flipX.isChecked();
    }

    public void setFlipX(boolean flipX) {
        this.flipX.setChecked(flipX);
    }

    public Color getTintColor() {
        return tintColorComponent.getColorValue();
    }

    public void setTintColor(Color tintColor) {
        tintColorComponent.setColorValue(tintColor);
    }

    public boolean isXYScaleLinked() {
        return linkScaleButton.isChecked();
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    // ---- RemoteEditablePanel: programmatic field setting + validation (MCP RemoteOps path) ----

    @Override
    public void setFieldValue(String key, Object value) {
        if (value == null) throw new IllegalArgumentException("null value for field: " + key);
        switch (key) {
            case "x": xValue.setText(numberToString(value)); break;
            case "y": yValue.setText(numberToString(value)); break;
            case "width":
                if (widthValue.isDisabled()) throw new IllegalArgumentException("width is not editable for this entity type");
                widthValue.setText(numberToString(value)); break;
            case "height":
                if (heightValue.isDisabled()) throw new IllegalArgumentException("height is not editable for this entity type");
                heightValue.setText(numberToString(value)); break;
            case "scaleX": scaleXValue.setText(numberToString(value)); break;
            case "scaleY": scaleYValue.setText(numberToString(value)); break;
            case "rotation": rotationValue.setText(numberToString(value)); break;
            case "flipX": flipX.setChecked(toBoolean(value)); break;
            case "flipY": flipY.setChecked(toBoolean(value)); break;
            case "id": idBox.setText(value.toString()); break;
            case "tint": setTintColor(RemoteEditableSupport.toColor(value)); break;
            default:
                throw new IllegalArgumentException("Unknown or unsupported field: " + key
                        + " (supported: x, y, width, height, scaleX, scaleY, rotation, flipX, flipY, id, tint)");
        }
    }

    @Override
    public java.util.List<String> validateFieldValues() {
        java.util.List<String> errors = new java.util.ArrayList<>();
        checkValid("x", xValue, errors);
        checkValid("y", yValue, errors);
        checkValid("width", widthValue, errors);
        checkValid("height", heightValue, errors);
        checkValid("scaleX", scaleXValue, errors);
        checkValid("scaleY", scaleYValue, errors);
        checkValid("rotation", rotationValue, errors);
        return errors;
    }

    private static void checkValid(String key, VisTextField field, java.util.List<String> errors) {
        if (field instanceof VisValidatableTextField) {
            VisValidatableTextField v = (VisValidatableTextField) field;
            String text = v.getText();
            for (com.kotcrab.vis.ui.util.InputValidator validator : v.getValidators()) {
                if (!validator.validateInput(text)) {
                    errors.add("field '" + key + "' invalid value: '" + text + "'");
                    return;
                }
            }
        }
    }

    private static String numberToString(Object value) {
        if (value instanceof Number) return String.valueOf(((Number) value).floatValue());
        return value.toString();
    }

    private static boolean toBoolean(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    private void setListeners() {
        idBox.addListener(new KeyboardListener(getUpdateEventName()));
        xValue.addListener(new KeyboardListener(getUpdateEventName()));
        yValue.addListener(new KeyboardListener(getUpdateEventName()));
        widthValue.addListener(new KeyboardListener(getUpdateEventName()));
        heightValue.addListener(new KeyboardListener(getUpdateEventName()));
        scaleXValue.addListener(new KeyboardListener(getUpdateEventName()));
        scaleXValue.addListener(new LinkedScaleChangeListener(scaleYValue));
        scaleYValue.addListener(new KeyboardListener(getUpdateEventName()));
        scaleYValue.addListener(new LinkedScaleChangeListener(scaleXValue));
        flipY.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        flipX.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        rotationValue.addListener(new KeyboardListener(getUpdateEventName()));

        tintColorComponent.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                facade.sendNotification(TINT_COLOR_BUTTON_CLICKED, tintColorComponent.getColorValue(), null);
            }
        });
        customVarsButton.addListener(new ButtonToNotificationListener(CUSTOM_VARS_BUTTON_CLICKED));
        tagsButton.addListener(new ButtonToNotificationListener(TAGS_BUTTON_CLICKED));
        addComponentButton.addListener(new ButtonToNotificationListener(ADD_COMPONENT_BUTTON_CLICKED));

        linkImage.addListener(new ClickListener() {
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                boolean isLinked = linkImage.isChecked();
                facade.sendNotification(LINKING_CHANGED, isLinked);
            }
        });
    }

    private final class LinkedScaleChangeListener extends ChangeListener {
        private final VisTextField linkedField;

        LinkedScaleChangeListener(VisTextField linkedField) {
            this.linkedField = linkedField;
        }

        @Override
        public void changed(ChangeEvent event, Actor actor) {
            if (isXYScaleLinked() && actor instanceof VisTextField) {
                VisTextField field = (VisTextField) actor;
                if (!field.getText().equals(linkedField.getText()))
                    linkedField.setText(field.getText());
            }
        }
    }
}
