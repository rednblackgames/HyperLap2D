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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.data.manager.PreferencesManager;
import games.rednblack.editor.data.migrations.ProjectVersionMigrator;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.resources.FontSizePair;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.RecursiveFileSuffixFilter;
import games.rednblack.editor.view.menu.HyperLap2DMenuBar;
import games.rednblack.editor.view.ui.dialog.SettingsDialog;
import games.rednblack.editor.view.ui.settings.LivePreviewSettings;
import games.rednblack.editor.view.ui.settings.ProjectExportSettings;
import games.rednblack.h2d.common.H2DDialogs;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.h2d.common.vo.SceneConfigVO;
import games.rednblack.h2d.common.vo.TexturePackerVO;
import games.rednblack.puremvc.Proxy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProjectManager extends Proxy {
    private static final String TAG = ProjectManager.class.getCanonicalName();
    public static final String NAME = TAG;
    private static final String EVENT_PREFIX = "games.rednblack.editor.proxy.ProjectManager";

    public static final String PROJECT_OPENED = EVENT_PREFIX + ".PROJECT_OPENED";
    public static final String PROJECT_DATA_UPDATED = EVENT_PREFIX + ".PROJECT_DATA_UPDATED";

    public static final String IMAGE_DIR_PATH = "assets/orig/images";
    public static final String SPINE_DIR_PATH = "assets/spine-animations";
    public static final String SPRITE_DIR_PATH = "assets/sprite-animations";
    public static final String PARTICLE_DIR_PATH = "assets/particles";
    public static final String TALOS_VFX_DIR_PATH = "assets/talos-vfx";
    public static final String SHADER_DIR_PATH = "assets/shaders";
    public static final String FONTS_DIR_PATH = "assets/freetypefonts";
    public static final String BITMAP_FONTS_DIR_PATH = "assets/bitmapfonts";
    public static final String TINY_VG_DIR_PATH = "assets/tinyvg";

    public ProjectVO currentProjectVO;
    public ProjectInfoVO currentProjectInfoVO;
    private String currentProjectPath;

    public ProjectManager() {
        super(NAME, null);
    }

    private ProjectExportSettings projectExportSettings;
    private LivePreviewSettings livePreviewSettings;

    private Thread fileWatcherThread;

    @Override
    public void onRegister() {
        super.onRegister();

        projectExportSettings = new ProjectExportSettings(facade);
        livePreviewSettings = new LivePreviewSettings(facade);
    }

    @Override
    public void onRemove() {
        super.onRemove();
    }

    public ProjectVO getCurrentProjectVO() {
        return currentProjectVO;
    }

    public ProjectInfoVO getCurrentProjectInfoVO() {
        return currentProjectInfoVO;
    }

    public void createEmptyProject(String projectPath, int width, int height, int pixelPerWorldUnit) throws IOException {
        String projectName = new File(projectPath).getName();
        String projPath = FilenameUtils.normalize(projectPath);

        FileUtils.forceMkdir(new File(projPath));
        FileUtils.forceMkdir(new File(projPath + File.separator + "export"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "scenes"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets/orig"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets/orig/images"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets/orig/pack"));

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        FileHandle whitePixel = new FileHandle(projPath + File.separator + "assets/orig/images" + File.separator + "white-pixel.png");
        PixmapIO.writePNG(whitePixel, pixmap);

        // create project file
        ProjectVO projVo = new ProjectVO();
        projVo.projectName = projectName;
        projVo.projectVersion = ProjectVersionMigrator.dataFormatVersion;

        // create project info file
        ProjectInfoVO projInfoVo = new ProjectInfoVO();
        projInfoVo.originalResolution.name = "orig";
        projInfoVo.originalResolution.width = width;
        projInfoVo.originalResolution.height = height;
        projInfoVo.pixelToWorld = pixelPerWorldUnit;
        TexturePackVO mainPack = new TexturePackVO();
        mainPack.name = "main";
        mainPack.regions.add("white-pixel");
        projInfoVo.imagesPacks.put("main", mainPack);
        TexturePackVO mainAnimPack = new TexturePackVO();
        mainAnimPack.name = "main";
        projInfoVo.animationsPacks.put("main", mainAnimPack);

        //TODO: add project orig resolution setting
        currentProjectVO = projVo;
        currentProjectInfoVO = projInfoVo;
        currentProjectPath = projPath;
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        sceneDataManager.createNewScene("MainScene");
        FileUtils.writeStringToFile(new File(projPath + "/project.h2d"), projVo.constructJsonString(), "utf-8");
        FileUtils.writeStringToFile(new File(projPath + "/project.dt"), projInfoVo.constructJsonString(), "utf-8");
    }

    public void openProjectFromPath(String path) {
        FileHandle projectFile = new FileHandle(path);
        if (!projectFile.exists() || !projectFile.extension().equals("h2d")
                || !projectFile.file().canRead() || !projectFile.file().canWrite())
            return;
        FileHandle projectFolder = projectFile.parent();
        String projectName = projectFolder.name();
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        settingsManager.setLastOpenedPath(projectFolder.parent().path());

        // here we load all data
        openProjectAndLoadAllData(projectFolder.path(), null, () -> {
            PluginUIBridge.get(facade).loadCurrentProject();

            facade.sendNotification(ProjectManager.PROJECT_OPENED);

            //Set title with opened file path
            setWindowTitle(getFormattedTitle(path));
        });
    }

    public void openProjectAndLoadAllData(String projectPath) {
        openProjectAndLoadAllData(projectPath, null, null);
    }

    public void openProjectAndLoadAllData(String projectPath, String resolution) {
        openProjectAndLoadAllData(projectPath, resolution, null);
    }

    /**
     * Loading is asynchronous: {@code onComplete} runs on the render thread once every resource is
     * in memory, and is where anything that depends on them belongs.
     */
    public void openProjectAndLoadAllData(String projectPath, String resolution, Runnable onComplete) {
        String prjFilePath = projectPath + "/project.h2d";
        FileHandle projectFile = Gdx.files.internal(prjFilePath);
        if (!projectFile.exists() || !projectFile.extension().equals("h2d")
                || !projectFile.file().canRead() || !projectFile.file().canWrite()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        PreferencesManager prefs = PreferencesManager.getInstance();
        prefs.buildRecentHistory();
        prefs.pushHistory(prjFilePath);
        facade.sendNotification(HyperLap2DMenuBar.RECENT_LIST_MODIFIED);

        File prjFile = new File(prjFilePath);
        if (!prjFile.isDirectory()) {
            String projectContents = null;
            try {
                projectContents = FileUtils.readFileToString(projectFile.file(), "utf-8");
                Json json = HyperJson.getJson();
                json.setIgnoreUnknownFields(true);
                ProjectVO vo = json.fromJson(ProjectVO.class, projectContents);
                goThroughVersionMigrationProtocol(projectPath, vo);
                currentProjectVO = vo;
                String prjInfoFilePath = projectPath + "/project.dt";
                FileHandle projectInfoFile = Gdx.files.internal(prjInfoFilePath);
                String projectInfoContents = FileUtils.readFileToString(projectInfoFile.file(), "utf-8");
                currentProjectInfoVO = json.fromJson(ProjectInfoVO.class, projectInfoContents);
                syncTenPatchesFromImages(projectPath);
                projectExportSettings.setSettings(vo);
                facade.sendNotification(SettingsDialog.ADD_SETTINGS, projectExportSettings);
                livePreviewSettings.setSettings(vo);
                facade.sendNotification(SettingsDialog.ADD_SETTINGS, livePreviewSettings);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            if (resolution == null) {
                resolutionManager.currentResolutionName = currentProjectVO.lastOpenResolution.isEmpty() ? "orig" : currentProjectVO.lastOpenResolution;
            } else {
                resolutionManager.currentResolutionName = resolution;
                currentProjectVO.lastOpenResolution = resolutionManager.currentResolutionName;
            }
            currentProjectPath = projectPath;
            saveCurrentProject();

            loadProjectData(projectPath, onComplete);

            try {
                addFileWatcher(projectPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (onComplete != null) {
            onComplete.run();
        }
    }

    private void addFileWatcher(String projectPath) {
        stopFileWatcher();

        if (fileWatcherThread != null) return;

        Path directory = Paths.get(projectPath);

        fileWatcherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                    registerAll(directory, watchService);

                    while (true) {
                        WatchKey key;
                        try {
                            // Poll for file system events
                            key = watchService.poll(2, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            return;
                        }

                        if (key == null) {
                            // No events within the timeout period
                            continue;
                        }

                        List<WatchEvent<?>> events = key.pollEvents();

                        for (int i = 0; i < events.size(); i++) {
                            WatchEvent<?> event = events.get(i);
                            WatchEvent.Kind<?> kind = event.kind();
                            Path fileName = (Path) event.context();
                            Path filePath = ((Path) key.watchable()).resolve(fileName);
                            File file = filePath.toFile();

                            if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.PROJECT_FILE_CREATED, file));
                            } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.PROJECT_FILE_DELETED, file));
                            } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.PROJECT_FILE_MODIFIED, file));
                            }
                        }

                        // Reset the key
                        boolean valid = key.reset();
                        if (!valid) {
                            break; // Exit the loop if the key is no longer valid
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, "FileWatcherThread");

        fileWatcherThread.setDaemon(true);
        fileWatcherThread.start();
    }

    public void stopFileWatcher() {
        if (fileWatcherThread == null) return;
        fileWatcherThread.interrupt();
        fileWatcherThread = null;
    }

    private void registerAll(final Path start, final WatchService watchService) throws Exception {
        // Register the directory and its subdirectories recursively
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void goThroughVersionMigrationProtocol(String projectPath, ProjectVO projectVo) {
        ProjectVersionMigrator pvm = new ProjectVersionMigrator(projectPath, projectVo);
        pvm.start();
    }

    public void loadProjectData(String projectPath) {
        loadProjectData(projectPath, null);
    }

    /**
     * Loading is asynchronous: {@code onComplete} runs on the render thread once every resource is
     * in memory, and is where anything that depends on them belongs.
     */
    public void loadProjectData(String projectPath, Runnable onComplete) {
        // All legit loading assets
        ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        File pack = new File(currentProjectPath + "/assets/" + resolutionManager.currentResolutionName + "/pack/pack.atlas");
        if (!pack.exists()) {
            System.err.println("Main Pack not found! Trying to recovery...");
            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        }
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        resourceManager.loadCurrentProjectData(projectPath, resolutionManager.currentResolutionName, onComplete);
    }

    public void saveCurrentProject() {
        try {
            FileUtils.writeStringToFile(new File(currentProjectPath + "/project.h2d"), currentProjectVO.constructJsonString(), "utf-8");
            FileUtils.writeStringToFile(new File(currentProjectPath + "/project.dt"), currentProjectInfoVO.constructJsonString(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCurrentProject(SceneVO vo) {
        saveCurrentProject();
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        sceneDataManager.saveScene(vo);
    }

    public void saveProjectAs() {
        facade.sendNotification(MsgAPI.SHOW_BLACK_OVERLAY);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String selected = TinyFileDialogs.tinyfd_selectFolderDialog("Choose destination directory...", currentProjectPath);
            if (selected != null) {
                FileHandle fileHandle = new FileHandle(selected);
                if (fileHandle.isDirectory() && fileHandle.list().length == 0) {
                    FileHandle source = new FileHandle(currentProjectPath);
                    try {
                        FileUtils.copyDirectory(source.file(), fileHandle.file(), null);
                        Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "Project saved successfully"));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "ERROR: Unable to copy files!"));
                    }
                } else {
                    Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "ERROR: Please choose an empty directory!"));
                }
            }
            Gdx.app.postRunnable(() -> facade.sendNotification(MsgAPI.HIDE_BLACK_OVERLAY));
        });
        executor.shutdown();
    }

    /**
     * Keeps {@link ProjectInfoVO#tenPatches} in sync with the {@code .9.png} files of the original resolution,
     * which are the source of truth for stretch areas: entries are (re)read for new files and for files modified
     * after the last project save, and dropped when their file no longer exists. Tiling and crush mode are not
     * part of the image and are preserved.
     */
    private void syncTenPatchesFromImages(String projectPath) {
        File imagesDir = new File(projectPath + File.separator + IMAGE_DIR_PATH);
        if (!imagesDir.isDirectory()) return;
        File[] files = imagesDir.listFiles((dir, name) -> name.endsWith(".9.png"));
        if (files == null) return;

        if (currentProjectInfoVO.tenPatches == null) currentProjectInfoVO.tenPatches = new HashMap<>();
        long lastSave = new File(projectPath + "/project.dt").lastModified();

        java.util.HashSet<String> existing = new java.util.HashSet<>();
        for (File file : files) {
            String regionName = file.getName().substring(0, file.getName().length() - ".9.png".length());
            existing.add(regionName);
            if (currentProjectInfoVO.tenPatches.containsKey(regionName) && file.lastModified() <= lastSave) continue;
            registerTenPatch(regionName, file);
        }
        currentProjectInfoVO.tenPatches.keySet().retainAll(existing);
    }

    /**
     * Reads the stretch areas of a {@code .9.png} file into {@link ProjectInfoVO#tenPatches}. Everything an
     * existing entry defines beyond the areas (tiling, offsets, crush mode, gradient) is kept.
     */
    public void registerTenPatch(String regionName, File ninePatchFile) {
        TenPatchVO fromFile = games.rednblack.editor.utils.NinePatchUtils.readTenPatchVO(ninePatchFile);
        if (fromFile == null) return;
        if (currentProjectInfoVO.tenPatches == null) currentProjectInfoVO.tenPatches = new HashMap<>();
        TenPatchVO current = currentProjectInfoVO.tenPatches.get(regionName);
        TenPatchVO vo = current == null ? fromFile : new TenPatchVO(current);
        vo.horizontalStretchAreas = fromFile.horizontalStretchAreas;
        vo.verticalStretchAreas = fromFile.verticalStretchAreas;
        currentProjectInfoVO.tenPatches.put(regionName, vo);
    }

    public void copyImageFilesForAllResolutionsIntoProject(Array<FileHandle> files, Boolean performResize, ProgressHandler handler) {
        copyImageFilesIntoProject(files, currentProjectInfoVO.originalResolution, performResize, handler);
        for (FileHandle handle : files) {
            if (handle.name().endsWith(".9.png")) {
                registerTenPatch(handle.nameWithoutExtension().replace(".9", ""), handle.file());
            }
        }
        int totalWarnings = 0;
        for (int i = 0; i < currentProjectInfoVO.resolutions.size; i++) {
            ResolutionEntryVO resolutionEntryVO = currentProjectInfoVO.resolutions.get(i);
            totalWarnings += copyImageFilesIntoProject(files, resolutionEntryVO, performResize, handler);
        }
        if (totalWarnings > 0) {
            H2DDialogs.showOKDialog(PluginUIBridge.get(facade).getUIStage(), "Warning", totalWarnings + " images were not resized for smaller resolutions due to already small size ( < 3px )");
        }
    }

    /**
     * @param files
     * @param resolution
     * @param performResize
     * @return number of images that did needed to be resized but failed
     */
    private int copyImageFilesIntoProject(Array<FileHandle> files, ResolutionEntryVO resolution, Boolean performResize, ProgressHandler handler) {
        float ratio = ResolutionManager.getResolutionRatio(resolution, currentProjectInfoVO.originalResolution);
        String targetPath = currentProjectPath + "/assets/" + resolution.name + "/images";
        float perCopyPercent = 95.0f / files.size;

        int resizeWarningsCount = 0;

        for (FileHandle handle : files) {
            if (!HyperLap2DUtils.PNG_FILTER.accept(null, handle.name())) {
                continue;
            }
            try {
                BufferedImage bufferedImage;
                if (performResize) {
                    bufferedImage = ResolutionManager.imageResize(handle.file(), ratio);
                    if (bufferedImage == null) {
                        System.out.println(handle.file());
                        bufferedImage = ImageIO.read(handle.file());
                        resizeWarningsCount++;
                    }
                } else {
                    bufferedImage = ImageIO.read(handle.file());
                }

                File target = new File(targetPath);
                if (!target.exists()) {
                    File newFile = new File(targetPath);
                    newFile.mkdir();
                }

                ImageIO.write(bufferedImage, "png", new File(targetPath + "/" + handle.name()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.progressChanged(perCopyPercent);
        }

        return resizeWarningsCount;
    }

    public String getFreeTypeFontPath() {
        return currentProjectPath + File.separator + FONTS_DIR_PATH;
    }

    public void exportProject() {
        String defaultBuildPath = currentProjectPath + "/export";
        exportPacks(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportPacks(currentProjectVO.projectMainExportPath);
        }
        exportAnimations(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportAnimations(currentProjectVO.projectMainExportPath);
        }
        exportParticles(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportParticles(currentProjectVO.projectMainExportPath);
        }
        exportTalosVFX(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportTalosVFX(currentProjectVO.projectMainExportPath);
        }
        exportShaders(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportShaders(currentProjectVO.projectMainExportPath);
        }
        prepareFontsForExport();
        exportFonts(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportFonts(currentProjectVO.projectMainExportPath);
        }

        exportBitmapFonts(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportBitmapFonts(currentProjectVO.projectMainExportPath);
        }

        exportTinyVG(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportTinyVG(currentProjectVO.projectMainExportPath);
        }

        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        sceneDataManager.buildScenes(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            sceneDataManager.buildScenes(currentProjectVO.projectMainExportPath);
        }
    }

    private void exportShaders(String targetPath) {
        String srcPath = currentProjectPath + "/assets";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle shadersDirectory = origDirectoryHandle.child("shaders");
        File fileTarget = new File(targetPath + "/" + shadersDirectory.name());
        try {
            FileUtils.copyDirectory(shadersDirectory.file(), fileTarget);
        } catch (IOException ignore) {
        }
    }

    private void exportParticles(String targetPath) {
        String srcPath = currentProjectPath + "/assets";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle particlesDirectory = origDirectoryHandle.child("particles");
        File fileTarget = new File(targetPath + "/" + particlesDirectory.name());
        try {
            FileUtils.copyDirectory(particlesDirectory.file(), fileTarget);
        } catch (IOException ignore) {
        }
    }

    private void exportTalosVFX(String targetPath) {
        String srcPath = currentProjectPath + "/assets";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle particlesDirectory = origDirectoryHandle.child("talos-vfx");
        File fileTarget = new File(targetPath + "/" + "talos-vfx");
        try {
            FileFilter talosSuffixFilter = new RecursiveFileSuffixFilter(".p", ".shdr", ".fga");
            FileUtils.copyDirectory(particlesDirectory.file(), fileTarget, talosSuffixFilter);
        } catch (IOException ignore) {
        }
    }

    private void prepareFontsForExport() {
        FontManager fontManager = facade.retrieveProxy(FontManager.NAME);
        String srcPath = currentProjectPath + "/assets";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle fontsDirectory = origDirectoryHandle.child("freetypefonts");

        for (FileHandle fontFile : fontsDirectory.list()) {
            if (!fontFile.isDirectory())
                fontFile.delete();
        }

        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        ArrayList<FontSizePair> requiredFonts = resourceManager.getProjectRequiredFontsList();
        for (FontSizePair font : requiredFonts) {
            try {
                HashMap<String, String> fonts = fontManager.getFontsMap();
                if (fonts.containsKey(font.fontName)) {
                    FileHandle source = new FileHandle(fonts.get(font.fontName));
                    FileHandle dest = new FileHandle(fontsDirectory.path() + File.separator + font.fontName + ".ttf");
                    FileUtils.copyFile(source.file(), dest.file());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportBitmapFonts(String targetPath) {
        String srcPath = currentProjectPath + "/assets";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle fontsDirectory = origDirectoryHandle.child("bitmapfonts");
        File fileTarget = new File(targetPath + "/" + fontsDirectory.name());
        try {
            FileUtils.copyDirectory(fontsDirectory.file(), fileTarget);
        } catch (IOException ignore) {
        }
    }

    private void exportTinyVG(String targetPath) {
        String srcPath = currentProjectPath + "/assets";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle fontsDirectory = origDirectoryHandle.child("tinyvg");
        File fileTarget = new File(targetPath + "/" + fontsDirectory.name());
        try {
            FileUtils.copyDirectory(fontsDirectory.file(), fileTarget);
        } catch (IOException ignore) {
        }
    }

    private void exportFonts(String targetPath) {
        String srcPath = currentProjectPath + "/assets";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle fontsDirectory = origDirectoryHandle.child("freetypefonts");
        File fileTarget = new File(targetPath + "/" + fontsDirectory.name());
        try {
            FileUtils.copyDirectory(fontsDirectory.file(), fileTarget);
        } catch (IOException ignore) {
        }
    }

    private void exportAnimations(String targetPath) {
        exportSpineAnimationForResolution(targetPath);
    }

    private void exportSpineAnimationForResolution(String targetPath) {
        String spineSrcPath = currentProjectPath + "/assets" + File.separator + "spine-animations";
        try {
            FileUtils.forceMkdir(new File(targetPath + File.separator + "spine-animations"));
            File fileSrc = new File(spineSrcPath);
            String finalTarget = targetPath + File.separator + "spine-animations";

            File fileTargetSpine = new File(finalTarget);

            FileFilter jsonSuffixFilter = new RecursiveFileSuffixFilter(".json");
            FileUtils.copyDirectory(fileSrc, fileTargetSpine, jsonSuffixFilter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportPacks(String targetPath) {
        String srcPath = currentProjectPath + "/assets";
        FileHandle assetDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle[] assetDirectories = assetDirectoryHandle.list();
        for (FileHandle assetDirectory : assetDirectories) {
            if (assetDirectory.isDirectory()) {
                FileHandle assetDirectoryFileHandle = Gdx.files.absolute(assetDirectory.path());
                FileHandle[] packFiles = assetDirectoryFileHandle.child("pack").list();
                for (FileHandle packFile : packFiles) {
                    //Editor-only packs stay in the project but never ship
                    if (isEditorOnlyPackFile(packFile.nameWithoutExtension())) continue;

                    File fileTarget = new File(targetPath + "/" + assetDirectory.name() + "/" + packFile.name());
                    try {
                        FileUtils.copyFile(packFile.file(), fileTarget);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    /**
     * True when this packed file belongs to a pack marked {@link TexturePackVO#editorOnly}.
     *
     * @param baseName pack file name without extension, e.g. {@code foo}, {@code foo2} for its
     *                 second atlas page, or {@code pack} for the "main" pack
     */
    private boolean isEditorOnlyPackFile(String baseName) {
        return matchesEditorOnlyPack(baseName, currentProjectInfoVO.imagesPacks.values())
                || matchesEditorOnlyPack(baseName, currentProjectInfoVO.animationsPacks.values());
    }

    private boolean matchesEditorOnlyPack(String baseName, Collection<TexturePackVO> packs) {
        for (TexturePackVO vo : packs) {
            if (!vo.editorOnly) continue;
            String packName = vo.name.equals("main") ? "pack" : vo.name;
            if (baseName.equals(packName)) return true;
            // Extra atlas pages are the pack name plus a number, so match those too without
            // mistaking a pack whose own name ends in a digit for a page of a shorter one.
            if (baseName.startsWith(packName) && baseName.substring(packName.length()).matches("[0-9]+")) {
                return true;
            }
        }
        return false;
    }

    public void setExportPaths(String path) {
        currentProjectVO.projectMainExportPath = path;
    }

    public void setTexturePackerVO(TexturePackerVO texturePackerVO) {
        TexturePackerVO vo = currentProjectVO.texturePackerVO;
        vo.duplicate = texturePackerVO.duplicate;
        vo.filterMag = texturePackerVO.filterMag;
        vo.filterMin = texturePackerVO.filterMin;
        vo.maxHeight = texturePackerVO.maxHeight;
        vo.maxWidth = texturePackerVO.maxWidth;
        vo.square = texturePackerVO.square;
        vo.legacy = texturePackerVO.legacy;
        vo.fast = texturePackerVO.fast;
    }

    public Settings getTexturePackerSettings() {
        TexturePackerVO vo = currentProjectVO.texturePackerVO;
        Settings settings = new Settings();
        settings.maxHeight = Integer.parseInt(vo.maxHeight);
        settings.maxWidth = Integer.parseInt(vo.maxWidth);
        settings.duplicatePadding = vo.duplicate;
        settings.filterMag = TexturePackerVO.filterMap.get(vo.filterMag);
        settings.filterMin = TexturePackerVO.filterMap.get(vo.filterMin);
        settings.square = vo.square;
        settings.flattenPaths = true;
        settings.legacyOutput = vo.legacy;
        settings.fast = vo.fast;
        settings.limitMemory = false;
        return settings;
    }

    public void createNewProject(String projectPath, int originWidth, int originHeight, int pixelPerWorldUnit) {
        if (projectPath == null || projectPath.equals("")) {
            return;
        }
        String projectName = new File(projectPath).getName();

        if (projectName.equals("")) {
            return;
        }

        try {
            createEmptyProject(projectPath, originWidth, originHeight, pixelPerWorldUnit);
            openProjectAndLoadAllData(projectPath, null, () -> {
                String workSpacePath = projectPath.substring(0, projectPath.lastIndexOf(projectName));
                if (workSpacePath.length() > 0) {
                    SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
                    settingsManager.setLastOpenedPath(workSpacePath);
                }
                PluginUIBridge.get(facade).loadCurrentProject();
                facade.sendNotification(PROJECT_OPENED);

                //Set title with opened file path
                setWindowTitle(getFormattedTitle(projectPath));
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFormattedTitle(String path) {
        //App Name + path to opened file
        return currentProjectVO.projectName + " [ " + getCurrentSceneConfigVO().sceneName + " ] - " + path;
    }

    public void changeSceneWindowTitle() {
        setWindowTitle(getFormattedTitle(currentProjectPath));
    }

    private void setWindowTitle(String title) {
        WindowTitleManager windowTitleManager = facade.retrieveProxy(WindowTitleManager.NAME);
        windowTitleManager.setWindowTitle(title);
    }

    public SceneConfigVO getCurrentSceneConfigVO() {
        if (currentProjectVO == null)
            return null;
        for (int i = 0; i < currentProjectVO.sceneConfigs.size(); i++) {
            if (currentProjectVO.sceneConfigs.get(i).sceneName.equals(PluginUIBridge.get(facade).getCurrentSceneVO().sceneName)) {
                return currentProjectVO.sceneConfigs.get(i);
            }
        }

        SceneConfigVO newConfig = new SceneConfigVO();
        newConfig.sceneName = PluginUIBridge.get(facade).getCurrentSceneVO().sceneName;
        currentProjectVO.sceneConfigs.add(newConfig);

        return newConfig;
    }

    public String getCurrentProjectPath() {
        return currentProjectPath;
    }

    public String getCurrentRawImagesPath() {
        return currentProjectPath + File.separator + "assets" + File.separator + "orig" + File.separator + "images";
    }

    public void deleteRegionFromPack(HashMap<String, TexturePackVO> map, String region) {
        for (TexturePackVO vo : map.values())
            vo.regions.remove(region);
    }

    /**
     * Returns the VO pack name ("main", "foo", ...) that currently holds the given region across both
     * images and animations packs, or {@code null} if none. Used to scope a repack to just the pack
     * affected by a resource deletion.
     */
    public String findPackNameForRegion(String region) {
        for (TexturePackVO vo : currentProjectInfoVO.imagesPacks.values())
            if (vo.regions.contains(region)) return vo.name;
        for (TexturePackVO vo : currentProjectInfoVO.animationsPacks.values())
            if (vo.regions.contains(region)) return vo.name;
        return null;
    }
}