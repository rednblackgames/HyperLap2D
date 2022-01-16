package games.rednblack.editor.utils.asset;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;

import java.io.File;
import java.io.IOException;

public interface IAsset {
    int matchType(Array<FileHandle> files);
    boolean checkExistence(Array<FileHandle> files);
    void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack);
    boolean deleteAsset(int root, String name);
    boolean exportAsset(MainItemVO item, ExportMapperVO exportMapperVO, File tmpDir) throws IOException;
}
