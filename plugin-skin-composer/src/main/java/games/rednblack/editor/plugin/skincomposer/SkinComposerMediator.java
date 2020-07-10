package games.rednblack.editor.plugin.skincomposer;

import com.badlogic.gdx.utils.Json;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SkinComposerMediator  extends SimpleMediator<DownloadingDialog> {
    private static final String TAG = SkinComposerMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final SkinComposerPlugin plugin;

    public SkinComposerMediator(SkinComposerPlugin plugin) {
        super(NAME, new DownloadingDialog());
        this.plugin = plugin;
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                SkinComposerPlugin.PANEL_OPEN
        };
    }


    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case SkinComposerPlugin.PANEL_OPEN:
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String pluginPath = plugin.getAPI().getCacheDir();
                    String jarPath = pluginPath + File.separator + "SkinComposer.jar";
                    if (new File(jarPath).exists()) {
                        try {
                            Runtime.getRuntime().exec(" java -jar " + jarPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    viewComponent.show(plugin.getAPI().getUIStage());

                    try {
                        FileUtils.forceMkdir(new File(pluginPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        String data = HttpDownloadUtility.downloadToString("https://api.github.com/repos/raeleus/skin-composer/releases/latest");
                        Json json = new Json();
                        json.setIgnoreUnknownFields(true);
                        GithubReleaseData jsonData = json.fromJson(GithubReleaseData.class, data);
                        viewComponent.setCurrentVersion(jsonData.name);
                        for (GithubReleaseData.GithubReleaseAssetData assetData : jsonData.assets) {
                            if (assetData.name.equals("SkinComposer.jar")) {
                                try {
                                    HttpDownloadUtility.downloadFile(assetData.browser_download_url, pluginPath, viewComponent);
                                } catch (IOException e) {
                                    viewComponent.progressFailed();
                                    e.printStackTrace();
                                }
                                break;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        Runtime.getRuntime().exec(" java -jar " + jarPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                executor.shutdown();
                break;
        }
    }
}
