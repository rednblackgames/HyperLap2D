package games.rednblack.editor.plugin.skincomposer;

import com.badlogic.gdx.utils.Json;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.h2d.common.network.HttpDownloadUtility;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.network.model.GithubReleaseData;
import org.apache.commons.io.FileUtils;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SkinComposerMediator extends Mediator<DownloadingDialog> {
    private static final String TAG = SkinComposerMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final SkinComposerPlugin plugin;
    private String pluginPath, jarPath;

    public SkinComposerMediator(SkinComposerPlugin plugin) {
        super(NAME, new DownloadingDialog());
        this.plugin = plugin;
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                SkinComposerPlugin.PANEL_OPEN,
                SkinComposerPlugin.DOWNLOAD_JAR
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        pluginPath = plugin.getAPI().getCacheDir();
        jarPath = pluginPath + File.separator + "SkinComposer.jar";

        switch (notification.getName()) {
            case SkinComposerPlugin.PANEL_OPEN:
                if (viewComponent.isOpen) {
                    return;
                }

                if (!plugin.getSettingsVO().alwaysCheckUpdates) {
                    runJar(jarPath);
                    return;
                }

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {

                    viewComponent.show(plugin.getAPI().getUIStage());
                    viewComponent.setMessage("Checking for updates ...");
                    viewComponent.setProgress(0);

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

                        if (!new File(jarPath).exists() || plugin.getStorage().get("latest_update") == null || !plugin.getStorage().get("latest_update").equals(jsonData.tag_name)) {
                            Dialogs.showConfirmDialog(plugin.getAPI().getUIStage(),
                                    "New update found!", "A new version of Skin Composer has found, would you like to download it?",
                                    new String[]{"Later", "Download Now"}, new Integer[]{0, 1}, r -> {
                                        if (r == 1) {
                                            plugin.facade.sendNotification(SkinComposerPlugin.DOWNLOAD_JAR, jsonData);
                                        } else {
                                            viewComponent.progressComplete();
                                            runJar(jarPath);
                                        }
                                    }).padBottom(20).pack();
                        } else {
                            viewComponent.progressComplete();
                            runJar(jarPath);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                executor.shutdown();
                break;
            case SkinComposerPlugin.DOWNLOAD_JAR:
                ExecutorService downloader = Executors.newSingleThreadExecutor();
                downloader.execute(() -> {
                    GithubReleaseData jsonData = notification.getBody();

                    viewComponent.setMessage("Downloading " + jsonData.name + " ...");
                    for (GithubReleaseData.GithubReleaseAssetData assetData : jsonData.assets) {
                        if (assetData.name.equals("SkinComposer.jar")) {
                            try {
                                HttpDownloadUtility.downloadFile(assetData.browser_download_url, pluginPath, viewComponent);
                                plugin.getStorage().put("latest_update", jsonData.tag_name);

                                plugin.facade.sendNotification(MsgAPI.SAVE_EDITOR_CONFIG);
                                runJar(jarPath);
                            } catch (IOException e) {
                                viewComponent.progressFailed();
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                });
                downloader.shutdown();
                break;
        }
    }

    private void runJar(String jarPath) {
        try {
            Runtime.getRuntime().exec(" java -jar " + jarPath);
        } catch (IOException e) {
            viewComponent.progressFailed();
            e.printStackTrace();
        }
    }
}
