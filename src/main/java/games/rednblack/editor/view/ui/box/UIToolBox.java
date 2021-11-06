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

package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.HashMap;

public class UIToolBox extends VisTable {
    private final ButtonGroup<VisImageButton> toolsButtonGroup;

    private HashMap<String, VisImageButton> buttonMap = new HashMap<>();

    public UIToolBox() {
        toolsButtonGroup = new ButtonGroup<>();
    }

    public void addToolButton(String name, Tool tool) {
        VisImageButton button = createButton("tool-" + name, name);
        String toolTip = tool.getTitle() + (tool.getShortcut() != null ? " (" + tool.getShortcut() +")" : "");
        StandardWidgetsFactory.addTooltip(button, toolTip);

        buttonMap.put(name, button);
        add(button).width(31).height(31).row();
    }

    public void addToolButton(String name, VisImageButton.VisImageButtonStyle btnStyle, Tool tool) {
        VisImageButton button = createButton(btnStyle, name);
        String toolTip = tool.getTitle() + (tool.getShortcut() != null ? " (" + tool.getShortcut() +")" : "");
        StandardWidgetsFactory.addTooltip(button, toolTip);


        buttonMap.put(name, button);
        add(button).width(31).height(31).row();
    }

    private VisImageButton createButton(String styleName, String toolId) {
        VisImageButton visImageButton = new VisImageButton(styleName);
        toolsButtonGroup.add(visImageButton);
        visImageButton.addListener(new ToolboxButtonClickListener(toolId));
        return visImageButton;
    }

    private VisImageButton createButton(VisImageButton.VisImageButtonStyle btnStyle, String toolId) {
        VisImageButton visImageButton = new VisImageButton(btnStyle);
        toolsButtonGroup.add(visImageButton);
        visImageButton.addListener(new ToolboxButtonClickListener(toolId));
        return visImageButton;
    }

    private static class ToolboxButtonClickListener extends ClickListener {

        private final String toolId;

        public ToolboxButtonClickListener(String toolId) {
            this.toolId = toolId;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
            facade.sendNotification(MsgAPI.TOOL_CLICKED, toolId);
        }
    }

    public void setCurrentTool(String tool) {
        buttonMap.get(tool).setChecked(true);
    }
}
