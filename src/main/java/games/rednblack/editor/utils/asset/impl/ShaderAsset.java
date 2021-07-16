package games.rednblack.editor.utils.asset.impl;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ShaderAsset extends Asset {

    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equalsIgnoreCase("frag") || file.extension().equalsIgnoreCase("vert");
    }

    @Override
    public int getType() {
        return ImportUtils.TYPE_SHADER;
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
            String shadersPath = projectManager.getCurrentProjectPath() + "/assets/shaders";
            File destination = new File(projectManager.getCurrentProjectPath() + "/assets/shaders/" + handle.name());
            try {
                FileUtils.forceMkdir(new File(shadersPath));
                FileUtils.copyFile(handle.file(), destination);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean deleteAsset(Entity root, String name) {
        return false;
    }
}
