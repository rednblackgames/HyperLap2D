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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.systems.LightSystem;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extention.spine.SpineItemType;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.renderer.components.additional.ButtonComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.CompositeVO;
import games.rednblack.editor.renderer.data.LayerItemVO;
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


    public SceneControlMediator sceneControl;
    public ItemControlMediator itemControl;

    private final HashMap<String, Object> localClipboard = new HashMap<>();

    private Entity currentViewingEntity;

    /** This part contains legacy params that need to be removed one by one. */
    public int currTransformType = -1;
    public Entity currTransformHost;
    public boolean isResizing = false;
    public boolean dirty = false;
    public Vector3 copedItemCameraOffset;

    public String currentLoadedSceneFileName;
    private int gridSize = 1; // pixels
    private float zoomPercent = 100;
    private UIStage uiStage;
    private ItemSelector selector;
    private HyperLap2DFacade facade;

    private ProjectManager projectManager;
    private ResourceManager resourceManager;
    
    public PixelRect selectionRec;

    private SceneLoader sceneLoader;
	private Array<InputListener> listeners = new Array<>(1);
    /** End of shitty part. */


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

        //Remove Physics System and add Adjusting System for box2d objects to follow items and stop world tick
        sceneLoader.engine.removeSystem(sceneLoader.engine.getSystem(PhysicsSystem.class));
        sceneLoader.engine.removeSystem(sceneLoader.engine.getSystem(LightSystem.class));
        LightSystem lightSystem = new LightSystem();
        lightSystem.setRayHandler(sceneLoader.rayHandler);
        sceneLoader.engine.addSystem(new PhysicsAdjustSystem(sceneLoader.world));
        sceneLoader.engine.addSystem(lightSystem);

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

    public SceneControlMediator getSceneControl() {
        return sceneControl;
    }
    
    public Engine getEngine() {
        return sceneLoader.getEngine();
    }


    /**
     * TODO: loading fonts this way is a bit outdated and needs to change
     *
     * @param sceneName
     */
    public void initData(String sceneName) {
        sceneControl.initScene(sceneName);
    }

    public void loadCurrentProject(String name) {
    	//TODO fix and uncomment
        //sceneControl.getEssentials().rm = resourceManager;
        loadScene(name);
    }

    public void loadCurrentProject() {
        ProjectVO projectVO = projectManager.getCurrentProjectVO();
        loadCurrentProject(projectVO.lastOpenScene.isEmpty() ? "MainScene" : projectVO.lastOpenScene);
    }

    public void loadScene(String sceneName) {
        currentLoadedSceneFileName = sceneName;

        initData(sceneName);

        initView();

        initSceneView(sceneControl.getRootSceneVO());

        ProjectVO projectVO = projectManager.getCurrentProjectVO();
        projectVO.lastOpenScene = sceneName;
        projectManager.saveCurrentProject();

        facade.sendNotification(MsgAPI.LIBRARY_LIST_UPDATED);

        currentViewingEntity = getRootEntity();

        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        SceneConfigVO sceneConfigVO = projectManager.getCurrentSceneConfigVO();
        getCamera().position.set(sceneConfigVO.cameraPosition[0], sceneConfigVO.cameraPosition[1], 0);
        projectManager.changeSceneWindowTitle();

        //TODO: move this into SceneDataManager!
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        sceneDataManager.sendNotification(MsgAPI.SCENE_LOADED);

        // add additional components
        // TODO: maybe move this somewhere else
        sceneControl.sceneLoader.addComponentsByTagName("button", ButtonComponent.class);
    }

    public void initSceneView(CompositeItemVO compositeItemVO) {
    	//TODO fix and uncomment
        //initSceneView(sceneControl.initSceneView(compositeItemVO));
    }

    public void initSceneView(Entity composite) {
    	//TODO fix and uncomment
//        selector.clearSelections();
//        sandboxStage.mainBox.clear();
//        sceneControl.initSceneView(composite, true/*uiStage.getCompositePanel().isRootScene()*/);
////        if (uiStage.getCompositePanel().isRootScene()) {
////            uiStage.getCompositePanel().updateRootScene(sceneControl.getRootSceneVO());
////        }
//
//        sandboxStage.mainBox.addActor(sceneControl.getCurrentScene());
//        sceneControl.getCurrentScene().setX(0);
//        sceneControl.getCurrentScene().setY(0);
//
//        //uiStage.getLayerPanel().initContent();
//        forceContinuousParticles(composite);
    }

    /**
     * Some particle panels might not be continuous, so they will stop after first iteration, which is ok
     * This method will make sure they look continuous while in editor, so user will find and see them easily.
     *
     * @param composite composite on screen with particles to be forced to be continuous
     */
  //TODO fix and uncomment
