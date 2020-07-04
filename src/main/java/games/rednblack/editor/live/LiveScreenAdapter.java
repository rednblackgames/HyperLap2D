package games.rednblack.editor.live;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import games.rednblack.editor.renderer.resources.IResourceRetriever;

public class LiveScreenAdapter extends Game {

    private LivePreviewScreen livePreviewScreen;
    private final String sceneName;
    private final WorldSizeVO worldSizeVO;
    private final IResourceRetriever resourceManager;

    public LiveScreenAdapter(WorldSizeVO worldSizeVO, IResourceRetriever resourceManager, String sceneName) {
        FileHandle file = Gdx.files.internal("project.dt");
        System.out.println(file.path());
        this.sceneName = sceneName;
        this.worldSizeVO = worldSizeVO;
        this.resourceManager = resourceManager;
    }

    @Override
    public void create () {
        livePreviewScreen = new LivePreviewScreen(worldSizeVO, resourceManager, sceneName);
        setScreen(livePreviewScreen);
    }

    public void loadScene(String sceneName) {
        livePreviewScreen = new LivePreviewScreen(worldSizeVO, resourceManager, sceneName);
        setScreen(livePreviewScreen);
    }

    @Override
    public void dispose() {
        super.dispose();
        livePreviewScreen.dispose();
    }
}
