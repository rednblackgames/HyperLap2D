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

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.util.ToastManager;
import games.rednblack.editor.proxy.*;
import games.rednblack.editor.renderer.systems.LightSystem;
import games.rednblack.editor.renderer.systems.ParticleSystem;
import games.rednblack.editor.system.ParticleContinuousSystem;
import games.rednblack.editor.system.TalosContinuousSystem;
import games.rednblack.editor.view.stage.tools.PanTool;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.talos.TalosSystem;
import games.rednblack.h2d.extention.spine.SpineItemType;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.renderer.components.additional.ButtonComponent;
import games.rednblack.editor.renderer.data.CompositeVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.systems.PhysicsSystem;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.system.PhysicsAdjustSystem;
import games.rednblack.editor.view.ItemControlMediator;
import games.rednblack.editor.view.SceneControlMediator;
import games.rednblack.editor.view.stage.input.InputListener;
import games.rednblack.editor.view.ui.widget.actors.basic.PixelRect;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.h2d.common.vo.SceneConfigVO;

import java.util.HashMap;

/**
 * Sandbox is a complex hierarchy of managing classes that is supposed to be a main hub for the "commands" the part of editor where
 * user drops all panels, moves them around, and composes the scene. commands is responsible for using runtime to render the visual scene,
 * it is responsible to listen for all the events, item resizing, selecting, aligning, removing and things like that.
 *
 * @author azakhary
 */
public class Sandbox {

    private static Sandbox instance = null;

    private static final float CAMERA_ZOOM_DURATION = 0.65f;
    private static final float CAMERA_PAN_DURATION = 0.45f;

    public SceneControlMediator sceneControl;
    public ItemControlMediator itemControl;

    private final HashMap<String, Object> localClipboard = new HashMap<>();

    private Entity currentViewingEntity;

    public String currentLoadedSceneFileName;
    private UIStage uiStage;
    private ItemSelector selector;
    private HyperLap2DFacade facade;

    private ProjectManager projectManager;
    private ResourceManager resourceManager;

    SceneConfigVO sceneConfigVO;

    public PixelRect selectionRec;

    private SceneLoader sceneLoader;
    private Array<InputListener> listeners = new Array<>(1);

    Vector3 temp = new Vector3();
    private float timeToCameraZoomTarget, cameraZoomTarget, cameraZoomOrigin;
    private boolean moveCameraWithZoom = false;

    private float timeToCameraPosTarget;
    private Vector2 cameraPosTarget = new Vector2(), cameraPosOrigin = new Vector2();

    private Sandbox() {
        init();
    }

    /**
     * The instance gets created only when it is called for first time.
     * Lazy-loading
     */
    public synchronized static Sandbox getInstance() {
        if (instance == null) {
            instance = new Sandbox();
        }
        return instance;
    }

    private void init() {
        facade = HyperLap2DFacade.getInstance();
        projectManager = facade.retrieveProxy(ProjectManager.NAME);
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);

        UIStageMediator uiStageMediator = facade.retrieveMediator(UIStageMediator.NAME);
        uiStage = uiStageMediator.getViewComponent();

        sceneLoader = new SceneLoader(resourceManager);
        // adding spine as external component
        sceneLoader.injectExternalItemType(new SpineItemType());
        sceneLoader.injectExternalItemType(new TalosItemType());

        //Remove Physics System and add Adjusting System for box2d objects to follow items and stop world tick
        sceneLoader.getEngine().removeSystem(sceneLoader.getEngine().getSystem(PhysicsSystem.class));
        sceneLoader.getEngine().removeSystem(sceneLoader.getEngine().getSystem(LightSystem.class));
        LightSystem lightSystem = new LightSystem();
        lightSystem.setRayHandler(sceneLoader.getRayHandler());
        sceneLoader.getEngine().addSystem(new PhysicsAdjustSystem(sceneLoader.getWorld()));
        sceneLoader.getEngine().addSystem(lightSystem);

        //Remove particle system and use a continuous system for preview purpose
        sceneLoader.getEngine().removeSystem(sceneLoader.getEngine().getSystem(ParticleSystem.class));
        sceneLoader.getEngine().addSystem(new ParticleContinuousSystem());
        sceneLoader.getEngine().removeSystem(sceneLoader.getEngine().getSystem(TalosSystem.class));
        sceneLoader.getEngine().addSystem(new TalosContinuousSystem());

