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

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddComponentToItemCommand;
import games.rednblack.editor.controller.commands.CompositeCameraChangeCommand;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.stage.input.EntityClickListener;
import games.rednblack.editor.view.stage.input.InputListenerComponent;
import games.rednblack.editor.view.stage.tools.*;
import games.rednblack.editor.view.ui.box.UIToolBoxMediator;
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
            default:
                break;
        }
        if(currentSelectedTool != null) {
            currentSelectedTool.handleNotification(notification);
        }
    }

    private void handleSceneLoaded(INotification notification) {
        initItemListeners();

        setCurrentTool(SelectionTool.NAME);
    }

    private void initItemListeners() {
        Engine engine = getViewComponent().getEngine();
        Family rootFamily = Family.all(ViewPortComponent.class).get();
        Entity rootEntity = engine.getEntitiesFor(rootFamily).iterator().next();
        NodeComponent nodeComponent = ComponentRetriever.get(rootEntity, NodeComponent.class);
        SnapshotArray<Entity> childrenEntities = nodeComponent.children;

        for (Entity child: childrenEntities) {
            addListenerToItem(child);
        }
    }

    /**
     * TODO: this can be changed, as in ideal world entity factory should be adding listener component to ALL entities,
     * problem is currently this component is not part of runtime. but it will be.
     *
     * @param entity
     */
    private void addListenerToItem(Entity entity) {
        InputListenerComponent inputListenerComponent = entity.getComponent(InputListenerComponent.class);
        if(inputListenerComponent == null){
            inputListenerComponent = new InputListenerComponent();
            entity.add(inputListenerComponent);
        }
        inputListenerComponent.removeAllListener();
        inputListenerComponent.addListener(new SandboxItemEventListener(entity));
    }

    public Vector2 getStageCoordinates() {
        // TODO: remove this shit
        Engine engine = getViewComponent().getEngine();
        Family rootFamily = Family.all(ViewPortComponent.class).get();
        Entity rootEntity = engine.getEntitiesFor(rootFamily).iterator().next();

        ViewPortComponent viewPortComponent = ComponentRetriever.get(rootEntity, ViewPortComponent.class);
        Vector2 vec = new Vector2(Gdx.input.getX(), Gdx.input.getY());
        viewPortComponent.viewPort.unproject(vec);

        return vec;
    }

    public class SandboxItemEventListener extends EntityClickListener {

        public SandboxItemEventListener(final Entity entity) {
        	
        }

        @Override
        public boolean touchDown(Entity entity, float x, float y, int pointer, int button) {
            super.touchDown(entity, x, y, pointer, button);

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
        public void touchUp(Entity entity, float x, float y, int pointer, int button) {
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
        public void touchDragged(Entity entity, float x, float y, int pointer) {
            Vector2 coords = getStageCoordinates();

            if (currentSelectedTool != null) {
                currentSelectedTool.itemMouseDragged(entity, coords.x, coords.y);
            }
        }

        @Override
        public boolean scrolled(Entity entity, int amount) {

            return false;
        }
    }

    private class SandboxStageEventListener extends EntityClickListener {
        public SandboxStageEventListener() {
            setTapCountInterval(.5f);
        }

        @Override
        public boolean keyDown(Entity entity, int keycode) {
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
                currentSelectedTool.stageMouseDoubleClick(0, 0);
            }
            return true;
        }

        @Override
        public boolean keyUp(Entity entity, int keycode) {
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
        public boolean touchDown(Entity entity, float x, float y, int pointer, int button) {
            super.touchDown(entity, x, y, pointer, button);

            Sandbox sandbox = Sandbox.getInstance();

            // setting key and scroll focus on main area
            sandbox.getUIStage().setKeyboardFocus();
            sandbox.getUIStage().setScrollFocus(sandbox.getUIStage().midUI);
            sandbox.setKeyboardFocus();


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
        public void touchUp(Entity entity, float x, float y, int pointer, int button) {
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

        private void doubleClick(Entity entity, float x, float y) {
            if (currentSelectedTool != null) {
                Sandbox sandbox = Sandbox.getInstance();
                currentSelectedTool.stageMouseDoubleClick(x, y);
            }
        }

        @Override
        public void touchDragged(Entity entity, float x, float y, int pointer) {
            if (currentSelectedTool != null) {
                Sandbox sandbox = Sandbox.getInstance();
                currentSelectedTool.stageMouseDragged(x, y);
            }
        }


        @Override
        public boolean scrolled(Entity entity, int amount) {
            Sandbox sandbox = Sandbox.getInstance();
            // well, duh
            if (amount == 0) return false;

            // Control pressed as well
            if (isControlPressed()) {
                float zoomPercent = sandbox.getZoomPercent();
                zoomPercent-=amount*4f;
                if(zoomPercent < 5 ) zoomPercent = 5;

                sandbox.setZoomPercent(zoomPercent, true);
            }

            if (currentSelectedTool != null) {
                currentSelectedTool.stageMouseScrolled(amount);
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
}
