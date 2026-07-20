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

import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.ecs.io.JsonArtemisSerializer;
import games.rednblack.editor.renderer.ecs.managers.EngineSerializationManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationGLESFix;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.util.ToastManager;
import games.rednblack.editor.proxy.*;
import games.rednblack.editor.renderer.ExternalTypesConfiguration;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.physics.PhysicsBodyLoader;
import games.rednblack.editor.renderer.systems.CullingSystem;
import games.rednblack.editor.renderer.systems.LightSystem;
import games.rednblack.editor.renderer.systems.ParticleSystem;
import games.rednblack.editor.renderer.systems.PhysicsSystem;
import games.rednblack.h2d.extension.talos.TalosAnchorConstraintSystem;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.renderer.utils.TextureArrayCpuPolygonSpriteBatch;
import games.rednblack.editor.system.HyperLap2dRendererMiniMap;
import games.rednblack.editor.system.ParticleContinuousSystem;
import games.rednblack.editor.system.PhysicsAdjustSystem;
import games.rednblack.editor.system.TalosContinuousSystem;
import games.rednblack.editor.utils.NativeDialogs;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ItemControlMediator;
import games.rednblack.editor.view.SceneControlMediator;
import games.rednblack.editor.view.stage.input.InputListener;
import games.rednblack.editor.view.stage.tools.PanTool;
import games.rednblack.editor.view.ui.widget.actors.basic.PixelRect;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.h2d.common.vo.SceneConfigVO;
import games.rednblack.h2d.extension.bvb.BVBItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.talos.TalosSystem;
import games.rednblack.h2d.extension.tinyvg.TinyVGItemType;
import games.rednblack.h2d.extension.typinglabel.TypingLabelItemType;
import games.rednblack.puremvc.Facade;

import java.util.HashMap;

/**
 * Sandbox is a complex hierarchy of managing classes that is supposed to be a main hub for the "commands" the part of editor where
 * user drops all panels, moves them around, and composes the scene. commands is responsible for using runtime to render the visual scene,
 * it is responsible to listen for all events, item resizing, selecting, aligning, removing and things like that.
 *
 * @author azakhary
 */
public class Sandbox {

    private static Sandbox instance = null;

    private SceneControlMediator sceneControl;
    private ItemControlMediator itemControl;

    private int currentViewingEntity = -1;

    private String currentLoadedSceneFileName;
    private UIStage uiStage;
    private ItemSelector selector;
    private Facade facade;

    private ProjectManager projectManager;
    private ResourceManager resourceManager;
    private SettingsManager settingsManager;

    SceneConfigVO sceneConfigVO;

    private PixelRect selectionRec;

    private SceneLoader sceneLoader;
    private final InputListenerRegistry listenerRegistry = new InputListenerRegistry();
    private GridService gridService;
    private ClipboardService clipboardService;
    private SceneLoadService sceneLoadService;
    private CameraService cameraService;

    private CullingSystem cullingSystem;

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

    final EngineSerializationManager manager = new EngineSerializationManager();

    private void init() {
        facade = Facade.getInstance();
        projectManager = facade.retrieveProxy(ProjectManager.NAME);
        settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);

        gridService = new GridService(projectManager, facade);

        UIStageMediator uiStageMediator = facade.retrieveMediator(UIStageMediator.NAME);
        uiStage = uiStageMediator.getViewComponent();

        ExternalTypesConfiguration externalItemTypes = new ExternalTypesConfiguration();
        //Add external item types
        externalItemTypes.addExternalItemType(new BVBItemType());
        externalItemTypes.addExternalItemType(new TalosItemType());
        externalItemTypes.addExternalItemType(new TinyVGItemType());
        externalItemTypes.addExternalItemType(new TypingLabelItemType());

        Batch batch = new TextureArrayCpuPolygonSpriteBatch(32_767);
        SceneConfiguration config = new SceneConfiguration(batch, true, settingsManager.editorConfigVO.msaaSamples);
        config.setResourceRetriever(resourceManager);
        config.setExternalItemTypes(externalItemTypes);

