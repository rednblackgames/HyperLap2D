package games.rednblack.editor.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.asset.impl.*;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.panel.ImportPanel;
import games.rednblack.editor.view.ui.panel.ImportPanelMediator;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import games.rednblack.h2d.extension.spine.SpineVO;
import games.rednblack.h2d.extension.talos.TalosVO;
import games.rednblack.h2d.extension.tinyvg.TinyVGVO;

import java.io.File;
import java.io.IOException;

public class AssetIOManager {

    private static AssetIOManager sInstance;
    private ImportPanelMediator.AssetsImportProgressHandler progressHandler;
    private ImportPanel viewComponent;

    private final Array<Asset> assetDescriptors = new Array<>();
    private final ObjectMap<Class<? extends  MainItemVO>, Integer> dataClassExportMap = new ObjectMap<>();

    public static AssetIOManager getInstance() {
        if (sInstance == null) {
            sInstance = new AssetIOManager();
            sInstance.assetDescriptors.add(new ImageAsset());
            sInstance.assetDescriptors.add(new AtlasAsset());
            sInstance.assetDescriptors.add(new ParticleEffectAsset());
            sInstance.assetDescriptors.add(new TalosVFXAsset());
            sInstance.assetDescriptors.add(new TinyVGAsset());
            sInstance.assetDescriptors.add(new SpineAsset());
            sInstance.assetDescriptors.add(new SpriteAnimationAtlasAsset());
            sInstance.assetDescriptors.add(new SpriteAnimationSequenceAsset());
            sInstance.assetDescriptors.add(new ShaderAsset());
            sInstance.assetDescriptors.add(new BitmapFontAsset());
            sInstance.assetDescriptors.add(new HyperLap2DInternalLibraryAsset());
            sInstance.assetDescriptors.add(new HyperLap2DLibraryAsset());
            sInstance.assetDescriptors.add(new HyperLap2DActionAsset());

            sInstance.dataClassExportMap.put(SimpleImageVO.class, AssetsUtils.TYPE_IMAGE);
            sInstance.dataClassExportMap.put(Image9patchVO.class, AssetsUtils.TYPE_IMAGE);
            sInstance.dataClassExportMap.put(SpineVO.class, AssetsUtils.TYPE_SPINE_ANIMATION);
            sInstance.dataClassExportMap.put(SpriteAnimationVO.class, AssetsUtils.TYPE_SPRITE_ANIMATION_ATLAS);
            sInstance.dataClassExportMap.put(ParticleEffectVO.class, AssetsUtils.TYPE_PARTICLE_EFFECT);
            sInstance.dataClassExportMap.put(TalosVO.class, AssetsUtils.TYPE_TALOS_VFX);
            sInstance.dataClassExportMap.put(LabelVO.class, AssetsUtils.TYPE_BITMAP_FONT);
            sInstance.dataClassExportMap.put(TinyVGVO.class, AssetsUtils.TYPE_TINY_VG);
        }
        return sInstance;
    }

    private AssetIOManager() {

    }

    public void setProgressHandler(ImportPanelMediator.AssetsImportProgressHandler handler) {
        progressHandler = handler;
    }

    public void setViewComponent(ImportPanel component) {
        viewComponent = component;
    }

    public void postPathObtainAction(String[] paths) {
        int fileType = AssetsUtils.TYPE_UNKNOWN;

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

        int count = (type != AssetsUtils.TYPE_ANIMATION_PNG_SEQUENCE) ? files.size : 1;

        viewComponent.setImportingView(type, count);
    }

    public void importInternalResource(FileHandle file, ProgressHandler progressHandler) {
        Array<FileHandle> tmp = new Array<>();
        tmp.add(file);

        boolean assetFound = false;
        for (Asset asset : new Array.ArrayIterator<>(assetDescriptors)) {
            if (asset.matchType(tmp) > 0) {
                asset.asyncImport(tmp, progressHandler, true);
                assetFound = true;
                break;
            }
        }

        if (!assetFound) progressHandler.progressFailed();
    }

    private  Array<FileHandle> getFilesFromPaths(String[] paths) {
        Array<FileHandle> files = new Array<>();
        for (String path : paths) {
            files.add(new FileHandle(new File(path)));
        }

        return files;
    }

    private Asset getDescriptorFor(int type) {
        for (Asset asset : new Array.ArrayIterator<>(assetDescriptors)) {
            if (asset.getType() == type)
                return asset;
        }
        return null;
    }

    public boolean deleteAsset(int type, int root, String name) {
        Asset asset = getDescriptorFor(type);
        if (asset != null)
            return asset.deleteAsset(root, name);
        return false;
    }

    public boolean deleteAsset(int root, String name) {
        for (Asset asset : new Array.ArrayIterator<>(assetDescriptors)) {
            if (asset.deleteAsset(root, name))
                return true;
        }
        return false;
    }

    public boolean exportAsset(MainItemVO itemVO, ExportMapperVO exportMapperVO, File tmpDir) {
        try {
            return exportAsset(dataClassExportMap.get(itemVO.getClass()), itemVO, exportMapperVO, tmpDir);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean exportAsset(int type, MainItemVO itemVO, ExportMapperVO exportMapperVO, File tmpDir) throws IOException {
        Asset asset = getDescriptorFor(type);
        if (asset != null)
            return asset.exportAsset(itemVO, exportMapperVO, tmpDir);
        return false;
    }
}
