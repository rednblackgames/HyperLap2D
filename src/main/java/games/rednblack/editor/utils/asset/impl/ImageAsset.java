package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.components.NinePatchComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.Image9patchVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.renderer.data.SimpleImageVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ImageAsset extends Asset {

    @Override
    public int matchType(Array<FileHandle> files) {
        String[] names = new String[files.size];
        for (int i = 0; i < files.size; i++) {
            names[i] = files.get(i).nameWithoutExtension();
        }

        return ImportUtils.isAnimationSequence(names) ? ImportUtils.TYPE_UNKNOWN : super.matchType(files);
    }

    @Override
    public int getType() {
        return ImportUtils.TYPE_IMAGE;
    }

    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equalsIgnoreCase("png");
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
            projectManager.getCurrentProjectInfoVO().imagesPacks.get("main").regions.add(handle.nameWithoutExtension());
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
    }

    private void deleteAllImagesOfItem(CompositeItemVO compositeItemVO, String imageName) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> deleteCurrentItemImage(rootItemVo, imageName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void deleteCurrentItemImage(CompositeItemVO compositeItemVO, String imageName) {
        tmpImageList.clear();
        if (compositeItemVO.composite != null && compositeItemVO.composite.sImages.size() != 0) {
            ArrayList<SimpleImageVO> simpleImageVOs = compositeItemVO.composite.sImages;
            tmpImageList.addAll(simpleImageVOs
                    .stream()
                    .filter(simpleImageVO -> simpleImageVO.imageName.equals(imageName))
                    .collect(Collectors.toList()));
            simpleImageVOs.removeAll(tmpImageList);
        }

        tmpImageList.clear();
        if (compositeItemVO.composite != null && compositeItemVO.composite.sImage9patchs.size() != 0) {
            ArrayList<Image9patchVO> simple9PatchesVOs = compositeItemVO.composite.sImage9patchs;
            tmpImageList.addAll(simple9PatchesVOs
                    .stream()
                    .filter(simple9PatchVO -> simple9PatchVO.imageName.equals(imageName))
                    .collect(Collectors.toList()));
            simple9PatchesVOs.removeAll(tmpImageList);
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
}
