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

import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.event.ButtonToNotificationListener;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 7/2/2015.
 */
public class UIPolygonComponentProperties extends UIRemovableProperties implements RemoteEditablePanel {

    @Override
    public void setFieldValue(String key, Object value) {
        if (value == null) throw new IllegalArgumentException("null value for field: " + key);
        switch (key) {
            case "verticesCount": setVerticesCount(RemoteEditableSupport.toInt(value)); break;
            case "openPath": setOpenPath(RemoteEditableSupport.toBool(value)); break;
            default: throw new IllegalArgumentException("Unknown field: " + key + " (supported: verticesCount, openPath)");
        }
    }
    @Override public java.util.List<String> validateFieldValues() { return new java.util.ArrayList<>(); }

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UIPolygonComponentProperties";

    public static final String COPY_BUTTON_CLICKED = prefix + ".COPY_BUTTON_CLICKED";
    public static final String PASTE_BUTTON_CLICKED = prefix + ".PASTE_BUTTON_CLICKED";
    public static final String ADD_DEFAULT_MESH_BUTTON_CLICKED = prefix + ".ADD_DEFAULT_MESH_BUTTON_CLICKED";
    public static final String ADD_AUTO_TRACE_MESH_BUTTON_CLICKED = prefix + ".ADD_AUTO_TRACE_MESH_BUTTON_CLICKED";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";

    private VisTextButton addDefaultMeshButton;
    private VisTextButton addAutoTraceMeshButton;

    private VisLabel verticesCountLbl;
    private VisTextButton copyBtn;
    private VisTextButton pasteBtn;
    private VisCheckBox openEndedCheckbox;

    public UIPolygonComponentProperties() {
        super("Polygon Shape");
    }

    public void initView() {
        mainTable.clear();

        verticesCountLbl = PropertyGrid.value("");

        copyBtn = new VisTextButton("Copy");
        pasteBtn = new VisTextButton("Paste");
        openEndedCheckbox = StandardWidgetsFactory.createSwitch();

        PropertyGrid grid = PropertyGrid.on(mainTable);
        grid.rowCompact("Vertices", verticesCountLbl);
        grid.toggle("Open ended", openEndedCheckbox);
        grid.buttons(copyBtn, pasteBtn);

        initListeners();
    }

    public void setVerticesCount(int count) {
        verticesCountLbl.setText(count+"");
    }

    public void setOpenPath(boolean openPath) {
        openEndedCheckbox.setChecked(openPath);
    }

    public boolean isOpenEnded() {
        return openEndedCheckbox.isChecked();
    }

    public void initEmptyView() {
        mainTable.clear();

        addDefaultMeshButton = StandardWidgetsFactory.createTextButton("Make Default");
        addAutoTraceMeshButton = StandardWidgetsFactory.createTextButton("Auto Trace");

        PropertyGrid grid = PropertyGrid.on(mainTable);
        grid.wideCentered(PropertyGrid.value("This shape has no vertices"));
        grid.wideCentered(addDefaultMeshButton);
        grid.wideCentered(addAutoTraceMeshButton);

        initEmptyViewListeners();
    }

    private void initListeners() {
        copyBtn.addListener(new ButtonToNotificationListener(COPY_BUTTON_CLICKED));
        pasteBtn.addListener(new ButtonToNotificationListener(PASTE_BUTTON_CLICKED));
        openEndedCheckbox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
    }

    private void initEmptyViewListeners() {
        addDefaultMeshButton.addListener(new ButtonToNotificationListener(ADD_DEFAULT_MESH_BUTTON_CLICKED));
        addAutoTraceMeshButton.addListener(new ButtonToNotificationListener(ADD_AUTO_TRACE_MESH_BUTTON_CLICKED));
    }

    @Override
    public void onRemove() {
        facade.sendNotification(CLOSE_CLICKED);
    }
}
