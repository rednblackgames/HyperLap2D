package games.rednblack.editor.live;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.puremvc.patterns.facade.Facade;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.h2d.extention.spine.SpineItemType;

public class LivePreviewScreen extends ScreenAdapter {
    private Viewport viewport;
    private SceneLoader sceneLoader;

    private final Box2DDebugRenderer mBox2DDebugRenderer;
    private final Facade facade = HyperLap2DFacade.getInstance();
    private final Color bgColor;

    public LivePreviewScreen(WorldSizeVO worldSizeVO, IResourceRetriever resourceManager, String sceneName) {
        mBox2DDebugRenderer = new Box2DDebugRenderer();
        viewport = new ExtendViewport(worldSizeVO.getWorldWidth(), worldSizeVO.getWorldHeight());
        sceneLoader = new SceneLoader(resourceManager);
        sceneLoader.injectExternalItemType(new SpineItemType());
        sceneLoader.loadScene(sceneName, viewport);

        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        bgColor = projectManager.currentProjectVO.backgroundColor;
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(bgColor.r,bgColor.g,bgColor.b,bgColor.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        sceneLoader.getEngine().update(delta);
        mBox2DDebugRenderer.render(sceneLoader.world, viewport.getCamera().combined);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
