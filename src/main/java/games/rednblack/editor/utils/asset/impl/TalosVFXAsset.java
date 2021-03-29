package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.TalosResources;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public class TalosVFXAsset extends Asset {

    @Override
    protected boolean matchMimeType(FileHandle file) {
        try {
            String contents = FileUtils.readFileToString(file.file(), "utf-8");
            return file.extension().equalsIgnoreCase("p") && contents.contains("emitters");
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    protected int getType() {
        return ImportUtils.TYPE_TALOS_VFX;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            FileHandle fileHandle = new FileHandle(projectManager.getCurrentProjectPath() + File.separator
                    + ProjectManager.TALOS_VFX_DIR_PATH + File.separator + file.nameWithoutExtension() + ".p");
            if (fileHandle.exists())
                return true;
        }
        return false;
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), TalosResources.Module.class);
        }

        final String targetPath = projectManager.getCurrentProjectPath() + "/assets/orig/talos-vfx";
        Array<FileHandle> images = new Array<>();
        Array<FileHandle> assetsRes = new Array<>();
        for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
            if (!fileHandle.isDirectory() && fileHandle.exists()) {
                try {
                    TalosResources talosResources = json.fromJson(TalosResources.class, fileHandle);
                    //copy images
                    boolean allImagesFound = addTalosImages(talosResources, fileHandle, images);
                    if (allImagesFound) {
                        boolean allAssetFound = addTalosRes(talosResources, fileHandle, assetsRes);
                        if (allAssetFound) {
                            // copy the fileHandle
                            String newName = fileHandle.name();
                            File target = new File(targetPath + "/" + newName);
                            FileUtils.copyFile(fileHandle.file(), target);
                        }
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
        }
        if (assetsRes.size > 0) {
            for (FileHandle fileHandle : assetsRes) {
                try {
                    String newName = fileHandle.name();
                    File target = new File(targetPath + "/" + newName);
                    FileUtils.copyFile(fileHandle.file(), target);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!skipRepack) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        }
    }

    private boolean addTalosImages(TalosResources talosResources, FileHandle fileHandle, Array<FileHandle> imgs) {
        try {
            Array<String> resources = talosResources.metadata.resources;
            for (String res : resources) {
                res += ".png";
                File file = new File(FilenameUtils.getFullPath(fileHandle.path()) + res);
                if (file.exists()) {
                    imgs.add(new FileHandle(file));
                } else {
                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                            "\nCould not find " + file.getName() + ".\nCheck if the file exists in the same directory.").padBottom(20).pack();
                    imgs.clear();
                    return false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean addTalosRes(TalosResources talosResources, FileHandle fileHandle, Array<FileHandle> imgs) {
        try {
            for (TalosResources.Emitter emitter : talosResources.emitters) {
                for (TalosResources.Module module : emitter.modules) {
                    if (module.get("shdrAssetName") != null) {
                        String assetName = module.get("shdrAssetName").toString();
                        File file = new File(FilenameUtils.getFullPath(fileHandle.path()) + assetName);
                        if (file.exists()) {
                            imgs.add(new FileHandle(file));
                        } else {
                            Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                    "\nCould not find " + file.getName() + ".\nCheck if the file exists in the same directory.").padBottom(20).pack();
                            imgs.clear();
                            return false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
}
