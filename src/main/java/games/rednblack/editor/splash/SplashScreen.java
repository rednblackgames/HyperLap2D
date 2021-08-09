package games.rednblack.editor.splash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

public class SplashScreen extends ScreenAdapter {

    private SplashStage stage;

    public SplashScreen(boolean loading) {
        stage = new SplashStage(loading);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    public void loadedData() {
        stage.loadedData();
    }

    public void setProgressStatus(String status) {
        stage.setProgressStatus(status);
    }
}
