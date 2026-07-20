package games.rednblack.editor.view;
import games.rednblack.editor.proxy.SelectionProxy;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisImageButton;
import games.rednblack.editor.proxy.PluginUIBridge;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.view.menu.HyperLap2DMenuBarMediator;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.UIDropDownMenu;
import games.rednblack.editor.view.ui.UIDropDownMenuMediator;
import games.rednblack.editor.view.ui.box.UIToolBoxMediator;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.puremvc.Mediator;

import java.util.HashMap;
import java.util.HashSet;

/**
 * View-side implementation of {@link PluginUIBridge}. Owns all the coupling to
 * {@link Sandbox} and the UI mediators that {@link games.rednblack.editor.proxy.PluginManager}
 * used to reach into directly, so the proxy no longer imports the view layer.
 * Registered in {@code BootstrapViewCommand} under {@link PluginUIBridge#MEDIATOR_NAME}
 * before plugins are loaded.
 */
public class PluginUIBridgeMediator extends Mediator<Object> implements PluginUIBridge {

    public PluginUIBridgeMediator() {
        super(PluginUIBridge.MEDIATOR_NAME, null);
    }

    @Override
    public Stage getUIStage() {
        return Sandbox.getInstance().getUIStage();
    }

    @Override
    public SceneLoader getSceneLoader() {
        return Sandbox.getInstance().getSceneControl().sceneLoader;
    }

    @Override
    public HashSet<Integer> getProjectEntities() {
        return SelectionProxy.get(facade).getAllFreeItems();
    }

    @Override
    public void loadCurrentProject() {
        Sandbox.getInstance().loadCurrentProject();
    }

    @Override
    public SceneVO sceneVoFromItems() {
        return Sandbox.getInstance().sceneVoFromItems();
    }

    @Override
    public void showPopup(HashMap<String, String> actionsSet, Object observable) {
        UIDropDownMenu uiDropDownMenu = new UIDropDownMenu();
        actionsSet.entrySet().forEach(entry -> uiDropDownMenu.setActionName(entry.getKey(), entry.getValue()));

        Array<String> actions = new Array<>();
        actionsSet.keySet().forEach(actions::add);
        uiDropDownMenu.setActionList(actions);

        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        uiDropDownMenu.setX(sandbox.getInputX());
        uiDropDownMenu.setY(uiStage.getHeight() - sandbox.getInputY() - uiDropDownMenu.getHeight());
        uiStage.addActor(uiDropDownMenu);

        UIDropDownMenuMediator dropDownMenuMediator = facade.retrieveMediator(UIDropDownMenuMediator.NAME);
        dropDownMenuMediator.setCurrentObservable(observable);
    }

    @Override
    public void setDropDownItemName(String action, String name) {
        UIDropDownMenuMediator dropDownMenuMediator = facade.retrieveMediator(UIDropDownMenuMediator.NAME);
        dropDownMenuMediator.getViewComponent().setActionName(action, name);
    }

    @Override
    public void addMenuItem(String menu, String subMenuName, String notificationName) {
        HyperLap2DMenuBarMediator hyperlap2DMenuBarMediator = facade.retrieveMediator(HyperLap2DMenuBarMediator.NAME);
        hyperlap2DMenuBarMediator.addMenuItem(menu, subMenuName, notificationName);
    }

    @Override
    public void addTool(String toolName, VisImageButton.VisImageButtonStyle style, boolean addSeparator, Tool tool) {
        UIToolBoxMediator uiToolBoxMediator = facade.retrieveMediator(UIToolBoxMediator.NAME);
        uiToolBoxMediator.addTool(toolName, style, addSeparator, tool);
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

    @Override
    public SceneVO getCurrentSceneVO() {
        return Sandbox.getInstance().getSceneControl().getCurrentSceneVO();
    }

    @Override
    public String getCurrentSelectedLayerName() {
        String layerName = games.rednblack.editor.proxy.LayerSelectionProxy.get(facade).getCurrentLayerName();
        return layerName;
    }
}