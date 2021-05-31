package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class ParticleEffectAsset extends Asset {

    @Override
    protected int getType() {
        return ImportUtils.TYPE_PARTICLE_EFFECT;
    }

    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            String contents = FileUtils.readFileToString(file.file(), "utf-8");
            return contents.contains("- Options - ") && contents.contains("- Image Paths -") && contents.contains("- Duration -");
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.PARTICLE_DIR_PATH + File.separator + file.nameWithoutExtension() + ".p");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        final String targetPath = projectManager.getCurrentProjectPath() + "/assets/orig/particles";

        Array<FileHandle> images = new Array<>();
        for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
            if (!fileHandle.isDirectory() && fileHandle.exists()) {
                try {
                    //copy images
                    boolean allImagesFound = addParticleEffectImages(fileHandle, images);
                    if (allImagesFound) {
                        // copy the fileHandle
                        String newName = fileHandle.name();
                        File target = new File(targetPath + "/" + newName);
                        FileUtils.copyFile(fileHandle.file(), target);
                    } else {
                        Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                "\nAll PNG files needs to have same location as the particle file.").padBottom(20).pack();
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error importing particles.");
                    throw e;
                } catch (IOException e) {
                    System.out.println("Error importing particles.");
                    e.printStackTrace();
                }
            }
        }
        if (images.size > 0) {
            projectManager.copyImageFilesForAllResolutionsIntoProject(images, false, progressHandler);

            for (FileHandle handle : new Array.ArrayIterator<>(images)) {
                projectManager.getCurrentProjectVO().imagesPacks.get("main").regions.add(handle.nameWithoutExtension());
            }
        }
        if (!skipRepack) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        }
    }

    private boolean addParticleEffectImages(FileHandle fileHandle, Array<FileHandle> imgs) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileHandle.read()), 64);
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.trim().equals("- Image Paths -")) {
                    line = reader.readLine();
                    while (line != null && !line.equals("")) {
                        if (line.contains("\\") || line.contains("/")) {
                            // then it's a path let's see if exists.
                            File tmp = new File(line);
                            if (tmp.exists()) {
                                imgs.add(new FileHandle(tmp));
                            } else {
                                line = FilenameUtils.getBaseName(line) + ".png";
                                File file = new File(FilenameUtils.getFullPath(fileHandle.path()) + line);
                                if (file.exists()) {
                                    imgs.add(new FileHandle(file));
                                } else {
                                    imgs.clear();
                                    return false;
                                }
                            }
                        } else {
                            File file = new File(FilenameUtils.getFullPath(fileHandle.path()) + line);
                            if (file.exists()) {
                                imgs.add(new FileHandle(file));
                            } else {
                                imgs.clear();
                                return false;
                            }
                        }
                        line = reader.readLine();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
