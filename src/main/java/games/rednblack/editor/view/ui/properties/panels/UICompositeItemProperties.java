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
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.H2DTextTooltip;

/**
 * Created by azakhary on 4/16/2015.
 */
public class UICompositeItemProperties extends UIItemCollapsibleProperties implements RemoteEditablePanel {

    @Override
    public void setFieldValue(String key, Object value) {
        if (value == null) throw new IllegalArgumentException("null value for field: " + key);
        switch (key) {
            case "scissorsEnabled": setScissorsEnabled(RemoteEditableSupport.toBool(value)); break;
            case "renderToFBO": setRenderToFBOEnabled(RemoteEditableSupport.toBool(value)); break;
            case "automaticResize": setAutomaticResize(RemoteEditableSupport.toBool(value)); break;
            default: throw new IllegalArgumentException("Unknown field: " + key + " (supported: scissorsEnabled, renderToFBO, automaticResize)");
        }
    }

    @Override
    public java.util.List<String> validateFieldValues() { return new java.util.ArrayList<>(); }

    private final VisCheckBox scissorsEnabledCheckBox, automaticResizeCheckBox, renderToFBOCheckBox;
    private boolean ownedByWidget, sizedByAutoLayout;
    /** Says why automatic resize is locked, while an auto layout sizes the composite. */
    private H2DTextTooltip autoLayoutTooltip;

    public UICompositeItemProperties() {
        super("Composite");
        scissorsEnabledCheckBox = StandardWidgetsFactory.createSwitch();
        automaticResizeCheckBox = StandardWidgetsFactory.createSwitch();
        renderToFBOCheckBox = StandardWidgetsFactory.createSwitch();

        PropertyGrid grid = PropertyGrid.on(mainTable);
        grid.toggle("Scissors", scissorsEnabledCheckBox);
        grid.toggle("Automatic resize", automaticResizeCheckBox);
        grid.toggle("Render to FBO", renderToFBOCheckBox);

        setListeners();
    }

    public boolean isScissorsEnabled() {
        return scissorsEnabledCheckBox.isChecked();
    }

    public boolean isRenderToFBOEnabled() {
        return renderToFBOCheckBox.isChecked();
    }

    public void setScissorsEnabled(boolean scissorsEnabled) {
        scissorsEnabledCheckBox.setChecked(scissorsEnabled);
    }

    public void setRenderToFBOEnabled(boolean renderToFBO) {
        renderToFBOCheckBox.setChecked(renderToFBO);
        scissorsEnabledCheckBox.setDisabled(renderToFBO);
    }

    /**
     * A widget such as a scroll pane keeps its own rectangle and clips what it holds: both switches
     * belong to it, so they are shown as they are and cannot be changed by hand.
     */
    public void setOwnedByWidget(boolean owned) {
        ownedByWidget = owned;
        scissorsEnabledCheckBox.setDisabled(owned);
        automaticResizeCheckBox.setDisabled(owned || sizedByAutoLayout);
    }

    /**
     * An auto layout decides the composite's size, so automatic resize is shown off and locked. Only
     * the panel says so: the composite keeps its setting for when the layout is taken away.
     */
    public void setSizedByAutoLayout(boolean sized) {
        if (sized == sizedByAutoLayout) return;
        sizedByAutoLayout = sized;
        automaticResizeCheckBox.setDisabled(ownedByWidget || sized);
        if (sized) {
            if (autoLayoutTooltip == null) autoLayoutTooltip = StandardWidgetsFactory.addTooltip(automaticResizeCheckBox, "The auto layout decides this composite's size");
            else automaticResizeCheckBox.addListener(autoLayoutTooltip);
        } else if (autoLayoutTooltip != null) {
            automaticResizeCheckBox.removeListener(autoLayoutTooltip);
        }
    }

    public boolean isAutomaticResizeIsEnabled(){
        return automaticResizeCheckBox.isChecked();
    }

    public void setAutomaticResize(boolean automaResize){
        automaticResizeCheckBox.setChecked(automaResize);
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }

    private void setListeners() {
        scissorsEnabledCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        automaticResizeCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        renderToFBOCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
    }
}
