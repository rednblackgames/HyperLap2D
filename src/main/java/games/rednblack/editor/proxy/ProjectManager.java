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
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.data.manager.PreferencesManager;
import games.rednblack.editor.data.migrations.ProjectVersionMigrator;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.resources.FontSizePair;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.RecursiveFileSuffixFilter;
import games.rednblack.editor.view.menu.HyperLap2DMenuBar;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.dialog.SettingsDialog;
import games.rednblack.editor.view.ui.settings.LivePreviewSettings;
import games.rednblack.editor.view.ui.settings.ProjectExportSettings;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ProjectVO;
import games.rednblack.h2d.common.vo.SceneConfigVO;
import games.rednblack.h2d.common.vo.TexturePackerVO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.puremvc.java.patterns.proxy.Proxy;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        super(NAME);
    }

    private ProjectExportSettings projectExportSettings;
    private LivePreviewSettings livePreviewSettings;

    private FileAlterationMonitor fileWatcherMonitor;

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();

        projectExportSettings = new ProjectExportSettings();
        livePreviewSettings = new LivePreviewSettings();
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
        openProjectAndLoadAllData(projectFolder.path());
        Sandbox.getInstance().loadCurrentProject();

        facade.sendNotification(ProjectManager.PROJECT_OPENED);

        //Set title with opened file path
        setWindowTitle(getFormattedTitle(path));
    }

    public void openProjectAndLoadAllData(String projectPath) {
        openProjectAndLoadAllData(projectPath, null);
    }

    public void openProjectAndLoadAllData(String projectPath, String resolution) {
        String prjFilePath = projectPath + "/project.h2d";
        FileHandle projectFile = Gdx.files.internal(prjFilePath);
        if (!projectFile.exists() || !projectFile.extension().equals("h2d")
                || !projectFile.file().canRead() || !projectFile.file().canWrite())
            return;

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

            checkForConsistency(projectPath);
            loadProjectData(projectPath);

            try {
                addFileWatcher(projectPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addFileWatcher(String projectPath) throws Exception {
        stopFileWatcher();

        fileWatcherMonitor = new FileAlterationMonitor(2000);

        FileAlterationObserver observer = new FileAlterationObserver(projectPath);
        FileAlterationListener listener = new FileAlterationListenerAdaptor() {
            @Override
            public void onFileCreate(File file) {
                facade.sendNotification(MsgAPI.PROJECT_FILE_CREATED, file);
            }

            @Override
            public void onFileDelete(File file) {
                facade.sendNotification(MsgAPI.PROJECT_FILE_DELETED, file);
            }

            @Override
            public void onFileChange(File file) {
                facade.sendNotification(MsgAPI.PROJECT_FILE_MODIFIED, file);
            }
        };
        observer.addListener(listener);

        fileWatcherMonitor.addObserver(observer);
        fileWatcherMonitor.start();
    }

    public void stopFileWatcher() {
        if (fileWatcherMonitor != null) {
            try {
                fileWatcherMonitor.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            fileWatcherMonitor = null;
        }
    }

    private void goThroughVersionMigrationProtocol(String projectPath, ProjectVO projectVo) {
        ProjectVersionMigrator pvm = new ProjectVersionMigrator(projectPath, projectVo);
        pvm.start();
    }

    private void checkForConsistency(String projectPath) {
        // check if current project requires cleanup

        FileHandle sourceDir = new FileHandle(projectPath + "/scenes/");
        for (FileHandle entry : sourceDir.list(HyperLap2DUtils.DT_FILTER)) {
            if (!entry.file().isDirectory()) {
                Json json = HyperJson.getJson();
                json.setIgnoreUnknownFields(true);
                SceneVO sceneVO = json.fromJson(SceneVO.class, entry);
                if (sceneVO.composite == null) continue;
                Array<MainItemVO> items = sceneVO.composite.getAllItems();

                for (CompositeItemVO libraryItem : currentProjectInfoVO.libraryItems.values()) {
                    if (libraryItem == null) continue;
                    items = libraryItem.getAllItems();
                }
            }
        }
    }

    public void loadProjectData(String projectPath) {
        // All legit loading assets
        ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        File pack = new File(currentProjectPath + "/assets/" + resolutionManager.currentResolutionName + "/pack/pack.atlas");
        if (!pack.exists()) {
            System.err.println("Main Pack not found! Trying to recovery...");
            resolutionManager.rePackProjectImagesForAllResolutionsSync();
        }
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        resourceManager.loadCurrentProjectData(projectPath, resolutionManager.currentResolutionName);
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

    public void copyImageFilesForAllResolutionsIntoProject(Array<FileHandle> files, Boolean performResize, ProgressHandler handler) {
        copyImageFilesIntoProject(files, currentProjectInfoVO.originalResolution, performResize, handler);
        int totalWarnings = 0;
        for (int i = 0; i < currentProjectInfoVO.resolutions.size; i++) {
            ResolutionEntryVO resolutionEntryVO = currentProjectInfoVO.resolutions.get(i);
            totalWarnings += copyImageFilesIntoProject(files, resolutionEntryVO, performResize, handler);
        }
        if (totalWarnings > 0) {
            Dialogs.showOKDialog(Sandbox.getInstance().getUIStage(), "Warning", totalWarnings + " images were not resized for smaller resolutions due to already small size ( < 3px )");
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
            openProjectAndLoadAllData(projectPath);
            String workSpacePath = projectPath.substring(0, projectPath.lastIndexOf(projectName));
            if (workSpacePath.length() > 0) {
                SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
                settingsManager.setLastOpenedPath(workSpacePath);
            }
            Sandbox.getInstance().loadCurrentProject();
            facade.sendNotification(PROJECT_OPENED);

            //Set title with opened file path
            setWindowTitle(getFormattedTitle(projectPath));

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
            if (currentProjectVO.sceneConfigs.get(i).sceneName.equals(Sandbox.getInstance().getSceneControl().getCurrentSceneVO().sceneName)) {
                return currentProjectVO.sceneConfigs.get(i);
            }
        }

        SceneConfigVO newConfig = new SceneConfigVO();
        newConfig.sceneName = Sandbox.getInstance().getSceneControl().getCurrentSceneVO().sceneName;
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
}