        //Remove Physics System and add Adjusting System for box2d objects to follow items and stop world tick
        config.removeSystem(PhysicsSystem.class);
        config.removeSystem(LightSystem.class);
        PhysicsAdjustSystem physicsAdjustSystem = new PhysicsAdjustSystem();
        config.addSystem(physicsAdjustSystem);
        LightSystem lightSystem = new LightSystem();
        config.addSystem(lightSystem);

        //Remove particle system and use a continuous system for preview purpose
        config.removeSystem(ParticleSystem.class);
        config.addSystem(new ParticleContinuousSystem());
        config.removeSystem(TalosSystem.class);
        config.addSystem(new TalosAnchorConstraintSystem());
        config.addSystem(new TalosContinuousSystem());
        config.setRendererSystem(new HyperLap2dRendererMiniMap(batch, true, config.getMsaaSamples()));

        config.addSystem(manager);

        sceneLoader = new SceneLoader(config);
        cullingSystem = sceneLoader.getEngine().getSystem(CullingSystem.class);

        manager.setSerializer(new JsonArtemisSerializer(sceneLoader.getEngine()));

        physicsAdjustSystem.setBox2DWorld(sceneLoader.getWorld());
        lightSystem.setRayHandler(sceneLoader.getRayHandler());

        sceneControl = new SceneControlMediator(sceneLoader);
        itemControl = new ItemControlMediator(sceneControl);

        selector = new ItemSelector(this, facade);
        facade.registerProxy(new SelectionProxy(selector));

        cameraService = new CameraService(this, facade);
        sceneLoadService = new SceneLoadService(this, projectManager, facade);
        clipboardService = new ClipboardService();
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

    public ItemControlMediator getItemControl() {
        return itemControl;
    }

    public String getCurrentLoadedSceneFileName() {
        return currentLoadedSceneFileName;
    }

    public void setCurrentLoadedSceneFileName(String sceneName) {
        currentLoadedSceneFileName = sceneName;
    }

    public PixelRect getSelectionRec() {
        return selectionRec;
    }

    public Engine getEngine() {
        return sceneLoader.getEngine();
    }

    public void loadCurrentProject() {
        sceneLoadService.loadCurrentProject();
    }

    public void loadScene(String sceneName) {
        sceneLoadService.loadScene(sceneName);
    }

    /**
     * Renderer method used to animate zoom and camera position
     *
     * @param deltaTime
     */
    public void render(float deltaTime) {
        cameraService.update(deltaTime);
        cullingSystem.setDebug(settingsManager.editorConfigVO.showBoundingBoxes);
    }

    public void adjustCameraInComposites() {
        cameraService.adjustCameraInComposites();
    }

    public void scenePanned() {
        cameraService.scenePanned();
    }

    public void panSceneTo(float x, float y) {
        cameraService.panSceneTo(x, y);
    }

    public void panSceneBy(float amountX, float amountY) {
        cameraService.panSceneBy(amountX, amountY);
    }

    public Vector2 getCameraPosTarget() {
        return cameraService.getCameraPosTarget();
    }

    public float getCameraZoomTarget() {
        return cameraService.getCameraZoomTarget();
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
        return sceneLoadService.sceneVoFromItems();
    }

    public ItemSelector getSelector() {
        return selector;
    }

    public void prepareSelectionRectangle(float x, float y) {
        selectionRec.setOpacity(0.8f);
        selectionRec.setWidth(0);
        selectionRec.setHeight(0);
        selectionRec.setX(x);
        selectionRec.setY(y);
    }

    public int getZoomPercent() {
        return cameraService.getZoomPercent();
    }

    public Vector3 getCameraPosition() {
        return cameraService.getCameraPosition();
    }

    public void setZoomPercent(float percent, boolean moveCamera) {
        cameraService.setZoomPercent(percent, moveCamera);
    }

    public void zoomDivideBy(float amount) {
        cameraService.zoomDivideBy(amount);
    }

    public float getWorldGridSize() {
        return getGridSize() > 1 ? getGridSize() : getGridSize() / sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
    }

