package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class SpriteAnimationAtlasAsset extends Asset {

    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(file, file.parent(), false);
            return AssetsUtils.isAtlasAnimationSequence(atlas.getRegions());
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public int getType() {
        return AssetsUtils.TYPE_SPRITE_ANIMATION_ATLAS;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SPRITE_DIR_PATH + File.separator + file.nameWithoutExtension() + File.separator +
                    file.nameWithoutExtension() + ".atlas");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
            String newAnimName;

            try {
                String fileNameWithoutExt = AssetsUtils.getAtlasName(fileHandle);

                String targetPath = projectManager.getCurrentProjectPath() + File.separator
                        + ProjectManager.SPRITE_DIR_PATH + File.separator + fileNameWithoutExt;
                File targetDir = new File(targetPath);
                if (targetDir.exists()) {
                    FileUtils.deleteDirectory(targetDir);
                }

                FileHandle tmpDir = new FileHandle(projectManager.getCurrentProjectPath() + File.separator + "tmp");
                if (tmpDir.exists())
                    FileUtils.forceDelete(tmpDir.file());
                FileUtils.forceMkdir(tmpDir.file());
                AssetsUtils.unpackAtlasIntoTmpFolder(fileHandle.file(), null, tmpDir.path());
                Array<FileHandle> images = new Array<>(tmpDir.list());
                projectManager.copyImageFilesForAllResolutionsIntoProject(images, true, progressHandler);
                FileUtils.forceDelete(tmpDir.file());

                FileUtils.copyFileToDirectory(fileHandle.file(), targetDir);

                TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(fileHandle, fileHandle.parent(), false);
                for (TextureAtlas.TextureAtlasData.Page imageFile : new Array.ArrayIterator<>(atlas.getPages())) {
                    FileUtils.copyFileToDirectory(imageFile.textureFile.file(), targetDir);
                }

                newAnimName = fileNameWithoutExt;
            } catch (IOException e) {
                e.printStackTrace();
                progressHandler.progressFailed();
                return;
            }

            if (newAnimName != null) {
                TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(fileHandle, fileHandle.parent(), false);

                for (TextureAtlas.TextureAtlasData.Region region : new Array.ArrayIterator<>(atlas.getRegions())) {
                    projectManager.getCurrentProjectInfoVO().animationsPacks.get("main").regions.add(region.name);
                }

                resolutionManager.rePackProjectImagesForAllResolutionsSync();
            }
        }
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        for (ResolutionEntryVO resolutionEntryVO : projectManager.getCurrentProjectInfoVO().resolutions) {
            if(!deleteSpriteAnimation(resolutionEntryVO.name, name))
                return false;
        }

        if (deleteSpriteAnimation("orig", name)) {
            postDeleteSpriteAnimation(root, name);
            return true;
        }
        return false;
    }

    private boolean deleteSpriteAnimation(String resolutionName, String spriteName) {
        String spritePath = projectManager.getCurrentProjectPath() + File.separator + ProjectManager.SPRITE_DIR_PATH + File.separator;
        String filePath = spritePath + spriteName;
        FileHandle imagesPath = new FileHandle(projectManager.getCurrentProjectPath() + "/assets/" + resolutionName + "/images" + File.separator);
        String prefix = spriteName + "_";
        for (FileHandle f : imagesPath.list()) {
            if (f.nameWithoutExtension().startsWith(prefix)) {
                f.delete();
            }
        }
        projectManager.deleteRegionFromPack(projectManager.getCurrentProjectInfoVO().animationsPacks, spriteName);
        return AssetsUtils.deleteDirectory(filePath);
    }

    protected void postDeleteSpriteAnimation(int root, String spriteAnimationName) {
        deleteEntitiesWithSpriteAnimation(root, spriteAnimationName);
        deleteAllItemsSpriteAnimations(spriteAnimationName);
    }

    private void deleteAllItemsSpriteAnimations(String spriteAnimationName) {
        for (CompositeItemVO compositeItemVO : projectManager.getCurrentProjectInfoVO().libraryItems.values()) {
            deleteAllSpriteAnimationsOfItem(compositeItemVO, spriteAnimationName);
        }

        for (SceneVO scene : projectManager.currentProjectInfoVO.scenes) {
            SceneVO loadedScene = resourceManager.getSceneVO(scene.sceneName);
            CompositeItemVO tmpVo = new CompositeItemVO(loadedScene.composite);
            deleteAllSpriteAnimationsOfItem(tmpVo, spriteAnimationName);
            loadedScene.composite = tmpVo;
            SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
            sceneDataManager.saveScene(loadedScene);
        }
    }

    private void deleteAllSpriteAnimationsOfItem(CompositeItemVO rootItemVo, String spriteAnimationName) {
        Consumer<CompositeItemVO> action = (currentItemVo) -> deleteCurrentItemSpriteAnimations(currentItemVo, spriteAnimationName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(rootItemVo, action);
    }

    private void deleteCurrentItemSpriteAnimations(CompositeItemVO compositeItemVO, String spriteAnimationName) {
        tmpImageList.clear();
        if (compositeItemVO != null && compositeItemVO.getElementsArray(SpriteAnimationVO.class).size != 0) {
            Array<SpriteAnimationVO> spriteAnimations = compositeItemVO.getElementsArray(SpriteAnimationVO.class);

            for (SpriteAnimationVO spriteVO :spriteAnimations)
                if (spriteVO.getResourceName().equals(spriteAnimationName))
                    tmpImageList.add(spriteVO);

            spriteAnimations.removeAll(tmpImageList, true);
        }
    }

    private void deleteEntitiesWithSpriteAnimation(int rootEntity, String spriteAnimationName) {
        tmpEntityList.clear();
        Consumer<Integer> action = (root) -> {
            SpriteAnimationComponent spriteAnimationComponent = SandboxComponentRetriever.get(root, SpriteAnimationComponent.class);
            if (spriteAnimationComponent != null && spriteAnimationComponent.animationName.equals(spriteAnimationName)) {
                tmpEntityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(tmpEntityList);
    }

    @Override
    public boolean exportAsset(MainItemVO item, ExportMapperVO exportMapperVO, File tmpDir) throws IOException {
        super.exportAsset(item, exportMapperVO, tmpDir);
        SpriteAnimationVO spriteAnimationVO = (SpriteAnimationVO) item;
        File fileSrc = new File(currentProjectPath + ProjectManager.SPRITE_DIR_PATH + File.separator + spriteAnimationVO.animationName);
        FileUtils.copyDirectory(fileSrc, tmpDir);
        exportMapperVO.mapper.add(new ExportMapperVO.ExportedAsset(AssetsUtils.TYPE_SPRITE_ANIMATION_ATLAS, fileSrc.getName() + ".atlas"));
        return true;
    }
}