//    private void forceContinuousParticles(CompositeItem composite) {
//        ArrayList<IBaseItem> asd = composite.getItems();
//        for (int i = 0; i < asd.size(); i++) {
//            IBaseItem item = asd.get(i);
//            if (item instanceof ParticleItem) {
//                ((ParticleItem) item).forceContinuous();
//                continue;
//            }
//            if (item instanceof CompositeItem) {
//                forceContinuousParticles((CompositeItem) item);
//            }
//
//        }
//    }

    /**
     * Well... that's a bummer, I cannot remember why this was for. but the name speaks for itself sort of.
     * TODO: figure this out
     *
     * @return SceneVO
     */
    public SceneVO sceneVoFromItems() {
        sceneControl.getCurrentSceneVO().composite = new CompositeVO();
        sceneControl.getCurrentSceneVO().composite.loadFromEntity(getRootEntity());

        return sceneControl.getCurrentSceneVO();
    }

    /**
     * Initializes current scene on screen from a tools object.
     *
     * @param vo CompositeItemVO tools
     */
    public void reconstructFromSceneVo(CompositeItemVO vo) {
        initSceneView(vo);
    }

    /**
     * TODO: what does this do? seems to be saving as checkpoint of Flow? it so it should be renamed
     */
    public void saveSceneCurrentSceneData() {
		//TODO fix and uncomment
        //sceneControl.getCurrentScene().updateDataVO();
    }


    public ItemSelector getSelector() {
        return selector;
    }

    /**
     * @deprecated
     * @return
     */
    public boolean isComponentSkinAvailable() {
        return true;
    }

    public LayerItemVO getSelectedLayer() {
        return uiStage.getCurrentSelectedLayer();
    }

    public void setCurrentlyTransforming(Entity item, int transformType) {
        if (item == null || item.getClass().getSimpleName().equals("LabelItem")) return;
        currTransformType = transformType;
        currTransformHost = item;
    }

    public Entity getCurrentScene() {
        return sceneControl.getCurrentScene();
    }

    public void prepareSelectionRectangle(float x, float y, boolean setOpacity) {
        // space is panning, so if we are not, then prepare the selection rectangle
        if (setOpacity) {
            selectionRec.setOpacity(0.6f);
        }
        selectionRec.setWidth(0);
        selectionRec.setHeight(0);
        selectionRec.setX(x);
        selectionRec.setY(y);
    }


    public int getZoomPercent() {
        return (int)zoomPercent;
    }

    public void setZoomPercent(float percent) {
        zoomPercent = percent;
        getCamera().zoom = 1f / (zoomPercent / 100f);
    }

    public void zoomBy(float amount) {
        zoomPercent += -amount * 15f;

        if (zoomPercent < 20) zoomPercent = 20;
        if (zoomPercent > 1000) zoomPercent = 1000;

        setZoomPercent(zoomPercent);
        facade.sendNotification(MsgAPI.ZOOM_CHANGED);
    }

    public void zoomDivideBy(float amount) {
        zoomPercent /= amount;
        if (zoomPercent < 20) zoomPercent = 20;
        if (zoomPercent > 1000) zoomPercent = 1000;

        setZoomPercent(zoomPercent);
        facade.sendNotification(MsgAPI.ZOOM_CHANGED);
    }

    public float getWorldGridSize(){
        return (float)gridSize/sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
        facade.sendNotification(MsgAPI.GRID_SIZE_CHANGED, gridSize);
    }

    public void setLockLines(boolean lockLines) {
        facade.sendNotification(MsgAPI.LOCK_LINES_CHANGED, lockLines);
    }

    
    public Entity getRootEntity(){
    	return sceneControl.getRootEntity();
    }
    
    
    //Global Listeners part
    
    public void addListener(InputListener listener){
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
		
	}
	
	public void removeListener(InputListener listener){
		listeners.removeValue(listener, true);
	}
	
	public void removeAllListener(){
		listeners.clear();
	}
	
	public Array<InputListener> getAllListeners(){
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
        if(getCurrentViewingEntity() == null) return null;
        ViewPortComponent viewPortComponent = ComponentRetriever.get(getCurrentViewingEntity(), ViewPortComponent.class);
        return viewPortComponent;
    }

    public Viewport getViewport() {
        ViewPortComponent viewPortComponent = getViewportComponent();
        if(viewPortComponent == null) return null;
        return viewPortComponent.viewPort;
    }

    /** Transformations **/

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
        if ( sceneControl.sceneLoader.getRm().getProjectVO() == null) {
            return vector;
        }
        int pixelPerWU = sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
        OrthographicCamera camera = Sandbox.getInstance().getCamera();
        Viewport viewport = Sandbox.getInstance().getViewport();

        vector.x = (vector.x - (viewport.getScreenWidth()/2f - camera.position.x*pixelPerWU/camera.zoom))*camera.zoom;
        vector.y = (vector.y - (viewport.getScreenHeight()/2f - camera.position.y*pixelPerWU/camera.zoom))*camera.zoom;

        vector.scl(1f/pixelPerWU);


        return vector;
    }

    public Vector2 worldToScreen(Vector2 vector) {
        // TODO: WTF, project had to work instead I am back to this barbarian methods of unholy land!
        //vector = getViewport().project(vector);
        int pixelPerWU = sceneControl.sceneLoader.getRm().getProjectVO().pixelToWorld;
        OrthographicCamera camera = Sandbox.getInstance().getCamera();
        Viewport viewport = Sandbox.getInstance().getViewport();
        vector.x = vector.x/camera.zoom + (viewport.getWorldWidth()/2 - (camera.position.x)/camera.zoom);
        vector.y = vector.y/camera.zoom + (viewport.getWorldHeight()/2 - (camera.position.y)/camera.zoom);

        vector.scl(pixelPerWU);

        return vector;
    }

    public Vector2 screenToWorld(float x, float y) {
        return screenToWorld(new Vector2(x, y));
    }

    public Vector2 worldToScreen(float x, float y) {
        return worldToScreen(new Vector2(x, y));
    }


    public void copyToClipboard(Object data) {
        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;
        Json json = new Json();
        app.getClipboard().setContents(json.toJson(data));
    }

    public Object retrieveFromClipboard() {
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
        if (sceneLoader.getRm().getProjectVO() == null) {
            return 1;
        }
        return sceneLoader.getRm().getProjectVO().pixelToWorld;
    }
}