    public float getGridSize() {
        return gridService.getGridSize();
    }

    public void setGridSize(float gridSize) {
        gridService.setGridSize(gridSize);
    }

    public boolean getLockLines() {
        return gridService.getLockLines();
    }

    public void setLockLines(boolean lockLines) {
        gridService.setLockLines(lockLines);
    }

    public int getRootEntity() {
        return sceneControl.getRootEntity();
    }

    public boolean isViewingRootEntity() {
        return currentViewingEntity == getRootEntity();
    }

    public void overrideAmbientLightInComposite() {
        SceneVO sceneVO = sceneControl.getCurrentSceneVO();

        boolean override = !isViewingRootEntity() && settingsManager.editorConfigVO.disableAmbientComposite;
        sceneLoader.setAmbientInfo(sceneVO, override);
    }

    //Global Listeners part

    public void addListener(InputListener listener) {
        listenerRegistry.add(listener);
    }

    public void removeListener(InputListener listener) {
        listenerRegistry.remove(listener);
    }

    public void removeAllListener() {
        listenerRegistry.removeAll();
    }

    public Array<InputListener> getAllListeners() {
        return listenerRegistry.getAll();
    }

    public OrthographicCamera getCamera() {
        return cameraService.getCamera();
    }

    public int getCurrentViewingEntity() {
        return currentViewingEntity;
    }

    public void setCurrentViewingEntity(int entity) {
        currentViewingEntity = entity;
    }

    public ViewPortComponent getViewportComponent() {
        return cameraService.getViewportComponent();
    }

    public Viewport getViewport() {
        return cameraService.getViewport();
    }

    /**
     * Transformations
     **/

    public Rectangle screenToWorld(Rectangle rect) {
        return cameraService.screenToWorld(rect);
    }

    public Vector2 screenToWorld(Vector2 vector) {
        return cameraService.screenToWorld(vector);
    }

    public Vector2 worldToScreen(Vector2 vector) {
        return cameraService.worldToScreen(vector);
    }

    public Vector2 screenToWorld(float x, float y) {
        return cameraService.screenToWorld(x, y);
    }

    public Vector2 worldToScreen(float x, float y) {
        return cameraService.worldToScreen(x, y);
    }

    public float getInputX() {
        return cameraService.getInputX();
    }

    public float getInputX(float offset) {
        return cameraService.getInputX(offset);
    }

    public float getInputY() {
        return cameraService.getInputY();
    }

    public float getInputY(float offset) {
        return cameraService.getInputY(offset);
    }

    public static void copyToClipboard(Object data) {
        Object[] payload = new Object[2];
        payload[0] = new Vector2(Sandbox.getInstance().getCamera().position.x,Sandbox.getInstance().getCamera().position.y);
        payload[1] = data;

        Lwjgl3ApplicationGLESFix app = (Lwjgl3ApplicationGLESFix) Gdx.app;
        Json json = HyperJson.getJson();
        try {
            app.getClipboard().setContents(json.toJson(payload));
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            NativeDialogs.showError("You are trying to copy too many objects!");
        }
    }

    public static Object retrieveFromClipboard() {
        Lwjgl3ApplicationGLESFix app = (Lwjgl3ApplicationGLESFix) Gdx.app;
        Json json = HyperJson.getJson();
        Object[] data = null;
        try {
            data = json.fromJson(Object[].class, app.getClipboard().getContents());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public void copyToLocalClipboard(String key, Object data) {
        clipboardService.copyToLocalClipboard(key, data);
    }

    public Object retrieveFromLocalClipboard(String key) {
        return clipboardService.retrieveFromLocalClipboard(key);
    }

    public int getPixelPerWU() {
        return sceneLoader.getPixelsPerWU();
    }

    public void dispose() {
        sceneLoader.dispose();
        PhysicsBodyLoader.getInstance().dispose();
    }

    public void resize(int width, int height) {
        if (height == 0 && width == 0)
            return;

        sceneLoader.resize(width, height);
        if (getViewport() != null)
            getCamera().update();
    }
}