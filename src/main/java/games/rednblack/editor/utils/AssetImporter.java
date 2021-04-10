package games.rednblack.editor.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.asset.impl.*;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.panel.ImportPanel;
import games.rednblack.editor.view.ui.panel.ImportPanelMediator;
import games.rednblack.h2d.common.ProgressHandler;

import java.io.File;

public class AssetImporter {

    private static AssetImporter sInstance;
    private ImportPanelMediator.AssetsImportProgressHandler progressHandler;
    private ImportPanel viewComponent;

    private final Array<Asset> assetDescriptors = new Array<>();

    public static AssetImporter getInstance() {
        if (sInstance == null) {
            sInstance = new AssetImporter();
            sInstance.assetDescriptors.add(new ImageAsset());
            sInstance.assetDescriptors.add(new AtlasAsset());
            sInstance.assetDescriptors.add(new ParticleEffectAsset());
            sInstance.assetDescriptors.add(new TalosVFXAsset());
            sInstance.assetDescriptors.add(new SpineAsset());
            sInstance.assetDescriptors.add(new SpriteAnimationAtlasAsset());
            sInstance.assetDescriptors.add(new SpriteAnimationSequenceAsset());
            sInstance.assetDescriptors.add(new ShaderAsset());
            sInstance.assetDescriptors.add(new HyperLap2DInternalLibraryAsset());
            sInstance.assetDescriptors.add(new HyperLap2DLibraryAsset());
            sInstance.assetDescriptors.add(new HyperLap2DActionAsset());
        }
        return sInstance;
    }

    private AssetImporter() {

    }

    public void setProgressHandler(ImportPanelMediator.AssetsImportProgressHandler handler) {
        progressHandler = handler;
    }

    public void setViewComponent(ImportPanel component) {
        viewComponent = component;
    }

    public void postPathObtainAction(String[] paths) {
        int fileType = ImportUtils.TYPE_UNKNOWN;

        Array<FileHandle> files = getFilesFromPaths(paths);
        for (Asset asset : assetDescriptors) {
            fileType = asset.matchType(files);
            if (fileType > 0) {
                if (asset.checkExistence(files)) {
                    int type = fileType;
                    Dialogs.showConfirmDialog(Sandbox.getInstance().getUIStage(),
                            "Duplicate file", "You have already an asset with this name,\nwould you like to overwrite it?",
                            new String[]{"Overwrite", "Cancel"}, new Integer[]{0, 1}, result -> {
                                if (result == 0) {
                                    initImportUI(type, files);
                                    asset.asyncImport(files, progressHandler,false);
                                }
                            }).padBottom(20).pack();
                } else {
                    initImportUI(fileType, files);
                    asset.asyncImport(files, progressHandler,false);
                }
                break;
            }
        }

        if (fileType <= 0) {
            viewComponent.showError(fileType);
        }
    }

    private void initImportUI(int type, Array<FileHandle> files) {
        SettingsManager settingsManager = HyperLap2DFacade.getInstance().retrieveProxy(SettingsManager.NAME);
        settingsManager.setLastImportedPath(files.get(0).parent().path());

        int count = (type != ImportUtils.TYPE_ANIMATION_PNG_SEQUENCE) ? files.size : 1;

        viewComponent.setImportingView(type, count);
    }

    private final Array<FileHandle> tmp = new Array<>();

    public void importInternalResource(FileHandle file, ProgressHandler progressHandler) {
        tmp.clear();
        tmp.add(file);

        for (Asset asset : new Array.ArrayIterator<>(assetDescriptors)) {
            if (asset.matchType(tmp) > 0) {
                asset.asyncImport(tmp, progressHandler, true);
                break;
            }
        }
    }

    private  Array<FileHandle> getFilesFromPaths(String[] paths) {
        Array<FileHandle> files = new Array<>();
        for (String path : paths) {
            files.add(new FileHandle(new File(path)));
        }

        return files;
    }
}
