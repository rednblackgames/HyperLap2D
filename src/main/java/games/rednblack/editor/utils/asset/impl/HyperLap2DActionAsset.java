package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;

import java.util.HashMap;

public class HyperLap2DActionAsset extends Asset {
    private final Json json = HyperJson.getJson();

    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equalsIgnoreCase("h2daction");
    }

    @Override
    public int getType() {
        return AssetsUtils.TYPE_HYPERLAP2D_ACTION;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        boolean exists = false;
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            if (projectManager.getCurrentProjectInfoVO().libraryActions.get(file.nameWithoutExtension()) != null) {
                exists = true;
            }
        }
        return exists;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
            GraphVO action = json.fromJson(GraphVO.class, fileHandle);
            projectManager.getCurrentProjectInfoVO().libraryActions.put(fileHandle.nameWithoutExtension(), action);
            projectManager.saveCurrentProject();
        }
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        HashMap<String, GraphVO> libraryActions = projectManager.currentProjectInfoVO.libraryActions;

        libraryActions.remove(name);

        return true;
    }
}
