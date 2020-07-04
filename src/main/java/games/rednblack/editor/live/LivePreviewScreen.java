package games.rednblack.editor.live;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.resources.IResourceRetriever;

public class LivePreviewScreen extends ScreenAdapter {
    private Viewport viewport;
    private SceneLoader sceneLoader;

    public LivePreviewScreen(WorldSizeVO worldSizeVO, IResourceRetriever resourceManager, String sceneName) {
        viewport = new StretchViewport(worldSizeVO.getWorldWidth(), worldSizeVO.getWorldHeight());
        sceneLoader = new SceneLoader(resourceManager);
        sceneLoader.loadScene(sceneName, viewport);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        Gdx.gl.glClearColor(0,0,0,0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        sceneLoader.getEngine().update(delta);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
