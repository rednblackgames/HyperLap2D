package games.rednblack.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.editor.utils.AppConfig;
import org.apache.commons.lang3.SystemUtils;

public class HyperLap2DApp extends ApplicationAdapter {
    private static HyperLap2DApp sInstance = null;

    public static HyperLap2DApp initInstance(double width, double height) {
        if (sInstance == null) {
            sInstance = new HyperLap2DApp(width, height);
        }
        return sInstance;
    }

    public static HyperLap2DApp getInstance() {
        return sInstance;
    }

    public HyperLap2D hyperlap2D;
    public Lwjgl3Window splashWindow, mainWindow;
    final private double windowWidth, windowHeight;

    private HyperLap2DApp(double width, double height) {
        windowWidth = width;
        windowHeight = height;
    }

    @Override
    public void create() {
        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;

        Lwjgl3ApplicationConfiguration config2 = new Lwjgl3ApplicationConfiguration();
        config2.setWindowedMode(467, 385);
        config2.setTitle("HyperLap2D");
        config2.setResizable(false);
        config2.setDecorated(false);
        config2.setIdleFPS(60);
        config2.setForegroundFPS(60);
        config2.useVsync(false);
        config2.setWindowIcon("hyperlap_icon_96.png");

        splashWindow = app.newWindow(new SplashScreenAdapter(), config2);

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                hyperlap2D = new HyperLap2D();

                Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
                config.setTitle("HyperLap2D - Public Alpha v" + AppConfig.getInstance().versionString);
                config.setResizable(true);
                config.setWindowedMode((int) (windowWidth), (int) (windowHeight));
                config.setIdleFPS(60);
                config.setForegroundFPS(60);
                config.useVsync(false);
                config.setInitialVisible(false);
                config.setMaximized(true);
                config.setWindowIcon("hyperlap_icon_96.png");
                config.setWindowSizeLimits(920, 800, -1, -1);
                if (SystemUtils.IS_OS_WINDOWS)
                    config.setWindowPosition(0, (int) (windowHeight * .04));

                mainWindow = app.newWindow(hyperlap2D, config);
                mainWindow.setWindowListener(hyperlap2D);
            }
        });
    }

    public void showUISplashWindow() {
        if (splashWindow != null)
            splashWindow.closeWindow();
        Lwjgl3Application app = (Lwjgl3Application) Gdx.app;

        Lwjgl3ApplicationConfiguration config2 = new Lwjgl3ApplicationConfiguration();
        config2.setWindowedMode(467, 385);
        config2.setTitle("HyperLap2D");
        config2.setResizable(false);
        config2.setDecorated(false);
        config2.setIdleFPS(60);
        config2.setForegroundFPS(60);
        config2.useVsync(false);
        config2.setWindowIcon("hyperlap_icon_96.png");

        SplashScreenAdapter adapter = new SplashScreenAdapter();
        adapter.setLoading(false);

        splashWindow = app.newWindow(adapter, config2);
    }
}
