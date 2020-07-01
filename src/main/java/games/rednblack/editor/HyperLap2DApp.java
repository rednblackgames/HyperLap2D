package games.rednblack.editor;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import games.rednblack.editor.splash.SplashScreenAdapter;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.h2d.common.MsgAPI;
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
        config2.setWindowedMode(467, 415);
        config2.setTitle("HyperLap2D");
        config2.setResizable(false);
        config2.setDecorated(false);
        config2.useVsync(false);
        config2.setIdleFPS(60);
        config2.setWindowIcon("hyperlap_icon_96.png");

        splashWindow = app.newWindow(new SplashScreenAdapter(), config2);

        hyperlap2D = new HyperLap2D();

        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                hyperlap2D = new HyperLap2D();

                Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
                config.setTitle("HyperLap2D - Public Alpha v" + AppConfig.getInstance().version);
                config.setResizable(true);
                config.setWindowedMode((int) (windowWidth), (int) (windowHeight));
                config.setIdleFPS(60);
                config.setInitialVisible(false);
                config.setMaximized(true);
                config.setWindowIcon("hyperlap_icon_96.png");
                config.setWindowSizeLimits(920, 800, -1, -1);
                if (SystemUtils.IS_OS_WINDOWS)
                    config.setWindowPosition(0, (int) (windowHeight * .04));

                mainWindow = app.newWindow(hyperlap2D, config);
                mainWindow.setWindowListener(new Lwjgl3WindowListener() {
                    @Override
                    public void created(Lwjgl3Window window) {

                    }

                    @Override
                    public void iconified(boolean isIconified) {

                    }

                    @Override
                    public void maximized(boolean isMaximized) {

                    }

                    @Override
                    public void focusLost() {

                    }

                    @Override
                    public void focusGained() {

                    }

                    @Override
                    public boolean closeRequested() {
                        hyperlap2D.sendNotification(MsgAPI.APP_EXIT);
                        return false;
                    }

                    @Override
                    public void filesDropped(String[] files) {

                    }

                    @Override
                    public void refreshRequested() {

                    }
                });
            }
        });
    }
}
