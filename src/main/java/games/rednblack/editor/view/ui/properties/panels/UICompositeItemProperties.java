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
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 4/16/2015.
 */
public class UICompositeItemProperties extends UIItemCollapsibleProperties {

    private final VisCheckBox scissorsEnabledCheckBox, automaticResizeCheckBox, renderToFBOCheckBox;

    public UICompositeItemProperties() {
        super("Composite");
        scissorsEnabledCheckBox = StandardWidgetsFactory.createCheckBox("Scissors Enabled");
        automaticResizeCheckBox = StandardWidgetsFactory.createCheckBox("Automatic Resize");
        renderToFBOCheckBox = StandardWidgetsFactory.createCheckBox("Render to FBO");

        mainTable.add(scissorsEnabledCheckBox).left().row();
        mainTable.add(automaticResizeCheckBox).left().row();
        mainTable.add(renderToFBOCheckBox).left();

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
