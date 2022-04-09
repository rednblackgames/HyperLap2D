package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import games.rednblack.h2d.extension.tinyvg.TinyVGComponent;
import games.rednblack.h2d.extension.tinyvg.TinyVGVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class TinyVGAsset extends Asset {

    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equals("tvg");
    }

    @Override
    public int getType() {
        return AssetsUtils.TYPE_TINY_VG;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.TINY_VG_DIR_PATH + File.separator + file.nameWithoutExtension() + ".tvg");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        final String targetPath = projectManager.getCurrentProjectPath() + File.separator + ProjectManager.TINY_VG_DIR_PATH;

        for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
            if (!fileHandle.isDirectory() && fileHandle.exists()) {
                String newName = fileHandle.name();
                File target = new File(targetPath + "/" + newName);
                try {
                    FileUtils.copyFile(fileHandle.file(), target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.TINY_VG_DIR_PATH + File.separator + name + ".tvg");
        if (fileHandle.delete()) {
            deleteEntitiesWithImages(root, name);
            deleteAllItemsImages(name);
            return true;
        }

        return false;
    }

    private void deleteAllItemsImages(String name) {
        for (CompositeItemVO compositeItemVO : projectManager.getCurrentProjectInfoVO().libraryItems.values()) {
            deleteAllImagesOfItem(compositeItemVO, name);
        }

        for (SceneVO scene : projectManager.currentProjectInfoVO.scenes) {
            SceneVO loadedScene = resourceManager.getSceneVO(scene.sceneName);
            CompositeItemVO tmpVo = new CompositeItemVO(loadedScene.composite);
            deleteAllImagesOfItem(tmpVo, name);
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
        if (compositeItemVO != null && compositeItemVO.getElementsArray(TinyVGVO.class).size != 0) {
            Array<TinyVGVO> tinyVGVOS = compositeItemVO.getElementsArray(TinyVGVO.class);

            for (TinyVGVO tinyVGVO : tinyVGVOS)
                if (tinyVGVO.getResourceName().equals(imageName))
                    tmpImageList.add(tinyVGVO);

            tinyVGVOS.removeAll(tmpImageList, true);
        }
    }

    private void deleteEntitiesWithImages(int rootEntity, String imageName) {
        tmpEntityList.clear();
        Consumer<Integer> action = (root) -> {
            TinyVGComponent tinyVGComponent = SandboxComponentRetriever.get(root, TinyVGComponent.class);
            if (tinyVGComponent != null && tinyVGComponent.imageName.equals(imageName)) {
                tmpEntityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(tmpEntityList);
    }

    @Override
    public boolean exportAsset(MainItemVO item, ExportMapperVO exportMapperVO, File tmpDir) throws IOException {
        super.exportAsset(item, exportMapperVO, tmpDir);

        TinyVGVO tinyVGVO = (TinyVGVO) item;
        File fileSrc = new File(currentProjectPath + ProjectManager.TINY_VG_DIR_PATH + File.separator + tinyVGVO.imageName + ".tvg");
        FileUtils.copyFileToDirectory(fileSrc, tmpDir);
        exportMapperVO.mapper.add(new ExportMapperVO.ExportedAsset(AssetsUtils.TYPE_TINY_VG, fileSrc.getName()));
        return true;
    }
}
