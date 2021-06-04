package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class AtlasAsset extends Asset {

    @Override
    protected int getType() {
        return ImportUtils.TYPE_TEXTURE_ATLAS;
    }

    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            TextureAtlas.TextureAtlasData atlas = new TextureAtlas.TextureAtlasData(file, file.parent(), false);
            return !ImportUtils.isAtlasAnimationSequence(atlas.getRegions());
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        //TODO
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        try {
            for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
                FileHandle tmpDir = new FileHandle(projectManager.getCurrentProjectPath() + File.separator + "tmp");
                if (tmpDir.exists())
                    FileUtils.forceDelete(tmpDir.file());
                FileUtils.forceMkdir(tmpDir.file());
                ImportUtils.unpackAtlasIntoTmpFolder(fileHandle.file(), null, tmpDir.path());
                Array<FileHandle> images = new Array<>(tmpDir.list());
                projectManager.copyImageFilesForAllResolutionsIntoProject(images, true, progressHandler);
                FileUtils.forceDelete(tmpDir.file());

                TexturePackVO texturePackVO = projectManager.getCurrentProjectInfoVO().imagesPacks.get(fileHandle.nameWithoutExtension());
                if (texturePackVO == null) {
                    texturePackVO = new TexturePackVO();
                    texturePackVO.name = fileHandle.nameWithoutExtension();

                    projectManager.getCurrentProjectInfoVO().imagesPacks.put(texturePackVO.name, texturePackVO);
                }

                for (FileHandle image : images) {
                    texturePackVO.regions.add(image.nameWithoutExtension().replace(".9", ""));
                }
            }

            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        } catch (IOException e) {
            e.printStackTrace();
            progressHandler.progressFailed();
        }
    }
}
