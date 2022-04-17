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
import games.rednblack.editor.renderer.ExternalTypesConfiguration;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.additional.ButtonComponent;
import games.rednblack.editor.renderer.utils.TextureArrayCpuPolygonSpriteBatch;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGItemType;
import games.rednblack.h2d.extension.typinglabel.TypingLabelItemType;
import games.rednblack.h2d.extension.spine.SpineItemType;
import org.puremvc.java.interfaces.IFacade;

public class LivePreviewScreen extends ScreenAdapter implements GestureDetector.GestureListener {
    private final Vector3 vec3Zero = new Vector3(0, 0, 0);
    private final Vector3 cameraTargetPos = new Vector3();

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

        ExternalTypesConfiguration externalItemTypes = new ExternalTypesConfiguration();
        //Add external item types
        externalItemTypes.addExternalItemType(new SpineItemType());
        externalItemTypes.addExternalItemType(new TalosItemType());
        externalItemTypes.addExternalItemType(new TinyVGItemType());
        externalItemTypes.addExternalItemType(new TypingLabelItemType());

        SceneConfiguration config = new SceneConfiguration(new TextureArrayCpuPolygonSpriteBatch(10_000), true);
        config.setResourceRetriever(resourceManager);
        config.setExternalItemTypes(externalItemTypes);

        config.addTagTransmuter("button", ButtonComponent.class);
        sceneLoader = new SceneLoader(config);

        sceneLoader.loadScene(projectManager.getCurrentSceneConfigVO().sceneName, viewport);

        bgColor = projectManager.currentProjectVO.backgroundColor;

        Gdx.input.setInputProcessor(new GestureDetector(this));

        cameraTargetPos.set(mCamera.position);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(bgColor.r,bgColor.g,bgColor.b,bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mCamera.position.lerp(cameraTargetPos, 0.5f);

        viewport.apply();
        sceneLoader.getEngine().process();

        if (projectManager.currentProjectVO.box2dDebugRender)
            mBox2DDebugRenderer.render(sceneLoader.getWorld(), mCamera.combined);
    }

    @Override
    public void dispose() {
        super.dispose();
        sceneLoader.dispose();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        sceneLoader.resize(width, height);
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
        cameraTargetPos.set(deltaX, deltaY, 0);

        cameraTargetPos.set(viewport.unproject(vec3Zero.scl(0)).add(viewport.unproject(cameraTargetPos).scl(-1f)));

        cameraTargetPos.add(mCamera.position);
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