        sceneControl = new SceneControlMediator(sceneLoader);
        itemControl = new ItemControlMediator(sceneControl);

        selector = new ItemSelector(this);
    }

    public void initView() {
        selectionRec = new PixelRect(0, 0);
        selectionRec.setFillColor(new Color(1, 1, 1, 0.1f));
        selectionRec.setOpacity(0.0f);
        selectionRec.setTouchable(Touchable.disabled);
        uiStage.midUI.addActor(selectionRec);
    }

    public void setKeyboardFocus() {
        uiStage.setKeyboardFocus(uiStage.midUI);
    }

    /**
     * Getters *
     */
    public UIStage getUIStage() {
        return uiStage;
    }

    public ToastManager getToastManager() {
        return uiStage.getToastManager();
    }

    public SceneControlMediator getSceneControl() {
        return sceneControl;
    }

    public PooledEngine getEngine() {
        return sceneLoader.getEngine();
    }

    public void loadCurrentProject() {
        ProjectVO projectVO = projectManager.getCurrentProjectVO();
        loadScene(projectVO.lastOpenScene.isEmpty() ? "MainScene" : projectVO.lastOpenScene);
    }

    public void loadScene(String sceneName) {
        currentLoadedSceneFileName = sceneName;

        sceneControl.initScene(sceneName);

        initView();

        ProjectVO projectVO = projectManager.getCurrentProjectVO();
        projectVO.lastOpenScene = sceneName;
        projectManager.saveCurrentProject();

        facade.sendNotification(MsgAPI.LIBRARY_LIST_UPDATED);
        facade.sendNotification(MsgAPI.LIBRARY_ACTIONS_UPDATED);

        setCurrentViewingEntity(getRootEntity());

        sceneConfigVO = projectManager.getCurrentSceneConfigVO();
        getCamera().position.set(sceneConfigVO.cameraPosition[0], sceneConfigVO.cameraPosition[1], 0);
        setZoomPercent(sceneConfigVO.cameraZoom, false);
        projectManager.changeSceneWindowTitle();

        //TODO: move this into SceneDataManager!
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        sceneDataManager.sendNotification(MsgAPI.SCENE_LOADED);

        // add additional components
        // TODO: maybe move this somewhere else
        sceneControl.sceneLoader.addComponentByTagName("button", ButtonComponent.class);

        CommandManager commandManager = facade.retrieveProxy(CommandManager.NAME);
        commandManager.initHistory();
    }

    /**
     * Renderer method used to animate zoom and camera position
     *
     * @param deltaTime
     */
    public void render(float deltaTime) {
        if (timeToCameraZoomTarget > 0) {
            getCamera().unproject(temp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            float px = temp.x;
            float py = temp.y;

            timeToCameraZoomTarget -= deltaTime;
            float progress = timeToCameraZoomTarget < 0 ? 1 : 1f - timeToCameraZoomTarget / CAMERA_ZOOM_DURATION;
            getCamera().zoom = Interpolation.pow3Out.apply(cameraZoomOrigin, cameraZoomTarget, progress);
            getCamera().update();

            if (moveCameraWithZoom) {
                getCamera().unproject(temp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                getCamera().position.add(px - temp.x, py - temp.y, 0);
                getCamera().update();
            }

            facade.sendNotification(MsgAPI.ZOOM_CHANGED);
        }

        if (timeToCameraPosTarget > 0) {
            timeToCameraPosTarget -= deltaTime;
            float progress = timeToCameraPosTarget < 0 ? 1 : 1f - timeToCameraPosTarget / CAMERA_PAN_DURATION;
            float x = Interpolation.smoother.apply(cameraPosOrigin.x, cameraPosTarget.x, progress);
            float y = Interpolation.smoother.apply(cameraPosOrigin.y, cameraPosTarget.y, progress);
            getCamera().position.set(x, y, 0);

            facade.sendNotification(PanTool.SCENE_PANNED);
        }
    }

    public void adjustCameraInComposites() {
        if (!isViewingRootEntity()) {
            panSceneTo(0, 0);
        } else {
            panSceneTo(sceneConfigVO.cameraPosition[0], sceneConfigVO.cameraPosition[1]);
        }
    }

    public void scenePanned() {
        if (isViewingRootEntity() && timeToCameraPosTarget <= 0) {
            sceneConfigVO.cameraPosition[0] = getCamera().position.x;
            sceneConfigVO.cameraPosition[1] = getCamera().position.y;
        }
    }

    public void panSceneTo(float x, float y) {
        cameraPosOrigin.set(getCamera().position.x, getCamera().position.y);
        cameraPosTarget.set(x, y);
        timeToCameraPosTarget = CAMERA_PAN_DURATION - timeToCameraPosTarget * 0.5f;
    }

    public void panSceneBy(float amountX, float amountY) {
        cameraPosOrigin.set(getCamera().position.x, getCamera().position.y);
        cameraPosTarget.set(cameraPosOrigin.x + amountX, cameraPosOrigin.y + amountY);
        timeToCameraPosTarget = CAMERA_PAN_DURATION - timeToCameraPosTarget * 0.5f;
    }

    public Vector2 getCameraPosTarget() {
        return cameraPosTarget;
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
        CompositeVO newVo = new CompositeVO();
        newVo.loadFromEntity(getRootEntity());
        newVo.sStickyNotes.putAll(sceneControl.getCurrentSceneVO().composite.sStickyNotes);
        sceneControl.getCurrentSceneVO().composite = newVo;

        return sceneControl.getCurrentSceneVO();
    }

    public ItemSelector getSelector() {
        return selector;
    }

    public Entity getCurrentScene() {
        return sceneControl.getCurrentScene();
    }

    public void prepareSelectionRectangle(float x, float y) {
        selectionRec.setOpacity(0.8f);
        selectionRec.setWidth(0);
        selectionRec.setHeight(0);
        selectionRec.setX(x);
        selectionRec.setY(y);
    }

    public int getZoomPercent() {
        return (int) sceneConfigVO.cameraZoom;
    }

    public void setZoomPercent(float percent, boolean moveCamera) {
        sceneConfigVO.cameraZoom = percent;

        cameraZoomOrigin = getCamera().zoom;
        cameraZoomTarget = 1f / (sceneConfigVO.cameraZoom / 100f);

        timeToCameraZoomTarget = CAMERA_ZOOM_DURATION;
        moveCameraWithZoom = moveCamera;
    }

    public void zoomDivideBy(float amount) {
        float zoomPercent = sceneConfigVO.cameraZoom / amount;
        if (zoomPercent < 20) zoomPercent = 20;
        if (zoomPercent > 1000) zoomPercent = 1000;

        setZoomPercent(zoomPercent, false);
    }

    public float getWorldGridSize() {
        return (float) getGridSize() / sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
    }

    public int getGridSize() {
        return projectManager.currentProjectVO.gridSize;
    }

    public void setGridSize(int gridSize) {
        projectManager.currentProjectVO.gridSize = gridSize;
        projectManager.saveCurrentProject();
        facade.sendNotification(MsgAPI.GRID_SIZE_CHANGED, gridSize);
    }

    public boolean getLockLines() {
        return projectManager.currentProjectVO.lockLines;
    }

    public void setLockLines(boolean lockLines) {
        projectManager.currentProjectVO.lockLines = lockLines;
        projectManager.saveCurrentProject();
        facade.sendNotification(MsgAPI.LOCK_LINES_CHANGED, lockLines);
    }

    public Entity getRootEntity() {
        return sceneControl.getRootEntity();
    }

    public boolean isViewingRootEntity() {
        return currentViewingEntity.equals(getRootEntity());
    }

    public void overrideAmbientLightInComposite() {
        SceneVO sceneVO = sceneControl.getCurrentSceneVO();

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        boolean override = !isViewingRootEntity() && settingsManager.editorConfigVO.disableAmbientComposite;
        sceneLoader.setAmbientInfo(sceneVO, override);
    }

    //Global Listeners part

    public void addListener(InputListener listener) {
        if (!listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }

    public void removeListener(InputListener listener) {
        listeners.removeValue(listener, true);
    }

    public void removeAllListener() {
        listeners.clear();
    }

    public Array<InputListener> getAllListeners() {
        listeners.shrink();
        return listeners;
    }

    public OrthographicCamera getCamera() {
        return (OrthographicCamera) getViewport().getCamera();
    }

    public Entity getCurrentViewingEntity() {
        return currentViewingEntity;
    }

    public void setCurrentViewingEntity(Entity entity) {
        currentViewingEntity = entity;
    }

    public ViewPortComponent getViewportComponent() {
        if (getCurrentViewingEntity() == null) return null;
        return ComponentRetriever.get(getCurrentViewingEntity(), ViewPortComponent.class);
    }

    public Viewport getViewport() {
        ViewPortComponent viewPortComponent = getViewportComponent();
        if (viewPortComponent == null) return null;
        return viewPortComponent.viewPort;
    }

    /**
     * Transformations
     **/

    public Rectangle screenToWorld(Rectangle rect) {
        Vector2 pos = screenToWorld(new Vector2(rect.x, rect.y));
        Vector2 pos2 = screenToWorld(new Vector2(rect.x + rect.width, rect.y + rect.height));
        rect.x = pos.x;
        rect.y = pos.y;
        rect.width = pos2.x - rect.x;
        rect.height = pos2.y - rect.y;
        return rect;
    }

    public Vector2 screenToWorld(Vector2 vector) {
        // TODO: now unproject doesnot do well too. I am completely lost here. how hard is it to do screen to world, madafakas.
        //getViewport().unproject(vector);
        if (sceneControl.sceneLoader.getRm().getProjectVO() == null) {
            return vector;
        }
        int pixelPerWU = sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
        OrthographicCamera camera = Sandbox.getInstance().getCamera();
        Viewport viewport = Sandbox.getInstance().getViewport();

        vector.x = (vector.x - (viewport.getScreenWidth() / 2f - camera.position.x * pixelPerWU / camera.zoom)) * camera.zoom;
        vector.y = (vector.y - (viewport.getScreenHeight() / 2f - camera.position.y * pixelPerWU / camera.zoom)) * camera.zoom;

        vector.scl(1f / pixelPerWU);


        return vector;
    }

    public Vector2 worldToScreen(Vector2 vector) {
        // TODO: WTF, project had to work instead I am back to this barbarian methods of unholy land!
        //vector = getViewport().project(vector);
        int pixelPerWU = sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
        OrthographicCamera camera = Sandbox.getInstance().getCamera();
        Viewport viewport = Sandbox.getInstance().getViewport();
        vector.x = vector.x / camera.zoom + (viewport.getWorldWidth() / 2 - (camera.position.x) / camera.zoom);
        vector.y = vector.y / camera.zoom + (viewport.getWorldHeight() / 2 - (camera.position.y) / camera.zoom);

        vector.scl(pixelPerWU);

        return vector;
    }

    public Vector2 screenToWorld(float x, float y) {
        return screenToWorld(new Vector2(x, y));
    }

    public Vector2 worldToScreen(float x, float y) {
        return worldToScreen(new Vector2(x, y));
    }


    public static void copyToClipboard(Object data) {
        Object[] payload = new Object[2];
        payload[0] = new Vector2(Sandbox.getInstance().getCamera().position.x,Sandbox.getInstance().getCamera().position.y);
        payload[1] = data;

        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
        Json json = new Json();
        app.getClipboard().setContents(json.toJson(payload));
    }

    public static Object retrieveFromClipboard() {
        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
        Json json = new Json();
        Object[] data = null;
        try {
            data = json.fromJson(Object[].class, app.getClipboard().getContents());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public void copyToLocalClipboard(String key, Object data) {
        this.localClipboard.put(key, data);
    }

    public Object retrieveFromLocalClipboard(String key) {
        return localClipboard.get(key);
    }

    public int getPixelPerWU() {
        return sceneLoader.getPixelsPerWU();
    }

    public void dispose() {
        sceneLoader.dispose();
    }

    public void resize(int width, int height) {
        if (height == 0 && width == 0)
            return;

        sceneLoader.resize(width, height);
    }
}
