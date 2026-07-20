package games.rednblack.editor.view.stage;

import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.puremvc.Facade;

/**
 * Owns scene-loading orchestration, extracted from {@link Sandbox} (Phase 2
 * decomposition). {@code Sandbox} holds an instance and delegates
 * {@code loadCurrentProject/loadScene/sceneVoFromItems} to it. The service calls
 * back into {@code Sandbox} for the cross-cutting pieces that stay there
 * (initView, current viewing entity, camera/zoom, sceneConfigVO).
 */
public class SceneLoadService {

    private final Sandbox sandbox;
    private final ProjectManager projectManager;
    private final Facade facade;

    public SceneLoadService(Sandbox sandbox, ProjectManager projectManager, Facade facade) {
        this.sandbox = sandbox;
        this.projectManager = projectManager;
        this.facade = facade;
    }

    public void loadCurrentProject() {
        ProjectVO projectVO = projectManager.getCurrentProjectVO();
        loadScene(projectVO.lastOpenScene.isEmpty() ? "MainScene" : projectVO.lastOpenScene);
    }

    public void loadScene(String sceneName) {
        sandbox.currentLoadedSceneFileName = sceneName;

        sandbox.getSceneControl().initScene(sceneName);

        sandbox.initView();

        ProjectVO projectVO = projectManager.getCurrentProjectVO();
        projectVO.lastOpenScene = sceneName;
        projectManager.saveCurrentProject();

        facade.sendNotification(MsgAPI.LIBRARY_LIST_UPDATED);
        facade.sendNotification(MsgAPI.LIBRARY_ACTIONS_UPDATED);

        sandbox.setCurrentViewingEntity(sandbox.getRootEntity());

        sandbox.sceneConfigVO = projectManager.getCurrentSceneConfigVO();
        sandbox.getCamera().position.set(sandbox.sceneConfigVO.cameraPosition[0], sandbox.sceneConfigVO.cameraPosition[1], 0);
        sandbox.setZoomPercent(sandbox.sceneConfigVO.cameraZoom, false);
        projectManager.changeSceneWindowTitle();

        //TODO: move this into SceneDataManager!
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        sceneDataManager.sendNotification(MsgAPI.SCENE_LOADED);

        CommandManager commandManager = facade.retrieveProxy(CommandManager.NAME);
        commandManager.initHistory();
    }

    /**
     * When an entity is modified their respective VO in memory are not touched, so to save a scene we have to
     * recreate all current SceneVO from root entity state
     *
     * TODO This does not seems to be much smart
     *
     * @return SceneVO
     */
    public SceneVO sceneVoFromItems() {
        CompositeItemVO newVo = new CompositeItemVO();
        newVo.loadFromEntity(sandbox.getRootEntity(), sandbox.getEngine(), sandbox.getSceneControl().sceneLoader.getEntityFactory());
        newVo.sStickyNotes.putAll(sandbox.getSceneControl().getCurrentSceneVO().composite.sStickyNotes);
        sandbox.getSceneControl().getCurrentSceneVO().composite = newVo;

        return sandbox.getSceneControl().getCurrentSceneVO();
    }
}