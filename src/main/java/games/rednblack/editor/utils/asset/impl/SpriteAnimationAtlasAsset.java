package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class SpriteAnimationAtlasAsset extends Asset {

    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(file, file.parent(), false);
            return ImportUtils.isAtlasAnimationSequence(atlas.getRegions());
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    protected int getType() {
        return ImportUtils.TYPE_SPRITE_ANIMATION_ATLAS;
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
                String fileNameWithoutExt = ImportUtils.getAtlasName(fileHandle);

                String targetPath = projectManager.getCurrentProjectPath() + "/assets/orig/sprite-animations" + File.separator + fileNameWithoutExt;
                File targetDir = new File(targetPath);
                if (targetDir.exists()) {
                    FileUtils.deleteDirectory(targetDir);
                }

                FileHandle tmpDir = new FileHandle(projectManager.getCurrentProjectPath() + File.separator + "tmp");
                if (tmpDir.exists())
                    FileUtils.forceDelete(tmpDir.file());
                FileUtils.forceMkdir(tmpDir.file());
                ImportUtils.unpackAtlasIntoTmpFolder(fileHandle.file(), null, tmpDir.path());
                Array<FileHandle> images = new Array<>(tmpDir.list());
                projectManager.copyImageFilesForAllResolutionsIntoProject(images, true, progressHandler);
                FileUtils.forceDelete(tmpDir.file());

                File atlasTargetPath = new File(targetPath + File.separator + fileNameWithoutExt + ".atlas");
                FileUtils.copyFile(fileHandle.file(), atlasTargetPath);
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
}
