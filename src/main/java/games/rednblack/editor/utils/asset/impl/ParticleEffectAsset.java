package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.ParticleEffectVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ParticleEffectAsset extends Asset {

    @Override
    public int getType() {
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
                projectManager.getCurrentProjectInfoVO().imagesPacks.get("main").regions.add(handle.nameWithoutExtension());
            }
        }
        if (!skipRepack) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        }
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        String particlePath = projectManager.getCurrentProjectPath() + File.separator + ProjectManager.PARTICLE_DIR_PATH + File.separator;
        String filePath = particlePath + name;

        if ((new File(filePath)).delete()) {
            deleteEntitiesWithParticleEffects(root, name); // delete entities from scene
            deleteAllItemsWithParticleName(name);
            return true;
        }
        return false;
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

    private void deleteAllItemsWithParticleName(String name) {
        for (CompositeItemVO compositeItemVO : projectManager.getCurrentProjectInfoVO().libraryItems.values()) {
            deleteAllParticles(compositeItemVO, name);
        }

        for (SceneVO scene : projectManager.currentProjectInfoVO.scenes) {
            CompositeItemVO tmpVo = new CompositeItemVO();
            SceneVO loadedScene = resourceManager.getSceneVO(scene.sceneName);
            tmpVo.composite = loadedScene.composite;
            deleteAllParticles(tmpVo, name);
            loadedScene.composite = tmpVo.composite;
            SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
            sceneDataManager.saveScene(loadedScene);
        }
    }

    private void deleteAllParticles(CompositeItemVO compositeItemVO, String name) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> getParticles(rootItemVo, name);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void getParticles(CompositeItemVO compositeItemVO, String name) {
        tmpImageList.clear();
        if (compositeItemVO.composite != null && compositeItemVO.composite.sParticleEffects.size() != 0) {
            ArrayList<ParticleEffectVO> particleEffectList = compositeItemVO.composite.sParticleEffects;
            for (ParticleEffectVO particleEffectVO : particleEffectList) {
                if (particleEffectVO.particleName.equals(name)) {
                    tmpImageList.add(particleEffectVO);
                }
            }
            particleEffectList.removeAll(tmpImageList);
        }
    }

    private void deleteEntitiesWithParticleEffects(int rootEntity, String particleName) {
        tmpEntityList.clear();
        Consumer<Integer> action = (root) -> {
            ParticleComponent particleComponent = SandboxComponentRetriever.get(root, ParticleComponent.class);
            if (particleComponent != null && particleComponent.particleName.equals(particleName)) {
                tmpEntityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(tmpEntityList);
    }
}
