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

import com.kotcrab.vis.ui.widget.Separator;
import com.kotcrab.vis.ui.widget.VisImageButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.view.stage.tools.*;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.tools.Tool;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.HashMap;

public class UIToolBoxMediator extends Mediator<UIToolBox> {
    private static final String TAG = UIToolBoxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private HashMap<String, Tool> toolList;
    private String currentTool;

    public UIToolBoxMediator() {
        super(NAME, new UIToolBox());
    }

    @Override
    public void onRegister() {
        facade = HyperLap2DFacade.getInstance();

        toolList = new HashMap<>();
        initToolList();
        currentTool = SelectionTool.NAME;
    }

    private void initToolList() {
        toolList.put(SelectionTool.NAME, new SelectionTool());
        viewComponent.addToolButton(SelectionTool.NAME, toolList.get(SelectionTool.NAME));
        toolList.put(TransformTool.NAME, new TransformTool());
        viewComponent.addToolButton(TransformTool.NAME, toolList.get(TransformTool.NAME));
        toolList.put(PolygonTool.NAME, new PolygonTool());
        viewComponent.addToolButton(PolygonTool.NAME, toolList.get(PolygonTool.NAME));

        viewComponent.add(new Separator("tool")).padTop(2).padBottom(2).fill().expand().row();

        toolList.put(TextTool.NAME, new TextTool());
        viewComponent.addToolButton(TextTool.NAME, toolList.get(TextTool.NAME));
        toolList.put(PointLightTool.NAME, new PointLightTool());
        viewComponent.addToolButton(PointLightTool.NAME, toolList.get(PointLightTool.NAME));
        toolList.put(ConeLightTool.NAME, new ConeLightTool());
        viewComponent.addToolButton(ConeLightTool.NAME, toolList.get(ConeLightTool.NAME));
        toolList.put(PanTool.NAME, new PanTool());
    }

    public void addTool(String toolName, VisImageButton.VisImageButtonStyle toolBtnStyle, boolean addSeparator, Tool tool) {
        toolList.put(toolName, tool);
        if(addSeparator) {
            viewComponent.add(new Separator("tool")).padTop(2).padBottom(2).fill().expand().row();
        }
        viewComponent.addToolButton(toolName, toolBtnStyle, tool);

        facade.sendNotification(toolName, tool);
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.TOOL_CLICKED,
                SandboxMediator.SANDBOX_TOOL_CHANGED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case MsgAPI.TOOL_CLICKED:
                currentTool = notification.getBody();
                facade.sendNotification(MsgAPI.TOOL_SELECTED, currentTool);
                break;
            case SandboxMediator.SANDBOX_TOOL_CHANGED:
                if (notification.getBody() instanceof Tool) {
                    currentTool = ((Tool) notification.getBody()).getName();
                } else {
                    currentTool = notification.getBody();
                }

                setCurrentTool(currentTool);
                break;
        }
    }

    public void setCurrentTool(String tool) {
        viewComponent.setCurrentTool(tool);
        currentTool = tool;
    }

    public HashMap<String, Tool> getToolList() {
        return toolList;
    }
}
