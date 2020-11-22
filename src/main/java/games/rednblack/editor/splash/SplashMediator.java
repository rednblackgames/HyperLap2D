package games.rednblack.editor.splash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.h2d.common.network.HttpDownloadUtility;
import games.rednblack.h2d.common.network.model.GithubReleaseData;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SplashMediator extends Mediator<Object> {

    private static final String TAG = SplashMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public SplashMediator() {
        super(NAME, new Object());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                SplashScreenAdapter.UPDATE_SPLASH,
                SplashScreenAdapter.CLOSE_SPLASH
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        System.out.println(notification.getBody().toString());

        SplashScreenAdapter splash = (SplashScreenAdapter) (HyperLap2DApp.getInstance().splashWindow.getListener());

        if (HyperLap2DApp.getInstance().splashWindow != null && HyperLap2DApp.getInstance().mainWindow != null) {
            splash.setProgressStatus(notification.getBody().toString());

            if (notification.getName().equals(SplashScreenAdapter.CLOSE_SPLASH)) {
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    splash.setProgressStatus("Checking for updates...");
                    try {
                        String data = HttpDownloadUtility.downloadToString("https://api.github.com/repos/rednblackgames/HyperLap2D/releases/latest");
                        Json json = new Json();
                        json.setIgnoreUnknownFields(true);
                        GithubReleaseData jsonData = json.fromJson(GithubReleaseData.class, data);
                        int latestVer = Integer.parseInt(jsonData.tag_name.replace("v", "").replace(".", ""));
                        int currVer = Integer.parseInt(AppConfig.getInstance().version.replaceAll("[^0-9]", ""));
                        if (latestVer > currVer) {
                            boolean result = TinyFileDialogs.tinyfd_messageBox("New update found!",
                                    "A new version of HyperLap2D has found, would you like to download it?",
                                    "yesno", "info", true);
                            if (result) {
                                Gdx.net.openURI("https://github.com/rednblackgames/HyperLap2D/releases");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    splash.loadedData();
                    HyperLap2DApp.getInstance().mainWindow.setVisible(true);
                });
                executor.shutdown();
            }

            HyperLap2DApp.getInstance().splashWindow.focusWindow();
        }
    }
}
