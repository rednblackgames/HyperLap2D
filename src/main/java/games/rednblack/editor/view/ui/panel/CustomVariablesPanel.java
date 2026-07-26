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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.FormRow;
import games.rednblack.h2d.common.view.ui.ListTable;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

/**
 * Created by azakhary on 5/12/2015.
 */
public class CustomVariablesPanel extends UIDraggablePanel {
    public static final String PREFIX = "games.rednblack.editor.view.ui.panel.CustomVariablesDialog";
    public static final String ADD_BUTTON_PRESSED = PREFIX + ".ADD_BUTTON_PRESSED";
    public static final String DELETE_BUTTON_PRESSED = PREFIX + ".DELETE_BUTTON_PRESSED";

    private static final int MIN_WIDTH = 460;

    private final Facade facade;

    private final VisTextField keyField;
    private final VisTextField valueField;
    private final VisTextButton addButton;

    private final VisTable mainTable;

    public CustomVariablesPanel() {
        super("Custom variables");
        addCloseButton();

        facade = Facade.getInstance();

        keyField = StandardWidgetsFactory.createTextField();
        valueField = StandardWidgetsFactory.createTextField();
        addButton = StandardWidgetsFactory.createTextButton("Add");
        addButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(ADD_BUTTON_PRESSED);
            }
        });

        mainTable = new VisTable();
        getContentTable().add(mainTable).growX();
    }

    public void setEmptyMsg(String msg) {
        mainTable.clear();
        PropertyGrid.on(mainTable).dialogScale().padPanel().wideCentered(PropertyGrid.value(msg));
        invalidateHeight();
    }

    public void updateView(ObjectMap<String, String> vars) {
        mainTable.clear();

        PropertyGrid grid = PropertyGrid.on(mainTable).dialogScale().padPanel();
        grid.section("New variable");
        grid.wideContent(new FormRow()
                .label("Key").field(keyField)
                .label("Value").field(valueField)
                .action(addButton));

        grid.section("Variables");
        grid.wideContent(createVariablesList(vars));

        invalidateHeight();
    }

    /** Clicking a row loads it back into the form above, which is how a variable gets edited. */
    private ListTable createVariablesList(ObjectMap<String, String> vars) {
        ListTable list = new ListTable("Key", "Value");
        if (vars.size == 0) {
            return list.message("This item has no custom variables");
        }
        for (ObjectMap.Entry<String, String> entry : vars) {
            String key = entry.key;
            String value = entry.value;

            VisImageButton deleteButton = StandardWidgetsFactory.createImageButton("trash-button");
            deleteButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    facade.sendNotification(DELETE_BUTTON_PRESSED, key);
                }
            });

            list.item(key, value).action(deleteButton).onClick(() -> {
                keyField.setText(key);
                valueField.setText(value);
            });
        }
        return list;
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

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
    }
}
