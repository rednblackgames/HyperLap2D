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

package games.rednblack.editor.view.stage;
import games.rednblack.editor.proxy.EntityDataProxy;

import games.rednblack.editor.renderer.ecs.BaseComponentMapper;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.FocusManager;
import games.rednblack.editor.controller.commands.AddComponentToItemCommand;
import games.rednblack.editor.controller.commands.CompositeCameraChangeCommand;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.view.stage.input.InputListenerComponent;
import games.rednblack.editor.view.stage.tools.PanTool;
import games.rednblack.editor.view.stage.tools.SelectionTool;
import games.rednblack.editor.view.ui.box.UIToolBoxMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

import java.util.HashMap;

/**
 * Created by sargis on 4/20/15.
 */
public class SandboxMediator extends Mediator<Sandbox> {
    private static final String TAG = SandboxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private static final String PREFIX =  "games.rednblack.editor.view.stage.SandboxStageMediator";
    public static final String SANDBOX_TOOL_CHANGED = PREFIX + ".SANDBOX_TOOL_CHANGED";

    private SandboxStageEventListener stageListener;

    Tool hotSwapMemory;

    HashMap<String, Tool> sandboxTools;
    Tool currentSelectedTool;

    private static final Vector3 temp = new Vector3();
    private static final Vector2 tmp = new Vector2();

    SettingsManager settingsManager;

    public SandboxMediator() {
        super(NAME, Sandbox.getInstance());
    }

    @Override
    public void onRegister() {
        super.onRegister();

        stageListener = new SandboxStageEventListener(this);
        getViewComponent().addListener(stageListener);

        initTools();

        settingsManager = facade.retrieveProxy(SettingsManager.NAME);
    }

    private void initTools() {
        UIToolBoxMediator toolBoxMediator = facade.retrieveMediator(UIToolBoxMediator.NAME);
        sandboxTools = toolBoxMediator.getToolList();
    }

    private void setCurrentTool(String toolName) {
        currentSelectedTool = sandboxTools.get(toolName);

        if (currentSelectedTool != null) {
            facade.sendNotification(SANDBOX_TOOL_CHANGED, currentSelectedTool);
            currentSelectedTool.initTool();
        }
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.SCENE_LOADED,
                MsgAPI.TOOL_SELECTED,
                MsgAPI.NEW_ITEM_ADDED,
                MsgAPI.NEW_TOOL_ADDED);
        interests.add(MsgAPI.RESIZE,
                MsgAPI.DISPOSE,
                CompositeCameraChangeCommand.DONE,
                AddComponentToItemCommand.DONE);
        interests.add(RemoveComponentFromItemCommand.DONE,
                MsgAPI.ITEM_SELECTION_CHANGED,
                PanTool.SCENE_PANNED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                handleSceneLoaded(notification);
                break;
            case MsgAPI.TOOL_SELECTED:
                setCurrentTool(notification.getBody());
                break;
            case MsgAPI.NEW_ITEM_ADDED:
                addListenerToItem(notification.getBody());
                break;
            case CompositeCameraChangeCommand.DONE:
                initItemListeners();
                break;
            case PanTool.SCENE_PANNED:
                viewComponent.scenePanned();
                break;
            case MsgAPI.RESIZE:
                int[] data = notification.getBody();
                viewComponent.resize(data[0], data[1]);
                facade.sendNotification(MsgAPI.UPDATE_ALL_FOLLOWERS);
                break;
            case MsgAPI.DISPOSE:
                viewComponent.dispose();
                break;
            default:
                break;
        }
        if(currentSelectedTool != null) {
            currentSelectedTool.handleNotification(notification);
        }
    }

    private void handleSceneLoaded(INotification notification) {
        initItemListeners();

        facade.sendNotification(MsgAPI.TOOL_CLICKED, SelectionTool.NAME);
    }

    private void initItemListeners() {
        int rootEntity = getViewComponent().getCurrentViewingEntity();
        NodeComponent nodeComponent = EntityDataProxy.get().get(rootEntity, NodeComponent.class);
        SnapshotArray<Integer> childrenEntities = nodeComponent.children;

        for (int child: childrenEntities) {
            addListenerToItem(child);
        }
    }

    /**
     * TODO: this can be changed, as in ideal world entity factory should be adding listener component to ALL entities,
     * problem is currently this component is not part of runtime. but it will be.
     *
     * @param entity
     */
    private void addListenerToItem(int entity) {
        BaseComponentMapper<InputListenerComponent> mapper = ComponentMapper.getFor(InputListenerComponent.class, getViewComponent().getEngine());
        InputListenerComponent inputListenerComponent = mapper.get(entity);
        if(inputListenerComponent == null){
            inputListenerComponent = getViewComponent().getEngine().edit(entity).create(InputListenerComponent.class);
        }
        inputListenerComponent.removeAllListener();
        inputListenerComponent.addListener(new SandboxItemEventListener(this, entity));
    }

    public Facade getFacade() {
        return facade;
    }

    public Vector2 getStageCoordinates() {
        Vector3 vec = temp.set(Gdx.input.getX(), Gdx.input.getY(), 1);
        viewComponent.getCamera().unproject(vec);

        return tmp.set(vec.x, vec.y);
    }


    public void toolHotSwap(Tool tool) {
        if (currentSelectedTool == null || currentSelectedTool.getName().equals(tool.getName()))
            return;

        hotSwapMemory = currentSelectedTool;
        currentSelectedTool = tool;
        currentSelectedTool.initTool();
    }

    public void toolHotSwapBack() {
        if (hotSwapMemory == null)
            return;

        currentSelectedTool = hotSwapMemory;
        hotSwapMemory = null;
        currentSelectedTool.initTool();
    }

    public String getCurrentSelectedToolName() {
        return currentSelectedTool != null ? currentSelectedTool.getName() : "";
    }

    void setSandboxFocus() {
       Sandbox sandbox = getViewComponent();
        FocusManager.resetFocus(sandbox.getUIStage());

        // setting key and scroll focus on main area
        sandbox.getUIStage().setKeyboardFocus();
        sandbox.getUIStage().setScrollFocus(sandbox.getUIStage().midUI);
        sandbox.setKeyboardFocus();
    }
}
