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
import com.badlogic.gdx.utils.ObjectSet;
import com.mortennobel.imagescaling.ResampleOp;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.NinePatchUtils;
import games.rednblack.h2d.common.H2DDialogs;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Proxy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
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
        super(NAME, null);
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
                H2DDialogs.showOKDialog(PluginUIBridge.get(facade).getUIStage(), "Warning", resizeWarnings + " images were not resized for smaller resolutions due to already small size ( < 3px )");
            }
            Facade.getInstance().sendNotification(RESOLUTION_LIST_CHANGED);
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
        rePackProjectImages(resEntry, true);
    }

    /**
     * Packs the images of a single resolution into per-pack atlases.
     *
     * When {@code force} is true (manual repack, settings change, project recovery) every pack is
     * rebuilt and the whole {@code pack/} directory is wiped first — identical to legacy behaviour.
     *
     * When {@code force} is false (asset import, resource deletion) only the packs whose source
     * files changed on disk since their last pack are rebuilt: a pack is dirty if its atlas is
     * missing or any of its source PNGs is newer than its atlas file. This needs no stored baseline,
     * so it works on the very first run after upgrade — importing one image into the main pack never
     * touches a giant atlas. For pack-dialog reorganisation (moving a region, which changes no file
     * mtimes) use {@link #rePackProjectImages(ResolutionEntryVO, ObjectSet)} to name the packs.
     */
    public void rePackProjectImages(ResolutionEntryVO resEntry, boolean force) {
        doRepack(resEntry, force, null);
    }

    /**
     * Repacks the given packs (by their VO name, e.g. {@code "main"}/{@code "foo"}) plus any pack whose
     * source files changed on disk. Used by the pack-dialog when regions move between packs: no
     * source file changes, so the mtime heuristic alone would miss it, so the affected packs must be
     * named explicitly.
     */
    public void rePackProjectImages(ResolutionEntryVO resEntry, ObjectSet<String> forcePacks) {
        doRepack(resEntry, false, forcePacks);
    }

    private void doRepack(ResolutionEntryVO resEntry, boolean force, ObjectSet<String> forcePacks) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        TexturePacker.Settings settings = projectManager.getTexturePackerSettings();

        String sourcePath = projectManager.getCurrentProjectPath() + "/assets/" + resEntry.name + "/images";
        String outputPath = projectManager.getCurrentProjectPath() + "/assets/" + resEntry.name + "/pack";

        FileHandle sourceDir = new FileHandle(sourcePath);
        File outputDir = new File(outputPath);

        try {
            FileUtils.forceMkdir(outputDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // region-name -> on-disk pack name, plus the set of every pack name (images + animations)
        ObjectMap<String, String> regionsReverse = new ObjectMap<>();
        ObjectSet<String> packNames = new ObjectSet<>();
        for (TexturePackVO packVO : projectManager.currentProjectInfoVO.imagesPacks.values()) {
            String name = packVO.name.equals("main") ? "pack" : packVO.name;
            packNames.add(name);
            for (String region : packVO.regions)
                regionsReverse.put(region, name);
        }
        for (TexturePackVO packVO : projectManager.currentProjectInfoVO.animationsPacks.values()) {
            String name = packVO.name.equals("main") ? "pack" : packVO.name;
            packNames.add(name);
            for (String region : packVO.regions)
                regionsReverse.put(region, name);
        }

        // route every source PNG to the pack it belongs to (same routing as the legacy loop)
        ObjectMap<String, Array<FileHandle>> filesByPack = new ObjectMap<>();
        if (sourceDir.exists()) {
            for (FileHandle entry : sourceDir.list()) {
                if (!entry.extension().equals("png")) continue;
                String name = regionsReverse.get(entry.nameWithoutExtension().replace(".9", "").replaceAll("_[0-9]+", ""));
                if (name == null) name = "pack";
                Array<FileHandle> arr = filesByPack.get(name);
                if (arr == null) {
                    arr = new Array<>();
                    filesByPack.put(name, arr);
                }
                arr.add(entry);
            }
        }

        ObjectSet<String> dirty = new ObjectSet<>();
        if (force) {
            for (String name : packNames) dirty.add(name);
            try {
                FileUtils.cleanDirectory(outputDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // explicitly forced packs (VO names -> on-disk names, "main" -> "pack")
            if (forcePacks != null) {
                for (String voName : forcePacks) {
                    dirty.add(voName.equals("main") ? "pack" : voName);
                }
            }
            // mtime-incremental: a pack is dirty if its atlas is missing or any source file is newer than it
            for (String name : packNames) {
                if (dirty.contains(name)) continue;
                Array<FileHandle> files = filesByPack.get(name);
                File atlasFile = new File(outputDir, name + ".atlas");
                if (!atlasFile.exists()) {
                    dirty.add(name);
                    continue;
                }
                long atlasMtime = atlasFile.lastModified();
                if (files != null) {
                    for (FileHandle f : files) {
                        if (f.lastModified() > atlasMtime) {
                            dirty.add(name);
                            break;
                        }
                    }
                }
            }
            // remove atlases of packs that no longer exist (deleted / renamed packs)
            File[] existingAtlases = outputDir.listFiles((dir, n) -> n.endsWith(".atlas"));
            if (existingAtlases != null) {
                for (File atlas : existingAtlases) {
                    String atlasName = atlas.getName();
                    atlasName = atlasName.substring(0, atlasName.length() - ".atlas".length());
                    if (!packNames.contains(atlasName)) deletePackFiles(outputDir, atlasName);
                }
            }
            // delete the previous output (atlas + page PNGs) of every pack that will be rebuilt,
            // mirroring the cleanDirectory used by a full repack but scoped to dirty packs only
            for (String name : dirty) deletePackFiles(outputDir, name);
        }

        // pack only dirty packs; skip empty packs (they produce no atlas, matching legacy behaviour)
        for (String name : dirty) {
            Array<FileHandle> files = filesByPack.get(name);
            if (files == null || files.size == 0) continue;
            TexturePacker tp = new TexturePacker(settings);
            for (FileHandle entry : files) tp.addImage(entry.file());
            tp.pack(outputDir, name);
        }
    }

    /**
     * Removes a pack's previous atlas output — the {@code .atlas} file plus its page PNGs — from the
     * pack directory. This is the per-pack equivalent of the {@code cleanDirectory} used by a full
     * (forced) repack, scoped to just the packs being rebuilt.
     * <p>
     * libGDX names the pages of a pack {@code name} as {@code name.png}, {@code name2.png},
     * {@code name3.png}, ... (the first page has no numeric suffix, subsequent pages are numbered
     * from 2), so page files are matched as {@code name\d*\.png}.
     */
    private void deletePackFiles(File outputDir, String name) {
        String pagePattern = Pattern.quote(name) + "\\d*\\.png";
        File[] pages = outputDir.listFiles((dir, n) -> n.matches(pagePattern));
        if (pages != null) for (File f : pages) f.delete();
        new File(outputDir, name + ".atlas").delete();
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
        rePackProjectImagesForAllResolutions(reloadProjectData, true, null);
    }

    public void rePackProjectImagesForAllResolutions(boolean reloadProjectData, RepackCallback callback) {
        rePackProjectImagesForAllResolutions(reloadProjectData, true, callback);
    }

    /**
     * Async repack across every resolution. {@code force} selects full ({@code true}, legacy
     * behaviour used by the manual menu and settings change) vs incremental ({@code false}, used by
     * asset import and resource deletion).
     */
    public void rePackProjectImagesForAllResolutions(boolean reloadProjectData, boolean force, RepackCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.SHOW_LOADING_DIALOG));
            try {
                rePackProjectImagesForAllResolutionsSync(force);
                Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.HIDE_LOADING_DIALOG));
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
                    PluginUIBridge.get(facade).loadCurrentProject();
                    facade.sendNotification(ProjectManager.PROJECT_DATA_UPDATED);
                });
            }
        });
        executor.shutdown();
    }

    /**
     * Async incremental repack that forces the given packs (by VO name) to be rebuilt across every
     * resolution, in addition to any pack whose source files changed on disk. Used by the pack-dialog
     * when regions move between packs (a move changes no source file mtimes).
     */
    public void rePackProjectImagesForAllResolutions(boolean reloadProjectData, ObjectSet<String> forcePacks, RepackCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.SHOW_LOADING_DIALOG));
            try {
                rePackProjectImagesForAllResolutionsSync(forcePacks);
                Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.HIDE_LOADING_DIALOG));
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
                    PluginUIBridge.get(facade).loadCurrentProject();
                    facade.sendNotification(ProjectManager.PROJECT_DATA_UPDATED);
                });
            }
        });
        executor.shutdown();
    }

    public void rePackProjectImagesForAllResolutionsSync() {
        rePackProjectImagesForAllResolutionsSync(true);
    }

    public void rePackProjectImagesForAllResolutionsSync(boolean force) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        rePackProjectImages(projectManager.getCurrentProjectInfoVO().originalResolution, force);
        for (ResolutionEntryVO resolutionEntryVO : projectManager.getCurrentProjectInfoVO().resolutions) {
            rePackProjectImages(resolutionEntryVO, force);
        }
    }

    public void rePackProjectImagesForAllResolutionsSync(ObjectSet<String> forcePacks) {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        rePackProjectImages(projectManager.getCurrentProjectInfoVO().originalResolution, forcePacks);
        for (ResolutionEntryVO resolutionEntryVO : projectManager.getCurrentProjectInfoVO().resolutions) {
            rePackProjectImages(resolutionEntryVO, forcePacks);
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
        Facade.getInstance().sendNotification(RESOLUTION_LIST_CHANGED);
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
