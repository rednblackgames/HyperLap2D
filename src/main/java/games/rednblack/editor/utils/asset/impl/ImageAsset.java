package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.renderer.components.NinePatchComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class ImageAsset extends Asset {

    @Override
    public int matchType(Array<FileHandle> files) {
        String[] names = new String[files.size];
        for (int i = 0; i < files.size; i++) {
            names[i] = files.get(i).nameWithoutExtension();
        }

        return AssetsUtils.isAnimationSequence(names) ? AssetsUtils.TYPE_UNKNOWN : super.matchType(files);
    }

    @Override
    public int getType() {
        return AssetsUtils.TYPE_IMAGE;
    }

    @Override
    protected boolean matchMimeType(FileHandle file) {
        if (file.extension().equalsIgnoreCase("png")) {
            try {
                Pixmap image = new Pixmap(file);
                int width = image.getWidth();
                int height = image.getHeight();
                TexturePacker.Settings settings = projectManager.getTexturePackerSettings();
                if (width > settings.maxWidth - settings.paddingX || height > settings.maxHeight - settings.paddingY) {
                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                            "Provided image exceeds atlas limits (" + (settings.maxWidth - settings.paddingX) + "x" + (settings.maxHeight - settings.paddingY)
                                    + ")\nPlease, resize images to the correct size.").padBottom(20).pack();
                    return false;
                }
                return true;
            } catch (Exception ignore) {
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.IMAGE_DIR_PATH + File.separator + file.nameWithoutExtension() + ".png");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        projectManager.copyImageFilesForAllResolutionsIntoProject(files, true, progressHandler);

        if (!skipRepack) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        }

        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            projectManager.getCurrentProjectInfoVO().imagesPacks.get("main").regions.add(handle.nameWithoutExtension().replace(".9", ""));
        }
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        for (ResolutionEntryVO resolutionEntryVO : projectManager.getCurrentProjectInfoVO().resolutions) {
            if(!deleteSingleImage(resolutionEntryVO.name, name))
                return false;
        }

        if (deleteSingleImage("orig", name)) {
            postDeleteImage(root, name);
            return true;
        }

        return false;
    }

    private boolean deleteSingleImage(String resolutionName, String imageName) {
        String imagesPath = projectManager.getCurrentProjectPath() + "/assets/" + resolutionName + "/images" + File.separator;
        String filePath = imagesPath + imageName + ".png";
        projectManager.deleteRegionFromPack(projectManager.getCurrentProjectInfoVO().imagesPacks, imageName);
        if (!(new File(filePath)).delete()) {
            filePath = imagesPath + imageName + ".9.png";
            return (new File(filePath)).delete();
        }
        return true;
    }

    /**
     *  Clear scenes and library items that contains deleted image
     * @param imageName image to delete
     */
    protected void postDeleteImage(int root, String imageName) {
        deleteEntitiesWithImages(root, imageName);
        deleteAllItemsImages(imageName);
    }

    private void deleteAllItemsImages(String imageName) {
        for (CompositeItemVO compositeItemVO : projectManager.getCurrentProjectInfoVO().libraryItems.values()) {
            deleteAllImagesOfItem(compositeItemVO, imageName);
        }

        for (SceneVO scene : projectManager.currentProjectInfoVO.scenes) {
            SceneVO loadedScene = resourceManager.getSceneVO(scene.sceneName);
            CompositeItemVO tmpVo = new CompositeItemVO(loadedScene.composite);
            deleteAllImagesOfItem(tmpVo, imageName);
            loadedScene.composite = tmpVo;
            SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
            sceneDataManager.saveScene(loadedScene);
        }
    }

    private void deleteAllImagesOfItem(CompositeItemVO compositeItemVO, String imageName) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> deleteCurrentItemImage(rootItemVo, imageName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void deleteCurrentItemImage(CompositeItemVO compositeItemVO, String imageName) {
        tmpImageList.clear();
        if (compositeItemVO != null && compositeItemVO.getElementsArray(SimpleImageVO.class).size != 0) {
            Array<SimpleImageVO> simpleImageVOs = compositeItemVO.getElementsArray(SimpleImageVO.class);

            for (SimpleImageVO simpleImageVO : simpleImageVOs)
                if (simpleImageVO.getResourceName().equals(imageName))
                    tmpImageList.add(simpleImageVO);

            simpleImageVOs.removeAll(tmpImageList, true);
        }

        tmpImageList.clear();
        if (compositeItemVO != null && compositeItemVO.getElementsArray(Image9patchVO.class).size != 0) {
            Array<Image9patchVO> simple9PatchesVOs = compositeItemVO.getElementsArray(Image9patchVO.class);

            for (Image9patchVO simpleImageVO : simple9PatchesVOs)
                if (simpleImageVO.getResourceName().equals(imageName))
                    tmpImageList.add(simpleImageVO);

            simple9PatchesVOs.removeAll(tmpImageList, true);
        }
    }

    private void deleteEntitiesWithImages(int rootEntity, String regionName) {
        tmpEntityList.clear();
        Consumer<Integer> action = (root) -> {
            TextureRegionComponent regionComponent = SandboxComponentRetriever.get(root, TextureRegionComponent.class);
            if (regionComponent != null && regionComponent.regionName.equals(regionName)) {
                tmpEntityList.add(root);
            }

            NinePatchComponent ninePatchComponent = SandboxComponentRetriever.get(root, NinePatchComponent.class);
            if (ninePatchComponent != null && ninePatchComponent.textureRegionName.equals(regionName)) {
                tmpEntityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(tmpEntityList);
    }

    @Override
    public boolean exportAsset(MainItemVO item, ExportMapperVO exportMapperVO, File tmpDir) throws IOException {
        super.exportAsset(item, exportMapperVO, tmpDir);
        if (item instanceof SimpleImageVO imageVO) {
            File fileSrc = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + imageVO.imageName + ".png");
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportMapperVO.ExportedAsset(AssetsUtils.TYPE_IMAGE, fileSrc.getName()));
        } else if (item instanceof Image9patchVO imageVO) {
            File fileSrc = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + imageVO.imageName + ".9.png");
            FileUtils.copyFileToDirectory(fileSrc, tmpDir);
            exportMapperVO.mapper.add(new ExportMapperVO.ExportedAsset(AssetsUtils.TYPE_IMAGE, fileSrc.getName()));
        }

        return true;
    }
}
