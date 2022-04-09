package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import games.rednblack.h2d.extension.tinyvg.TinyVGVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

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
        return false;
    }

    @Override
    public boolean exportAsset(MainItemVO item, ExportMapperVO exportMapperVO, File tmpDir) throws IOException {
        super.exportAsset(item, exportMapperVO, tmpDir);

        TinyVGVO tinyVGVO = (TinyVGVO) item;

        return true;
    }
}
