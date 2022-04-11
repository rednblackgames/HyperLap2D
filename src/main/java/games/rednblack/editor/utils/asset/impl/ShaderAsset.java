package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class ShaderAsset extends Asset {

    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equalsIgnoreCase("frag") || file.extension().equalsIgnoreCase("vert");
    }

    @Override
    public int getType() {
        return AssetsUtils.TYPE_SHADER;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle frag = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SHADER_DIR_PATH + File.separator + file.nameWithoutExtension() + ".frag");
            FileHandle vert = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.SHADER_DIR_PATH + File.separator + file.nameWithoutExtension() + ".vert");
            if (frag.exists() || vert.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            // check if shaders folder exists
            String shadersPath = projectManager.getCurrentProjectPath() + File.separator + ProjectManager.SHADER_DIR_PATH;
            File destination = new File(projectManager.getCurrentProjectPath()
                    + File.separator + ProjectManager.SHADER_DIR_PATH + File.separator + handle.name());
            try {
                FileUtils.forceMkdir(new File(shadersPath));
                FileUtils.copyFile(handle.file(), destination);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        resourceManager.removeShaderProgram(name);
        FileHandle frag = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SHADER_DIR_PATH + File.separator + name + ".frag");
        FileHandle vert = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SHADER_DIR_PATH + File.separator + name + ".vert");
        frag.delete();
        vert.delete();
        postDeleteShader(root, name);
        deleteAllItems(name);
        return true;
    }

    private void deleteAllItems(String shaderName) {
        for (CompositeItemVO compositeItemVO : projectManager.getCurrentProjectInfoVO().libraryItems.values()) {
            deleteAllShadersOfItem(compositeItemVO, shaderName);
        }

        for (SceneVO scene : projectManager.currentProjectInfoVO.scenes) {
            SceneVO loadedScene = resourceManager.getSceneVO(scene.sceneName);
            CompositeItemVO tmpVo = new CompositeItemVO(loadedScene.composite);
            deleteAllShadersOfItem(tmpVo, shaderName);
            loadedScene.composite = tmpVo;
            SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
            sceneDataManager.saveScene(loadedScene);
        }
    }

    private void deleteAllShadersOfItem(CompositeItemVO compositeItemVO, String shaderName) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> deleteShader(rootItemVo, shaderName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void deleteShader(CompositeItemVO compositeItemVO, String shaderName) {
        if (compositeItemVO != null && compositeItemVO.getAllItems().size != 0) {
            Array<MainItemVO> items = compositeItemVO.getAllItems();

            for (MainItemVO itemVO : items)
                if (itemVO.shader.shaderName.equals(shaderName))
                    itemVO.shader.shaderName = "";
        }
    }

    protected void postDeleteShader(int root, String shaderName) {
        Consumer<Integer> action = (item) -> {
            ShaderComponent shaderComponent = SandboxComponentRetriever.get(item, ShaderComponent.class);
            if (shaderComponent != null && shaderComponent.shaderName.equals(shaderName)) {
                shaderComponent.clear();
            }
        };

        EntityUtils.applyActionRecursivelyOnEntities(root, action);
    }
}
