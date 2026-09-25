package games.rednblack.editor.plugin.performance;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import games.rednblack.editor.plugin.performance.advisor.Advisor;
import games.rednblack.editor.plugin.performance.data.PerformanceModel;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.plugins.H2DWindow;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

import java.util.Map;

/**
 * Opens the profiler window and feeds it.
 * <p>
 * The sampling lives here rather than in the window because a sample belongs to a frame of the <i>editor</i>:
 * the window redraws at its own rate, and reading the frame from there would chart whenever the profiler
 * happened to be painted. So the editor's render notification takes one sample, and the window only draws.
 */
public class PerformanceWindowMediator extends Mediator<PerformanceWindow> {
    private static final String TAG = PerformanceWindowMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    /** Advice is re-read half a second at a time: any faster and it cannot be read at all. */
    private static final int ADVICE_INTERVAL = 30;

    /**
     * How often the window is asked to draw. Sixty a second reads as smooth on any display, and costs the
     * loop a fraction of what drawing on every editor frame would at 165.
     */
    private static final float RENDER_INTERVAL = 1f / 60f;

    private static final int DEFAULT_WIDTH = 720;
    private static final int DEFAULT_HEIGHT = 680;

    private final PerformancePlugin performancePlugin;
    private final TextureAtlas atlas;

    private final PerformanceModel model = new PerformanceModel();
    private final Advisor advisor = new Advisor();

    private H2DWindow window;
    private int sinceAdvice;
    private float sinceRender;

    public PerformanceWindowMediator(PerformancePlugin performancePlugin, TextureAtlas atlas) {
        super(NAME, null);
        this.performancePlugin = performancePlugin;
        this.atlas = atlas;
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.SCENE_LOADED,
                MsgAPI.RENDER,
                PerformancePlugin.PANEL_OPEN);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                model.attach(performancePlugin.getAPI().getEngine(),
                        performancePlugin.getAPI().getSceneLoader(),
                        performancePlugin.getAPI().getUIStage().getBatch());
                if (isOpen() && viewComponent != null) viewComponent.sceneReady();
                break;
            case PerformancePlugin.PANEL_OPEN:
                open();
                break;
            case MsgAPI.RENDER:
                float[] delta = notification.getBody();
                onEditorFrame(delta[0]);
                break;
        }
    }

    private void onEditorFrame(float delta) {
        if (!isOpen()) return;

        model.update();

        if (sinceAdvice-- <= 0) {
            sinceAdvice = ADVICE_INTERVAL;
            advisor.evaluate(model);
        }

        //the window draws only when asked, so the asking happens here, at a rate of its own
        sinceRender += delta;
        if (sinceRender >= RENDER_INTERVAL) {
            sinceRender = 0f;
            window.requestRender();
        }
    }

    private void open() {
        if (isOpen()) {
            window.focus();
            return;
        }

        //started from here, while the editor's window is the current one: the GL profiler wraps whatever
        //graphics is current, and it is the editor's frame that is meant to be measured
        model.setRunning(true);

        Map<String, Object> preferences = performancePlugin.getStorage();
        PerformanceWindow listener = new PerformanceWindow(atlas, model, advisor, preferences);
        setViewComponent(listener);

        window = performancePlugin.getAPI().newWindow(listener, "HyperLap2D - Performance",
                size(preferences, "windowWidth", DEFAULT_WIDTH),
                size(preferences, "windowHeight", DEFAULT_HEIGHT),
                true, this::closed);

        if (window == null) {
            model.setRunning(false);
            Gdx.app.error("PerformancePlugin", "This backend cannot open a window for the profiler");
        }
    }

    private void closed() {
        model.setRunning(false);
        window = null;
    }

    private boolean isOpen() {
        return window != null && window.isOpen();
    }

    /** The config comes back through JSON, so a stored size may return as any kind of number. */
    private static int size(Map<String, Object> preferences, String key, int fallback) {
        Object stored = preferences.get(key);
        if (stored instanceof Number) {
            int value = ((Number) stored).intValue();
            if (value > 400) return value;
        }
        return fallback;
    }
}
