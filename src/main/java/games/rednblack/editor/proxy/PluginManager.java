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

package games.rednblack.editor.proxy;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisImageButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.PluginItemCommand;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.menu.HyperLap2DMenuBarMediator;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.UIDropDownMenu;
import games.rednblack.editor.view.ui.UIDropDownMenuMediator;
import games.rednblack.editor.view.ui.box.UILayerBoxMediator;
import games.rednblack.editor.view.ui.box.UIToolBoxMediator;
import games.rednblack.h2d.common.IItemCommand;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.factory.IFactory;
import games.rednblack.h2d.common.plugins.H2DPlugin;
import games.rednblack.h2d.common.plugins.PluginAPI;
import games.rednblack.h2d.common.proxy.CursorManager;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.h2d.common.vo.CursorData;
import games.rednblack.h2d.common.vo.EditorConfigVO;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.h2d.common.vo.SceneConfigVO;
import org.puremvc.java.interfaces.IFacade;
import org.puremvc.java.patterns.proxy.Proxy;

import java.util.*;

/**
 * Created by azakhary on 7/24/2015.
 */
public class PluginManager extends Proxy implements PluginAPI {
    private static final String TAG = PluginManager.class.getCanonicalName();
    public static final String NAME = TAG;

    private ArrayList<H2DPlugin> plugins = new ArrayList<>();
    private String pluginDir, cacheDir;

    private HashSet<Integer> pluginEntities;

    public PluginManager() {
        super(NAME);
        facade = HyperLap2DFacade.getInstance();
    }

    public H2DPlugin registerPlugin(H2DPlugin plugin) {
        plugins.add(plugin);

        return plugin;
    }

    public void initPlugin(H2DPlugin plugin) {
        if(plugins.contains(plugin)) return;

        registerPlugin(plugin);
        plugin.setAPI(this);
        plugin.initPlugin();
    }

    public void dropDownActionSets(Set<Integer> selectedEntities, Array<String> actionsSet) {
        for(H2DPlugin plugin: plugins) {
            plugin.onDropDownOpen(selectedEntities, actionsSet);
        }
    }

    public void setDropDownItemName(String action, String name) {
        UIDropDownMenuMediator dropDownMenuMediator = facade.retrieveMediator(UIDropDownMenuMediator.NAME);
        dropDownMenuMediator.getViewComponent().setActionName(action, name);
    }

