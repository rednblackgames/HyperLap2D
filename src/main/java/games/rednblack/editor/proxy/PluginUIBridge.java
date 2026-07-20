package games.rednblack.editor.proxy;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.kotcrab.vis.ui.widget.VisImageButton;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.h2d.common.view.tools.Tool;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.IMediator;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Seam between {@link PluginManager} (a proxy that must not depend on the view
 * layer) and the view-side mediators/{@code Sandbox} it used to reach into
 * directly. The view provides a single implementation, registered as the
 * mediator named by {@link #MEDIATOR_NAME}; {@code PluginManager} retrieves it
 * in {@code onRegister} and delegates every plugin-facing operation that touches
 * the UI through it.
 *
 * This is editor-internal (not part of the plugin-facing
 * {@code games.rednblack.h2d.common.plugins.PluginAPI} surface) — plugins still
 * call {@code PluginAPI}; only its implementation changes.
 */
public interface PluginUIBridge {

    /** Name under which the view-side {@code PluginUIBridgeMediator} is registered. */
    String MEDIATOR_NAME = "games.rednblack.editor.view.PluginUIBridgeMediator";

    Stage getUIStage();

    SceneLoader getSceneLoader();

    HashSet<Integer> getProjectEntities();

    void loadCurrentProject();

    SceneVO sceneVoFromItems();

    void showPopup(HashMap<String, String> actionsSet, Object observable);

    void setDropDownItemName(String action, String name);

    void addMenuItem(String menu, String subMenuName, String notificationName);

    void addTool(String toolName, VisImageButton.VisImageButtonStyle style, boolean addSeparator, Tool tool);

    void toolHotSwap(Tool tool);

    void toolHotSwapBack();

    String getCurrentSelectedLayerName();

    SceneVO getCurrentSceneVO();

    static PluginUIBridge get(Facade facade) {
        IMediator m = facade.retrieveMediator(MEDIATOR_NAME);
        return (PluginUIBridge) m;
    }
}