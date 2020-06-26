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

package games.rednblack.editor.view.ui.panel;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.editor.renderer.utils.CustomVariables;
import games.rednblack.editor.utils.StandardWidgetsFactory;

/**
 * Created by azakhary on 5/12/2015.
 */
public class CustomVariablesPanel extends UIDraggablePanel {
    public static final String PREFIX = "games.rednblack.editor.view.ui.panel.CustomVariablesDialog";
    public static final String ADD_BUTTON_PRESSED = PREFIX + ".ADD_BUTTON_PRESSED";
    public static final String DELETE_BUTTON_PRESSED = PREFIX + ".DELETE_BUTTON_PRESSED";

    private HyperLap2DFacade facade;

    private VisTextField keyField;
    private VisTextField valueField;
    private VisTextButton addButton;

    private VisTable variablesList;
    private VisTable addVariableTable;

    public CustomVariablesPanel() {
        super("Custom variables");
        addCloseButton();

        facade = HyperLap2DFacade.getInstance();

        addVariableTable = new VisTable();
        addVariableTable.padTop(4);
        variablesList = new VisTable();

        createAddVariableTable();

        getContentTable().add(addVariableTable).row();

        getContentTable().add(variablesList).fillX().row();
    }

    private void createAddVariableTable() {
        addVariableTable.clear();
        keyField = StandardWidgetsFactory.createTextField();
        valueField = StandardWidgetsFactory.createTextField();
        addButton = new VisTextButton("Add");

        addVariableTable.add(keyField).padLeft(6);
        addVariableTable.add(valueField).padLeft(5);
        addVariableTable.add(addButton).width(38).padLeft(4).padRight(5);

        addVariableTable.row();
        initListeners();
    }

    public void setEmpty() {
        variablesList.clear();
        VisLabel label = StandardWidgetsFactory.createLabel("No item selected");
        label.setAlignment(Align.center);
        variablesList.add(label).pad(10).width(278).center();
        addVariableTable.clear();
        invalidateHeight();
    }

    public void updateView(CustomVariables vars) {
        variablesList.clear();
        createAddVariableTable();

        variablesList.add("Key name").expandX();
        variablesList.add("Value").colspan(2).expandX();
        variablesList.row();
        variablesList.addSeparator().colspan(3).expandX().fillX().row();

        for (Map.Entry<String, String> entry : vars.getHashMap().entrySet()) {
            final String key = entry.getKey();
            String value = entry.getValue();

            VisTable keyTbl = new VisTable();
            keyTbl.setBackground(VisUI.getSkin().getDrawable("layer-bg"));
            VisTable valueTbl = new VisTable();
            valueTbl.setBackground(VisUI.getSkin().getDrawable("layer-bg"));

            keyTbl.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    keyField.setText(key);
                    valueField.setText(value);
                }
            });
            VisLabel keyLabel = new VisLabel(key);
            keyTbl.add(keyLabel);

            valueTbl.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    keyField.setText(key);
                    valueField.setText(value);
                }
            });
            VisLabel valueLabel = new VisLabel(value);
            valueTbl.add(valueLabel);

            VisImageButton trashBtn = new VisImageButton("trash-button");

            variablesList.add(keyTbl).height(20).expandX().fillX();
            variablesList.add(valueTbl).height(20).expandX().fillX();
            variablesList.add(trashBtn).padLeft(4);
            variablesList.row().padBottom(2);

            trashBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    facade.sendNotification(DELETE_BUTTON_PRESSED, key);
                }
            });
        }

        addVariableTable.setVisible(true);

        invalidateHeight();
    }

    private void initListeners() {
        addButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(ADD_BUTTON_PRESSED);
            }
        });
    }

    public String getKey() {
        return keyField.getText();
    }

    public String getValue() {
        return valueField.getText();
    }

    public void setKeyFieldValue(String key) {
        keyField.setText(key);
    }

    public void setValueFieldValue(String value) {
        valueField.setText(value);
    }

}
