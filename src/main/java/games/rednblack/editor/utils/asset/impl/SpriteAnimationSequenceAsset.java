package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class SpriteAnimationSequenceAsset extends Asset {

    @Override
    public int matchType(Array<FileHandle> files) {
        String[] names = new String[files.size];
        for (int i = 0; i < files.size; i++) {
            names[i] = files.get(i).nameWithoutExtension();
        }
        return ImportUtils.isAnimationSequence(names) ? getType() : ImportUtils.TYPE_UNKNOWN;
    }

    @Override
    protected boolean matchMimeType(FileHandle file) {
        return false;
    }

    @Override
    protected int getType() {
        return ImportUtils.TYPE_ANIMATION_PNG_SEQUENCE;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        String fileName = files.get(0).nameWithoutExtension().replaceAll("_.*", "");
        FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                + ProjectManager.SPRITE_DIR_PATH + File.separator + fileName + File.separator + fileName + ".atlas");
        return fileHandle.exists();
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        String newAnimName;

        String rawFileName = files.get(0).name();
        TexturePacker texturePacker = new TexturePacker(projectManager.getTexturePackerSettings());

        String fileNameWithoutExt = FilenameUtils.removeExtension(rawFileName);
        String fileNameWithoutFrame = fileNameWithoutExt.replaceAll("\\d*$", "").replace("_", "");

        boolean noFileNameWithoutFrame = false;
        if (Objects.equals(fileNameWithoutFrame, "")) {
            fileNameWithoutFrame = files.get(0).parent().name();
            noFileNameWithoutFrame = true;
        }

        String targetPath = projectManager.getCurrentProjectPath() + "/assets/orig/sprite-animations" + File.separator + fileNameWithoutFrame;

        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            File src = file.file();

            String destName;
            if (noFileNameWithoutFrame) {
                destName = targetPath + "Tmp" + File.separator + fileNameWithoutFrame + src.getName().replaceAll("[_](?=.*[_])", "");
            } else {
                destName = targetPath + "Tmp" + File.separator + src.getName().replaceAll("[_](?=.*[_])", "");
            }

            File dest = new File(destName);
            try {
                FileUtils.copyFile(src, dest);
            } catch (IOException e) {
                e.printStackTrace();
                progressHandler.progressFailed();
                return;
            }
        }

        FileHandle pngsDir = new FileHandle(targetPath + "Tmp");
        for (FileHandle entry : pngsDir.list(HyperLap2DUtils.PNG_FILTER)) {
            texturePacker.addImage(entry.file());
        }

        File targetDir = new File(targetPath);
        if (targetDir.exists()) {
            try {
                FileUtils.deleteDirectory(targetDir);
            } catch (IOException e) {
                e.printStackTrace();
                progressHandler.progressFailed();
                return;
            }
        }

        try {
            texturePacker.pack(targetDir, fileNameWithoutFrame);
        } catch (Exception e) {
            progressHandler.progressFailed();
            return;
        }

        //delete newly created directory and images
        try {
            FileUtils.deleteDirectory(pngsDir.file());
        } catch (IOException e) {
            e.printStackTrace();
            progressHandler.progressFailed();
            return;
        }

        newAnimName = fileNameWithoutFrame;

        if (newAnimName != null) {
            resolutionManager.resizeSpriteAnimationForAllResolutions(newAnimName, projectManager.getCurrentProjectInfoVO());
        }
    }
}
