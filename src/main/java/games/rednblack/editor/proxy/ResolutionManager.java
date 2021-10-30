/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.proxy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.mortennobel.imagescaling.ResampleOp;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.NinePatchUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ProgressHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.puremvc.java.patterns.proxy.Proxy;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResolutionManager extends Proxy {
    public interface RepackCallback {
        void onRepack(boolean success);
    }

    private static final String TAG = ResolutionManager.class.getCanonicalName();
    public static final String NAME = TAG;

    public static final String RESOLUTION_LIST_CHANGED = "games.rednblack.editor.proxy.ResolutionManager" + ".RESOLUTION_LIST_CHANGED";

    private static final String EXTENSION_9PATCH = ".9.png";
    public String currentResolutionName;
    private float currentPercent = 0.0f;

    private ProgressHandler handler;

    public ResolutionManager() {
        super(NAME);
    }

    public static BufferedImage imageResize(File file, float ratio) {
        BufferedImage destinationBufferedImage = null;
        try {
            BufferedImage sourceBufferedImage = ImageIO.read(file);
            if (ratio == 1.0) {
                return sourceBufferedImage;
            }
            // When image has to be resized smaller then 3 pixels we should leave it as is, as to ResampleOP limitations
            // But it should also trigger a warning dialog at the and of the import, to notify the user of non resized images.
            if (sourceBufferedImage.getWidth() * ratio < 3 || sourceBufferedImage.getHeight() * ratio < 3) {
                return null;
            }
            int newWidth = Math.max(3, Math.round(sourceBufferedImage.getWidth() * ratio));
            int newHeight = Math.max(3, Math.round(sourceBufferedImage.getHeight() * ratio));
            String name = file.getName();
            Integer[] patches = null;
            if (name.endsWith(EXTENSION_9PATCH)) {
                patches = NinePatchUtils.findPatches(sourceBufferedImage);
                sourceBufferedImage = NinePatchUtils.removePatches(sourceBufferedImage);

                newWidth = Math.round(sourceBufferedImage.getWidth() * ratio);
                newHeight = Math.round(sourceBufferedImage.getHeight() * ratio);
                System.out.println(sourceBufferedImage.getWidth());

                destinationBufferedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = destinationBufferedImage.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(sourceBufferedImage, 0, 0, newWidth, newHeight, null);
                g2.dispose();
            } else {
                // resize with bilinear filter
                ResampleOp resampleOp = new ResampleOp(newWidth, newHeight);
                destinationBufferedImage = resampleOp.filter(sourceBufferedImage, null);
            }

            if (patches != null) {
                destinationBufferedImage = NinePatchUtils.convertTo9Patch(destinationBufferedImage, patches, ratio);
            }

        } catch (IOException ignored) {

        }

        return destinationBufferedImage;
    }

    public static float getResolutionRatio(ResolutionEntryVO resolution, ResolutionEntryVO originalResolution) {
        float a;
        float b;
        switch (resolution.base) {
            default:
            case 0:
                a = resolution.width;
                b = originalResolution.width;
                break;
            case 1:
                a = resolution.height;
                b = originalResolution.height;
                break;
        }
        return a / b;
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    public void createNewResolution(ResolutionEntryVO resolutionEntryVO) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        projectManager.getCurrentProjectInfoVO().resolutions.add(resolutionEntryVO);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // create new folder structure
            String projPath = projectManager.getCurrentProjectPath();
            String sourcePath = projPath + "/" + "assets/orig/images";
            String targetPath = projPath + "/" + "assets/" + resolutionEntryVO.name + "/images";
            createIfNotExist(sourcePath);
            createIfNotExist(projPath + "/" + "assets/" + resolutionEntryVO.name + "/pack");
            copyTexturesFromTo(sourcePath, targetPath);
            int resizeWarnings = resizeTextures(targetPath, resolutionEntryVO);
            rePackProjectImages(resolutionEntryVO);
            changePercentBy(5);
            if (resizeWarnings > 0) {
                Dialogs.showOKDialog(Sandbox.getInstance().getUIStage(), "Warning", resizeWarnings + " images were not resized for smaller resolutions due to already small size ( < 3px )");
            }
            HyperLap2DFacade.getInstance().sendNotification(RESOLUTION_LIST_CHANGED);
        });
        executor.execute(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            projectManager.saveCurrentProject();
//            handler.progressComplete();
        });
        executor.shutdown();
    }


    private void changePercentBy(float value) {
        currentPercent += value;
        //handler.progressChanged(currentPercent);
    }

    public void rePackProjectImages(ResolutionEntryVO resEntry) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        TexturePacker.Settings settings = projectManager.getTexturePackerSettings();

        String sourcePath = projectManager.getCurrentProjectPath() + "/assets/" + resEntry.name + "/images";
        String outputPath = projectManager.getCurrentProjectPath() + "/assets/" + resEntry.name + "/pack";

        FileHandle sourceDir = new FileHandle(sourcePath);
        File outputDir = new File(outputPath);

        try {
            FileUtils.forceMkdir(outputDir);
            FileUtils.cleanDirectory(outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMap<String, TexturePacker> packerMap = new ObjectMap<>();
        ObjectMap<String, String> regionsReverse = new ObjectMap<>();
        for (TexturePackVO packVO : projectManager.currentProjectInfoVO.imagesPacks.values()) {
            String name = packVO.name.equals("main") ? "pack" : packVO.name;
            packerMap.put(name, new TexturePacker(settings));
            for (String region : packVO.regions)
                regionsReverse.put(region, name);
        }
        for (TexturePackVO packVO : projectManager.currentProjectInfoVO.animationsPacks.values()) {
            String name = packVO.name.equals("main") ? "pack" : packVO.name;
            if (packerMap.get(name) == null)
                packerMap.put(name, new TexturePacker(settings));
            for (String region : packVO.regions)
                regionsReverse.put(region, name);
        }


        for (FileHandle entry : sourceDir.list()) {
            if (entry.extension().equals("png")) {
                String name = regionsReverse.get(entry.nameWithoutExtension().replace(".9", "").replaceAll("_[0-9]+", ""));
                name = name == null ? "pack" : name;
                TexturePacker tp = packerMap.get(name);
                tp.addImage(entry.file());
            }
        }

        for (String name : packerMap.keys()) {
            TexturePacker tp = packerMap.get(name);
            tp.pack(outputDir, name);
        }
    }

    private int resizeTextures(String path, ResolutionEntryVO resolution) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        float ratio = getResolutionRatio(resolution, projectManager.getCurrentProjectInfoVO().originalResolution);
        FileHandle targetDir = new FileHandle(path);
        FileHandle[] entries = targetDir.list(HyperLap2DUtils.PNG_FILTER);
        float perResizePercent = 95.0f / entries.length;

        int resizeWarnings = 0;

        for (FileHandle entry : entries) {
            try {
                File file = entry.file();
                File destinationFile = new File(path + "/" + file.getName());
                BufferedImage resizedImage = ResolutionManager.imageResize(file, ratio);
                if (resizedImage == null) {
                    resizeWarnings++;
                    ImageIO.write(ImageIO.read(file), "png", destinationFile);
                } else {
                    ImageIO.write(ResolutionManager.imageResize(file, ratio), "png", destinationFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            changePercentBy(perResizePercent);
        }

        return resizeWarnings;
    }

    private void copyTexturesFromTo(String fromPath, String toPath) {
        FileHandle sourceDir = new FileHandle(fromPath);
        FileHandle[] entries = sourceDir.list(HyperLap2DUtils.PNG_FILTER);
        float perCopyPercent = 10.0f / entries.length;
        for (FileHandle entry : entries) {
            File file = entry.file();
            String filename = file.getName();
            File target = new File(toPath + "/" + filename);
            try {
                FileUtils.copyFile(file, target);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        changePercentBy(perCopyPercent);
    }

    private File createIfNotExist(String dirPath) {
        File theDir = new File(dirPath);
        boolean result = false;
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            result = theDir.mkdir();
        }

        if (result)
            return theDir;
        else return null;
    }

    public void resizeImagesTmpDirToResolution(String packName, File sourceFolder, ResolutionEntryVO resolution, File targetFolder) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        float ratio = ResolutionManager.getResolutionRatio(resolution, projectManager.getCurrentProjectInfoVO().originalResolution);

        if (targetFolder.exists()) {
            try {
                FileUtils.cleanDirectory(targetFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // now pack
        TexturePacker.Settings settings = projectManager.getTexturePackerSettings();

        TexturePacker tp = new TexturePacker(settings);
        for (final File fileEntry : sourceFolder.listFiles()) {
            if (!fileEntry.isDirectory()) {
                BufferedImage bufferedImage = ResolutionManager.imageResize(fileEntry, ratio);
                tp.addImage(bufferedImage, FilenameUtils.removeExtension(fileEntry.getName()));
            }
        }

        tp.pack(targetFolder, packName);
    }

    public float getCurrentMul() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        ResolutionEntryVO curRes = projectManager.getCurrentProjectInfoVO().getResolution(currentResolutionName);
        float mul = 1f;
        if (!currentResolutionName.equals("orig")) {
            if (curRes.base == 0) {
                mul = (float) curRes.width / (float) projectManager.getCurrentProjectInfoVO().originalResolution.width;
            } else {
                mul = (float) curRes.height / (float) projectManager.getCurrentProjectInfoVO().originalResolution.height;
            }
        }

        return mul;
    }

    public void rePackProjectImagesForAllResolutions(boolean reloadProjectData) {
        rePackProjectImagesForAllResolutions(reloadProjectData, null);
    }

    public void rePackProjectImagesForAllResolutions(boolean reloadProjectData, RepackCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            facade.sendNotification(MsgAPI.SHOW_LOADING_DIALOG);
            try {
                rePackProjectImagesForAllResolutionsSync();
                facade.sendNotification(MsgAPI.HIDE_LOADING_DIALOG);
                if (callback != null)
                    callback.onRepack(true);
            } catch (Exception e) {
                if (callback != null)
                    callback.onRepack(false);
            }

            if (reloadProjectData) {
                Gdx.app.postRunnable(() -> {
                    ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                    ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
                    resourceManager.loadCurrentProjectData(projectManager.getCurrentProjectPath(), currentResolutionName);
                    facade.sendNotification(ProjectManager.PROJECT_DATA_UPDATED);
                });
            }
        });
        executor.shutdown();
    }

    public void rePackProjectImagesForAllResolutionsSync() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        rePackProjectImages(projectManager.getCurrentProjectInfoVO().originalResolution);
        for (ResolutionEntryVO resolutionEntryVO : projectManager.getCurrentProjectInfoVO().resolutions) {
            rePackProjectImages(resolutionEntryVO);
        }
    }

    public void deleteResolution(ResolutionEntryVO resolutionEntryVO) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        try {
            FileUtils.deleteDirectory(new File(projectManager.getCurrentProjectPath() + "/assets/" + resolutionEntryVO.name));
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }

        currentResolutionName = getOriginalResolution().name;

        ProjectInfoVO projectInfo = projectManager.getCurrentProjectInfoVO();
        projectInfo.resolutions.removeValue(resolutionEntryVO, false);
        HyperLap2DFacade.getInstance().sendNotification(RESOLUTION_LIST_CHANGED);
        projectManager.saveCurrentProject();
        projectManager.openProjectAndLoadAllData(projectManager.getCurrentProjectPath(), "orig");
    }

    public Array<ResolutionEntryVO> getResolutions() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectInfoVO().resolutions;
    }

    public ResolutionEntryVO getOriginalResolution() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.getCurrentProjectInfoVO().originalResolution;
    }

    public ResolutionEntryVO getCurrentResolution() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        for (ResolutionEntryVO res : projectManager.getCurrentProjectInfoVO().resolutions) {
            if (res.name.equals(currentResolutionName)) {
                return res;
            }
        }
        return getOriginalResolution();
    }
}
