package games.rednblack.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationGLESFix;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.apache.commons.lang3.SystemUtils;

public class HyperLap2DApp extends ApplicationAdapter {
    private static HyperLap2DApp sInstance = null;

    public static HyperLap2DApp initInstance(double width, double height, SettingsManager settingsManager) {
        if (sInstance == null) {
            sInstance = new HyperLap2DApp(width, height, settingsManager);
        }
        return sInstance;
    }

    public static HyperLap2DApp getInstance() {
        return sInstance;
    }

    public HyperLap2D hyperlap2D;
    public Lwjgl3Window splashWindow, mainWindow;
    final private double windowWidth, windowHeight;
    private final SettingsManager settingsManager;

    private HyperLap2DApp(double width, double height, SettingsManager settingsManager) {
        windowWidth = width;
        windowHeight = height;
        this.settingsManager = settingsManager;
    }

    @Override
    public void create() {
        Lwjgl3ApplicationGLESFix app = (Lwjgl3ApplicationGLESFix) Gdx.app;

        StandardWidgetsFactory.init(HyperLap2DFacade.getInstance());

        Lwjgl3ApplicationConfiguration config2 = new Lwjgl3ApplicationConfiguration();
        config2.setWindowedMode(467, 385);
        config2.setTitle("HyperLap2D");
        config2.setResizable(false);
        config2.setDecorated(false);
        config2.setIdleFPS(60);
        config2.setForegroundFPS(settingsManager.editorConfigVO.fpsLimit);
        config2.useVsync(false);
        config2.setWindowIcon("hyperlap_icon_96.png");

        splashWindow = app.newWindow(new SplashScreenAdapter(), config2);

        Gdx.app.postRunnable(() -> {
            hyperlap2D = new HyperLap2D(settingsManager);

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("HyperLap2D - Beta v" + AppConfig.getInstance().versionString);
            config.setResizable(true);
            if (!SystemUtils.IS_OS_WINDOWS)
                config.setWindowedMode((int) (windowWidth), (int) (windowHeight));
            config.setIdleFPS(60);
            config.setForegroundFPS(settingsManager.editorConfigVO.fpsLimit);
            config.useVsync(false);
            config.setInitialVisible(false);
            config.setMaximized(true);
            config.setWindowIcon("hyperlap_icon_96.png");
            config.setWindowSizeLimits(920, 720, -1, -1);

            mainWindow = app.newWindow(hyperlap2D, config);
            mainWindow.setWindowListener(hyperlap2D);
            if (SystemUtils.IS_OS_WINDOWS) {
                Gdx.app.postRunnable(() -> HyperLap2DUtils.overwriteWindowProc2(mainWindow.getWindowHandle()));
            }
            if (SystemUtils.IS_OS_MAC) {
               Gdx.app.postRunnable(() -> HyperLap2DUtils.setCocoaCustomTitleBar(mainWindow.getWindowHandle(), true));
            }
        });
    }

    public void showUISplashWindow() {
        if (splashWindow != null)
            splashWindow.closeWindow();
        Lwjgl3ApplicationGLESFix app = (Lwjgl3ApplicationGLESFix) Gdx.app;

        Lwjgl3ApplicationConfiguration config2 = new Lwjgl3ApplicationConfiguration();
        config2.setWindowedMode(467, 385);
        config2.setTitle("HyperLap2D");
        config2.setResizable(false);
        config2.setDecorated(false);
        config2.setIdleFPS(60);
        config2.setForegroundFPS(settingsManager.editorConfigVO.fpsLimit);
        config2.useVsync(false);
        config2.setWindowIcon("hyperlap_icon_96.png");

        SplashScreenAdapter adapter = new SplashScreenAdapter();
        adapter.setLoading(false);

        splashWindow = app.newWindow(adapter, config2);
    }
}
