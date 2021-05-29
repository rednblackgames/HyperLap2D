package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;

import java.io.File;

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
    protected int getType() {
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
            projectManager.getCurrentProjectVO().imagesPacks.get("main").regions.add(handle.nameWithoutExtension());
        }
    }
}