    @Override
    public String getProjectPath() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectPath();
    }

    @Override
    public TextureAtlas.AtlasRegion getProjectTextureRegion(String regionName) {
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        return (TextureAtlas.AtlasRegion) resourceManager.getTextureRegion(regionName);
    }

    @Override
    public void reLoadProject() {
        Sandbox sandbox = Sandbox.getInstance();
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        projectManager.openProjectAndLoadAllData(projectManager.getCurrentProjectPath());
        sandbox.loadCurrentProject();
        facade.sendNotification(ProjectManager.PROJECT_DATA_UPDATED);
    }

    @Override
    public void saveProject() {
        Sandbox sandbox = Sandbox.getInstance();
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        SceneVO vo = sandbox.sceneVoFromItems();
        sceneDataManager.saveScene(vo);
    }

    @Override
    public void revertibleCommand(IItemCommand command, Object body) {
        Object payload = PluginItemCommand.build(command, body);
        facade.sendNotification(MsgAPI.ACTION_PLUGIN_PROXY_COMMAND, payload);
    }

    @Override
    public void removeFollower(int entity) {
        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        followersUIMediator.removeFollower(entity);
    }

    public void addMenuItem(String menu, String subMenuName, String notificationName) {
        HyperLap2DMenuBarMediator hyperlap2DMenuBarMediator = facade.retrieveMediator(HyperLap2DMenuBarMediator.NAME);
        hyperlap2DMenuBarMediator.addMenuItem(menu, subMenuName, notificationName);
    }

    @Override
    public void addTool(String toolName, VisImageButton.VisImageButtonStyle toolBtnStyle, boolean addSeparator, Tool tool) {
        UIToolBoxMediator uiToolBoxMediator = facade.retrieveMediator(UIToolBoxMediator.NAME);
        uiToolBoxMediator.addTool(toolName, toolBtnStyle, addSeparator, tool);
        Map.Entry<String, Tool> toolPair = new Map.Entry<String, Tool>() {
            @Override
            public String getKey() {
                return toolName;
            }

            @Override
            public Tool getValue() {
                return tool;
            }

            @Override
            public Tool setValue(Tool value) {
                Tool old = getValue();
                setValue(value);
                return old;
            }
        };
        facade.sendNotification(MsgAPI.NEW_TOOL_ADDED, toolPair);
    }

    @Override
    public void toolHotSwap(Tool tool) {
        SandboxMediator sandboxMediator = facade.retrieveMediator(SandboxMediator.NAME);
        sandboxMediator.toolHotSwap(tool);
    }

    @Override
    public void toolHotSwapBack() {
        SandboxMediator sandboxMediator = facade.retrieveMediator(SandboxMediator.NAME);
        sandboxMediator.toolHotSwapBack();
    }

    public void setPluginDir(String pluginDir) {
        this.pluginDir = pluginDir;
    }


    @Override
    public String getPluginDir() {
        return pluginDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    @Override
    public String getCacheDir() {
        return cacheDir;
    }

    @Override
    public SceneLoader getSceneLoader() {
        return Sandbox.getInstance().getSceneControl().sceneLoader;
    }

    @Override
    public IFacade getFacade() {
        return facade;
    }

    @Override
    public com.artemis.World getEngine() {
        return getSceneLoader().getEngine();
    }

    @Override
    public Stage getUIStage() {
        return Sandbox.getInstance().getUIStage();
    }

    @Override
    public IFactory getItemFactory() {
        return ItemFactory.get();
    }

    public boolean isEntityVisible(int e) {
        LayerItemVO layer = EntityUtils.getEntityLayer(e);
        return layer != null && layer.isVisible;
    }

    @Override
    public HashSet<Integer> getProjectEntities() {
        Sandbox sandbox = Sandbox.getInstance();
        return sandbox.getSelector().getAllFreeItems();
    }

    @Override
    public void showPopup(HashMap<String, String> actionsSet, Object observable) {
        UIDropDownMenu uiDropDownMenu = new UIDropDownMenu();
        actionsSet.entrySet().forEach(entry -> uiDropDownMenu.setActionName(entry.getKey(), entry.getValue()));

        Array<String> actions = new Array<>();
        actionsSet.keySet().forEach(key -> actions.add(key));
        uiDropDownMenu.setActionList(actions);

        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        uiDropDownMenu.setX(sandbox.getInputX());
        uiDropDownMenu.setY(uiStage.getHeight() - sandbox.getInputY() - uiDropDownMenu.getHeight());
        getUIStage().addActor(uiDropDownMenu);

        UIDropDownMenuMediator dropDownMenuMediator = facade.retrieveMediator(UIDropDownMenuMediator.NAME);
        dropDownMenuMediator.setCurrentObservable(observable);
    }

    @Override
    public void setCursor(CursorData cursorData, TextureRegion region) {
        CursorManager cursorManager = HyperLap2DFacade.getInstance().retrieveProxy(CursorManager.NAME);
        cursorManager.setCursor(cursorData, region);
    }

    @Override
    public String getCurrentSelectedLayerName() {
        UILayerBoxMediator uiLayerBoxMediator = facade.retrieveMediator(UILayerBoxMediator.NAME);
        return uiLayerBoxMediator.getViewComponent().getCurrentSelectedLayer().getLayerName();
    }

    @Override
    public EditorConfigVO getEditorConfig() {
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        return settingsManager.editorConfigVO;
    }

    @Override
    public SceneConfigVO getCurrentSceneConfigVO() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentSceneConfigVO();
    }

    @Override
    public ProjectInfoVO getCurrentProjectInfoVO() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectInfoVO();
    }

    @Override
    public ProjectVO getCurrentProjectVO() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectVO();
    }
}
