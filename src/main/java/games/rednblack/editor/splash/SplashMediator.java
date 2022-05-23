package games.rednblack.editor.splash;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.utils.Version;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.h2d.common.network.HttpDownloadUtility;
import games.rednblack.h2d.common.network.model.GithubReleaseData;
import games.rednblack.h2d.common.network.model.SnapshotReleaseData;
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
                        Version latestVer, currVer;
                        if (AppConfig.getInstance().build != null) {
                            String data = HttpDownloadUtility.downloadToString("https://hyperlap2d.rednblack.games/upload/snapshots/snapshot.json");
                            Json json = new Json();
                            json.setIgnoreUnknownFields(true);
                            SnapshotReleaseData jsonData = json.fromJson(SnapshotReleaseData.class, data);
                            latestVer = new Version(String.valueOf(jsonData.build));
                            currVer = new Version(AppConfig.getInstance().build);
                        } else {
                            String data = HttpDownloadUtility.downloadToString("https://api.github.com/repos/rednblackgames/HyperLap2D/releases/latest");
                            Json json = new Json();
                            json.setIgnoreUnknownFields(true);
                            GithubReleaseData jsonData = json.fromJson(GithubReleaseData.class, data);
                            latestVer = new Version(jsonData.tag_name.replace("v", ""));
                            currVer = AppConfig.getInstance().version;
                        }

                        if (latestVer.compareTo(currVer) > 0) {
                            boolean result = TinyFileDialogs.tinyfd_messageBox("New update found!",
                                    "A new version of HyperLap2D has been found " + latestVer.get() + " (current: " + currVer.get() + "), would you like to download it?",
                                    "yesno", "info", true);
                            if (result) {
                                Gdx.net.openURI("https://hyperlap2d.rednblack.games/download");
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
