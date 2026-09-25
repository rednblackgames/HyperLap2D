package games.rednblack.editor.plugin.performance;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import games.rednblack.editor.plugin.performance.advisor.Advisor;
import games.rednblack.editor.plugin.performance.data.PerformanceModel;
import games.rednblack.editor.plugin.performance.widget.Shapes;

import java.util.Map;

/**
 * The profiler's own window.
 * <p>
 * It has its own stage and its own batch, and not for tidiness: windows of one application share a GL
 * context, and a shared context shares textures and shaders but not vertex array objects. The editor's
 * batch cannot draw here. The skin can, which is why this looks like the rest of the editor.
 * <p>
 * Being a window of its own is the point: what it draws is no longer part of the frame it is reporting on.
 */
public class PerformanceWindow implements ApplicationListener {

    private static final Color BACKGROUND = new Color(0.13f, 0.14f, 0.15f, 1f);

    private final TextureAtlas atlas;
    private final PerformanceModel model;
    private final Advisor advisor;
    private final Map<String, Object> preferences;

    private Stage stage;
    private PolygonSpriteBatch batch;
    private PerformanceView view;

    /** What this window costs to draw, smoothed. It is not part of the editor's frame, but it is not free. */
    private float renderMillis;

    public PerformanceWindow(TextureAtlas atlas, PerformanceModel model, Advisor advisor, Map<String, Object> preferences) {
        this.atlas = atlas;
        this.model = model;
        this.advisor = advisor;
        this.preferences = preferences;
    }

    @Override
    public void create() {
        batch = new PolygonSpriteBatch();
        stage = new Stage(new ScreenViewport(), batch);

        Shapes.setPixel(atlas == null ? null : atlas.findRegion("white-pixel"));

        view = new PerformanceView(atlas, model, advisor, preferences);
        view.setFillParent(true);
        stage.addActor(view);

        Gdx.input.setInputProcessor(stage);

        //only when asked. Redrawing at the editor's rate means a buffer swap per editor frame, and on a
        //composited desktop that second swap paces the whole loop - the editor's frame rate with it
        Gdx.graphics.setContinuousRendering(false);
    }

    @Override
    public void render() {
        long startedAt = TimeUtils.nanoTime();

        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, BACKGROUND.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (view != null) view.setWindowMillis(renderMillis);

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 0.1f));
        stage.draw();

        renderMillis += ((TimeUtils.nanoTime() - startedAt) / 1000000f - renderMillis) * 0.2f;
    }

    @Override
    public void resize(int width, int height) {
        if (width == 0 || height == 0) return;

        stage.getViewport().update(width, height, true);
        preferences.put("windowWidth", width);
        preferences.put("windowHeight", height);
    }

    /** A scene was opened while the window was already up. */
    public void sceneReady() {
        if (view != null) view.sceneReady();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        if (view != null) view.savePreferences();
        //the stage was handed a batch, so it does not own it, so it will not dispose it
        if (stage != null) stage.dispose();
        if (batch != null) batch.dispose();

        view = null;
        stage = null;
        batch = null;
    }
}
