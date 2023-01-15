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

import com.artemis.BaseComponentMapper;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import com.kotcrab.vis.ui.FocusManager;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddComponentToItemCommand;
import games.rednblack.editor.controller.commands.CompositeCameraChangeCommand;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.input.EntityClickListener;
import games.rednblack.editor.view.stage.input.InputListenerComponent;
import games.rednblack.editor.view.stage.tools.PanTool;
import games.rednblack.editor.view.stage.tools.SelectionTool;
import games.rednblack.editor.view.stage.tools.TransformTool;
import games.rednblack.editor.view.ui.box.UIToolBoxMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.tools.Tool;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

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

    private Tool hotSwapMemory;

    private HashMap<String, Tool> sandboxTools;
    private Tool currentSelectedTool;

    private static final Vector3 temp = new Vector3();
    private static final Vector2 tmp = new Vector2();

    public SandboxMediator() {
        super(NAME, Sandbox.getInstance());
    }

    @Override
    public void onRegister() {
        super.onRegister();

        facade = HyperLap2DFacade.getInstance();

        stageListener = new SandboxStageEventListener();
        getViewComponent().addListener(stageListener);

        initTools();
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
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.SCENE_LOADED,
                MsgAPI.TOOL_SELECTED,
                MsgAPI.NEW_ITEM_ADDED,
                MsgAPI.NEW_TOOL_ADDED,
                MsgAPI.RESIZE,
                MsgAPI.DISPOSE,
                CompositeCameraChangeCommand.DONE,
                AddComponentToItemCommand.DONE,
                RemoveComponentFromItemCommand.DONE,
                MsgAPI.ITEM_SELECTION_CHANGED,
                PanTool.SCENE_PANNED
        };
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
        int rootEntity = Sandbox.getInstance().getCurrentViewingEntity();
        NodeComponent nodeComponent = SandboxComponentRetriever.get(rootEntity, NodeComponent.class);
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
        inputListenerComponent.addListener(new SandboxItemEventListener(entity));
    }

    public Vector2 getStageCoordinates() {
        Vector3 vec = temp.set(Gdx.input.getX(), Gdx.input.getY(), 1);
        viewComponent.getCamera().unproject(vec);

        return tmp.set(vec.x, vec.y);
    }

    public class SandboxItemEventListener extends EntityClickListener {

        public SandboxItemEventListener(final int entity) {
        	
        }

        @Override
        public boolean touchDown(int entity, float x, float y, int pointer, int button) {
            super.touchDown(entity, x, y, pointer, button);

            setSandboxFocus();

            switch (button) {
                case Input.Buttons.MIDDLE:
                    // if middle button is pressed - PAN the scene
                    toolHotSwap(sandboxTools.get(PanTool.NAME));
                    break;
            }

            Vector2 coords = getStageCoordinates();
            return currentSelectedTool != null && currentSelectedTool.itemMouseDown(entity, coords.x, coords.y);
        }

        
        @Override
        public void touchUp(int entity, float x, float y, int pointer, int button) {
            super.touchUp(entity, x, y, pointer, button);
            Vector2 coords = getStageCoordinates();

            if (button == Input.Buttons.MIDDLE) {
                toolHotSwapBack();
            }

            if (currentSelectedTool != null) {
                currentSelectedTool.itemMouseUp(entity, x, y);

                if (getTapCount() == 2) {
                    // this is double click
                    currentSelectedTool.itemMouseDoubleClick(entity, coords.x, coords.y);
                }
            }

            if (button == Input.Buttons.RIGHT) {
                // if right clicked on an item, drop down for current selection
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_RIGHT_CLICK);
            }
        }

        @Override
        public void touchDragged(int entity, float x, float y, int pointer) {
            Vector2 coords = getStageCoordinates();

            if (currentSelectedTool != null) {
                currentSelectedTool.itemMouseDragged(entity, coords.x, coords.y);
            }
        }

        @Override
        public boolean scrolled(int entity, float amountX, float amountY) {

            return false;
        }
    }

    private class SandboxStageEventListener extends EntityClickListener {
        public SandboxStageEventListener() {
            setTapCountInterval(.5f);
        }

        @Override
        public boolean keyDown(int entity, int keycode) {
            Sandbox sandbox = Sandbox.getInstance();
            if (sandbox.sceneControl.getCurrentSceneVO() == null) {
                return false;
            }

            facade.sendNotification(MsgAPI.ACTION_KEY_DOWN, keycode);

            if(currentSelectedTool != null) {
                currentSelectedTool.keyDown(entity, keycode);
            }

            switch (KeyBindingsLayout.mapAction(keycode)) {
                case KeyBindingsLayout.SELECTION_TOOL:
                    facade.sendNotification(MsgAPI.TOOL_CLICKED, SelectionTool.NAME);
                    break;
                case KeyBindingsLayout.TRANSFORM_TOOL:
                    facade.sendNotification(MsgAPI.TOOL_CLICKED, TransformTool.NAME);
                    break;
                case KeyBindingsLayout.PAN_TOOL:
                    toolHotSwap(sandboxTools.get(PanTool.NAME));
                    break;
                case KeyBindingsLayout.ZOOM_PLUS:
                    sandbox.zoomDivideBy(2f);
                    break;
                case KeyBindingsLayout.ZOOM_MINUS:
                    sandbox.zoomDivideBy(0.5f);
                    break;
                case KeyBindingsLayout.Z_INDEX_UP:
                    // going to front of next item in z-index ladder
                    sandbox.itemControl.itemZIndexChange(sandbox.getSelector().getCurrentSelection(), true);
                    facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, sandbox.getSelector().getCurrentSelection());
                    break;
                case KeyBindingsLayout.Z_INDEX_DOWN:
                    // going behind the next item in z-index ladder
                    sandbox.itemControl.itemZIndexChange(sandbox.getSelector().getCurrentSelection(), false);
                    facade.sendNotification(MsgAPI.ACTION_Z_INDEX_CHANGED, sandbox.getSelector().getCurrentSelection());
                    break;
                case KeyBindingsLayout.SELECT_ALL:
                    // Ctrl+A means select all
                    facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, sandbox.getSelector().getAllFreeItems());
                    break;
                case KeyBindingsLayout.COPY:
                    facade.sendNotification(MsgAPI.ACTION_COPY);
                    break;
                case KeyBindingsLayout.CUT:
                    facade.sendNotification(MsgAPI.ACTION_CUT);
                    break;
                case KeyBindingsLayout.PASTE:
                    facade.sendNotification(MsgAPI.ACTION_PASTE);
                    break;
                case KeyBindingsLayout.UNDO:
                    CommandManager commandManager = facade.retrieveProxy(CommandManager.NAME);
                    commandManager.undoCommand();
                    break;
                case KeyBindingsLayout.REDO:
                    commandManager = facade.retrieveProxy(CommandManager.NAME);
                    commandManager.redoCommand();
                    break;
                case KeyBindingsLayout.RESET_CAMERA:
                    sandbox.getCamera().position.set(0 ,0, 0);
                    sandbox.setZoomPercent(100, false);
                    break;
                case KeyBindingsLayout.ALIGN_TOP:
                    sandbox.getSelector().alignSelections(Align.top);
                    break;
                case KeyBindingsLayout.ALIGN_LEFT:
                    sandbox.getSelector().alignSelections(Align.left);
                    break;
                case KeyBindingsLayout.ALIGN_BOTTOM:
                    sandbox.getSelector().alignSelections(Align.bottom);
                    break;
                case KeyBindingsLayout.ALIGN_RIGHT:
                    sandbox.getSelector().alignSelections(Align.right);
                    break;
            }

            if (keycode == Input.Keys.ESCAPE) {
                if (sandbox.getSelector().getSelectedItems().size() > 0) {
                    facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, null);
                } else {
                    currentSelectedTool.stageMouseDoubleClick(0, 0);
                }
            }
            return true;
        }

        @Override
        public boolean keyUp(int entity, int keycode) {
            facade.sendNotification(MsgAPI.ACTION_KEY_UP, keycode);

            Sandbox sandbox = Sandbox.getInstance();
            switch (KeyBindingsLayout.mapAction(keycode)) {
                case KeyBindingsLayout.PAN_TOOL:
                    // if pan mode is disabled set cursor back
                    toolHotSwapBack();
                    break;
                case KeyBindingsLayout.DELETE:
                    // delete selected item
                    sandbox.getSelector().removeCurrentSelectedItems();
                    break;
            }

            if(currentSelectedTool != null) {
                currentSelectedTool.keyUp(entity, keycode);
            }

            return true;
        }


        @Override
        public boolean touchDown(int entity, float x, float y, int pointer, int button) {
            super.touchDown(entity, x, y, pointer, button);

            setSandboxFocus();

            switch (button) {
                case Input.Buttons.MIDDLE:
                    // if middle button is pressed - PAN the scene
                    toolHotSwap(sandboxTools.get(PanTool.NAME));
                    break;
            }

            if (currentSelectedTool != null) {
                currentSelectedTool.stageMouseDown(x, y);
            }

            return true;
        }

        @Override
        public void touchUp(int entity, float x, float y, int pointer, int button) {
            super.touchUp(entity, x, y, pointer, button);

            if(currentSelectedTool != null) {
                currentSelectedTool.stageMouseUp(x, y);
            }

            Sandbox sandbox = Sandbox.getInstance();
            if (button == Input.Buttons.RIGHT) {
                // if clicked on empty space, selections need to be cleared
                sandbox.getSelector().clearSelections();

                // show default dropdown
                facade.sendNotification(MsgAPI.SCENE_RIGHT_CLICK, new Vector2(x, y));

                return;
            }

            if (button == Input.Buttons.MIDDLE) {
                toolHotSwapBack();
            }

            if (getTapCount() == 2 && button == Input.Buttons.LEFT) {
                doubleClick(entity, x, y);
            }

        }

        private void doubleClick(int entity, float x, float y) {
            if (currentSelectedTool != null) {
                Sandbox sandbox = Sandbox.getInstance();
                currentSelectedTool.stageMouseDoubleClick(x, y);
            }
        }

        @Override
        public void touchDragged(int entity, float x, float y, int pointer) {
            if (currentSelectedTool != null) {
                Sandbox sandbox = Sandbox.getInstance();
                currentSelectedTool.stageMouseDragged(x, y);
            }
        }


        @Override
        public boolean scrolled(int entity, float amountX, float amountY) {
            Sandbox sandbox = Sandbox.getInstance();
            // well, duh
            if (amountX == 0 && amountY == 0) return false;

            // Control pressed as well
            if (isControlPressed()) {
                float zoomPercent = sandbox.getZoomPercent();
                zoomPercent-= amountY * 4f;
                if(zoomPercent < 5 ) zoomPercent = 5;

                sandbox.setZoomPercent(zoomPercent, true);
            } else {
                if (currentSelectedTool != null
                        && !currentSelectedTool.stageMouseScrolled(amountX, amountY)) {
                    float scale = 30f / sandbox.getPixelPerWU();
                    viewComponent.panSceneBy(amountX * scale, -amountY * scale);
                }
            }

            return false;
        }

        private boolean isControlPressed() {
            return Gdx.input.isKeyPressed(Input.Keys.SYM)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        }
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

    private void setSandboxFocus() {
       Sandbox sandbox = Sandbox.getInstance();
        FocusManager.resetFocus(sandbox.getUIStage());

        // setting key and scroll focus on main area
        sandbox.getUIStage().setKeyboardFocus();
        sandbox.getUIStage().setScrollFocus(sandbox.getUIStage().midUI);
        sandbox.setKeyboardFocus();
    }
}
