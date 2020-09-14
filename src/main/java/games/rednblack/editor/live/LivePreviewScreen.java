package games.rednblack.editor.live;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.extention.spine.SpineItemType;
import org.puremvc.java.interfaces.IFacade;

public class LivePreviewScreen extends ScreenAdapter implements GestureDetector.GestureListener {
    private Viewport viewport;
    private SceneLoader sceneLoader;

    private final ProjectManager projectManager;

    private final Box2DDebugRenderer mBox2DDebugRenderer;
    private final IFacade facade = HyperLap2DFacade.getInstance();
    private final Color bgColor;
    
    private final OrthographicCamera mCamera;

    public LivePreviewScreen() {
        projectManager = facade.retrieveProxy(ProjectManager.NAME);
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);

        int previewWidth = resolutionManager.getOriginalResolution().width;
        int previewHeight = resolutionManager.getOriginalResolution().height;

        WorldSizeVO worldSizeVO = new WorldSizeVO(Sandbox.getInstance().getPixelPerWU(), previewWidth, previewHeight);

        mBox2DDebugRenderer = new Box2DDebugRenderer();

        viewport = new ExtendViewport(worldSizeVO.getWorldWidth(), worldSizeVO.getWorldHeight());
        mCamera = (OrthographicCamera) viewport.getCamera();
        sceneLoader = new SceneLoader(resourceManager);
        sceneLoader.injectExternalItemType(new SpineItemType());
        sceneLoader.loadScene(projectManager.getCurrentSceneConfigVO().sceneName, viewport);

        bgColor = projectManager.currentProjectVO.backgroundColor;

        Gdx.input.setInputProcessor(new GestureDetector(this));
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(bgColor.r,bgColor.g,bgColor.b,bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        sceneLoader.getEngine().update(delta);

        if (projectManager.currentProjectVO.box2dDebugRender)
            mBox2DDebugRenderer.render(sceneLoader.getWorld(), mCamera.combined);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        mCamera.position.add(
                mCamera.unproject(new Vector3(0, 0, 0))
                        .add(mCamera.unproject(new Vector3(deltaX, deltaY, 0)).scl(-1f))
        );
        mCamera.update();
        return true;
    }

    @Override
    public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
        return true;
    }

    @Override
    public void pinchStop() {

    }
}
