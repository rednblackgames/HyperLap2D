package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.LabelVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public class BitmapFontAsset extends Asset {
    @Override
    protected boolean matchMimeType(FileHandle file) {
        if (!file.extension().equals("fnt")) return false;

        try {
            new BitmapFont.BitmapFontData(file, false);
            return true;
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public int getType() {
        return AssetsUtils.TYPE_BITMAP_FONT;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.BITMAP_FONTS_DIR_PATH + File.separator + file.nameWithoutExtension() + ".fnt");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        final String targetPath = projectManager.getCurrentProjectPath() + File.separator + ProjectManager.BITMAP_FONTS_DIR_PATH;

        Array<FileHandle> images = new Array<>();
        for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
            if (!fileHandle.isDirectory() && fileHandle.exists()) {
                BitmapFont.BitmapFontData font = new BitmapFont.BitmapFontData(fileHandle, false);
                for (String textureName : font.getImagePaths()) {
                    FileHandle tmp = new FileHandle(textureName);
                    if (!tmp.exists()) {
                        Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                "\nAll PNG files needs to have same location as the font file.").padBottom(20).pack();
                        return;
                    }
                    images.add(tmp);
                }

                String newName = fileHandle.name();
                File target = new File(targetPath + "/" + newName);
                try {
                    FileUtils.copyFile(fileHandle.file(), target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (images.size > 0)
            projectManager.copyImageFilesForAllResolutionsIntoProject(images, false, progressHandler);

        if (!skipRepack) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        }
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        return false;
    }

    @Override
    public boolean exportAsset(MainItemVO item, ExportMapperVO exportMapperVO, File tmpDir) throws IOException {
        super.exportAsset(item, exportMapperVO, tmpDir);
        LabelVO labelVO = (LabelVO) item;
        if (labelVO.bitmapFont == null) return true;

        File fileSrc = new File(currentProjectPath + ProjectManager.BITMAP_FONTS_DIR_PATH + File.separator + labelVO.bitmapFont + ".fnt");
        FileUtils.copyFileToDirectory(fileSrc, tmpDir);
        exportMapperVO.mapper.add(new ExportMapperVO.ExportedAsset(AssetsUtils.TYPE_BITMAP_FONT, fileSrc.getName()));
        BitmapFont.BitmapFontData bitmapFontData = new BitmapFont.BitmapFontData(new FileHandle(fileSrc), false);
        for (String textureName : bitmapFontData.imagePaths) {
            File f = new File(currentProjectPath + ProjectManager.IMAGE_DIR_PATH + File.separator + FilenameUtils.getName(textureName));
            FileUtils.copyFileToDirectory(f, tmpDir);
        }
        return true;
    }
}
