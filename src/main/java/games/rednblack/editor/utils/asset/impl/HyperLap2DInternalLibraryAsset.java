package games.rednblack.editor.utils.asset.impl;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.Image9patchVO;
import games.rednblack.editor.renderer.data.LabelVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;

public class HyperLap2DInternalLibraryAsset extends Asset {
    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equalsIgnoreCase("lib");
    }

    @Override
    public int getType() {
        return ImportUtils.TYPE_HYPERLAP2D_INTERNAL_LIBRARY;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle handle : new Array.ArrayIterator<>(files)) {
            Json json = new Json();
            String projectInfoContents = null;
            try {
                projectInfoContents = FileUtils.readFileToString(handle.file(), "utf-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            CompositeItemVO voInfo = json.fromJson(CompositeItemVO.class, projectInfoContents);
            adjustPPWCoordinates(voInfo);

            String fileNameAndExtension = handle.name();
            String fileName = FilenameUtils.removeExtension(fileNameAndExtension);
            projectManager.getCurrentProjectInfoVO().libraryItems.put(fileName, voInfo);
            projectManager.saveCurrentProject();
        }
    }

    @Override
    public boolean deleteAsset(Entity root, String name) {
        return false;
    }

    private void adjustPPWCoordinates(CompositeItemVO compositeItemVO) {
        int ppwu = projectManager.getCurrentProjectInfoVO().pixelToWorld;
        for (MainItemVO item : compositeItemVO.composite.getAllItems()) {
            item.originX = item.originX / ppwu;
            item.originY = item.originY / ppwu;
            item.x = item.x / ppwu;
            item.y = item.y / ppwu;

            if (item instanceof CompositeItemVO) {
                ((CompositeItemVO) item).width = ((CompositeItemVO) item).width / ppwu;
                ((CompositeItemVO) item).height = ((CompositeItemVO) item).height / ppwu;
            }

            if (item instanceof Image9patchVO) {
                ((Image9patchVO) item).width = ((Image9patchVO) item).width / ppwu;
                ((Image9patchVO) item).height = ((Image9patchVO) item).height / ppwu;
            }

            if (item instanceof LabelVO) {
                ((LabelVO) item).width = ((LabelVO) item).width / ppwu;
                ((LabelVO) item).height = ((LabelVO) item).height / ppwu;
            }
        }
    }
}
