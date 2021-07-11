package games.rednblack.editor.utils.asset;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.h2d.common.ProgressHandler;

public interface IAsset {
    int matchType(Array<FileHandle> files);
    boolean checkExistence(Array<FileHandle> files);
    void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack);
    boolean deleteAsset(Entity root, String name);
}
