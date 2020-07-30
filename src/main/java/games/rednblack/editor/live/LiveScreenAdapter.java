package games.rednblack.editor.live;

import com.badlogic.gdx.Game;

public class LiveScreenAdapter extends Game {

    private LivePreviewScreen livePreviewScreen;

    @Override
    public void create () {
        livePreviewScreen = new LivePreviewScreen();
        setScreen(livePreviewScreen);
    }

    @Override
    public void dispose() {
        super.dispose();
        livePreviewScreen.dispose();
    }
}
