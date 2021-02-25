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
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectSet;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.talosvfx.talos.runtime.ParticleEmitterDescriptor;
import com.talosvfx.talos.runtime.modules.AbstractModule;
import com.talosvfx.talos.runtime.modules.ShadedSpriteModule;
import com.talosvfx.talos.runtime.serialization.ExportData;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.data.manager.PreferencesManager;
import games.rednblack.editor.data.migrations.ProjectVersionMigrator;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.utils.MySkin;
import games.rednblack.editor.renderer.utils.Version;
import games.rednblack.editor.utils.AssetImporter;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.utils.ZipUtils;
import games.rednblack.editor.utils.runtime.TalosResources;
import games.rednblack.editor.view.menu.HyperLap2DMenuBar;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.dialog.SettingsDialog;
import games.rednblack.editor.view.ui.settings.LivePreviewSettings;
import games.rednblack.editor.view.ui.settings.ProjectExportSettings;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.*;
import games.rednblack.h2d.extention.spine.SpineItemType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.puremvc.java.patterns.proxy.Proxy;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectManager extends Proxy {
    private static final String TAG = ProjectManager.class.getCanonicalName();
    public static final String NAME = TAG;
    private static final String EVENT_PREFIX = "games.rednblack.editor.proxy.ProjectManager";

    public static final String PROJECT_OPENED = EVENT_PREFIX + ".PROJECT_OPENED";
    public static final String PROJECT_DATA_UPDATED = EVENT_PREFIX + ".PROJECT_DATA_UPDATED";

    public static final String IMAGE_DIR_PATH = "assets/orig/images";
    public static final String SPINE_DIR_PATH = "assets/orig/spine-animations";
    public static final String SPRITE_DIR_PATH = "assets/orig/sprite-animations";
    public static final String PARTICLE_DIR_PATH = "assets/orig/particles";
    public static final String TALOS_VFX_DIR_PATH = "assets/orig/talos-vfx";
    public static final String SHADER_DIR_PATH = "assets/shaders";

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

        /*
        if (workspacePath.endsWith(File.separator)) {
            workspacePath = workspacePath.substring(0, workspacePath.length() - 1);
        }

        String projPath = workspacePath + File.separator + projectName;
        */
        String projectName = new File(projectPath).getName();
        String projPath = FilenameUtils.normalize(projectPath);

        FileUtils.forceMkdir(new File(projPath));
        FileUtils.forceMkdir(new File(projPath + File.separator + "export"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "scenes"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets/orig"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets/orig/images"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets/orig/particles"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets/orig/animations"));
        FileUtils.forceMkdir(new File(projPath + File.separator + "assets/orig/pack"));


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

        //TODO: add project orig resolution setting
        currentProjectVO = projVo;
        currentProjectInfoVO = projInfoVo;
        currentProjectPath = projPath;
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        sceneDataManager.createNewScene("MainScene");
        FileUtils.writeStringToFile(new File(projPath + "/project.h2d"), projVo.constructJsonString(), "utf-8");
        FileUtils.writeStringToFile(new File(projPath + "/project.dt"), projInfoVo.constructJsonString(), "utf-8");
    }

    public void openProjectAndLoadAllData(String projectPath) {
        openProjectAndLoadAllData(projectPath, null);
    }

    public void openProjectAndLoadAllData(String projectPath, String resolution) {
        String prjFilePath = projectPath + "/project.h2d";

        PreferencesManager prefs = PreferencesManager.getInstance();
        prefs.buildRecentHistory();
        prefs.pushHistory(prjFilePath);
        facade.sendNotification(HyperLap2DMenuBar.RECENT_LIST_MODIFIED);

        File prjFile = new File(prjFilePath);
        if (!prjFile.isDirectory()) {
            if (!prjFile.exists()) {

                ProjectVO projVoEmpty = new ProjectVO();
                projVoEmpty.projectName = prjFile.getName();
                projVoEmpty.projectVersion = ProjectVersionMigrator.dataFormatVersion;

                try {
                    FileUtils.writeStringToFile(prjFile, projVoEmpty.constructJsonString(), "utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            FileHandle projectFile = Gdx.files.internal(prjFilePath);
            String projectContents = null;
            try {
                projectContents = FileUtils.readFileToString(projectFile.file(), "utf-8");
                Json json = new Json();
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
                saveCurrentProject();

            }
            currentProjectPath = projectPath;
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
                Json json = new Json();
                json.setIgnoreUnknownFields(true);
                SceneVO sceneVO = json.fromJson(SceneVO.class, entry);
                if (sceneVO.composite == null) continue;
                ArrayList<MainItemVO> items = sceneVO.composite.getAllItems();

                for (CompositeItemVO libraryItem : currentProjectInfoVO.libraryItems.values()) {
                    if (libraryItem.composite == null) continue;
                    items = libraryItem.composite.getAllItems();
                }
            }
        }
    }

    public void reLoadProjectAssets() {
        ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        resourceManager.loadCurrentProjectAssets(currentProjectPath + "/assets/" + resolutionManager.currentResolutionName + "/pack/pack.atlas");
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

    private ArrayList<File> getScmlFileImagesList(FileHandle fileHandle) {
        ArrayList<File> images = new ArrayList<File>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
            org.w3c.dom.Document document = db.parse(fileHandle.file());
            NodeList nodeList = document.getElementsByTagName("file");
            for (int x = 0, size = nodeList.getLength(); x < size; x++) {
                String absolutePath = fileHandle.path();
                String path = absolutePath.substring(0, FilenameUtils.indexOfLastSeparator(fileHandle.path())) + File.separator + nodeList.item(x).getAttributes().getNamedItem("name").getNodeValue();
                File imgFile = new File(path);
                images.add(imgFile);
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return images;
    }


    public void importSpineAnimationsIntoProject(final Array<FileHandle> fileHandles, ProgressHandler progressHandler) {
        if (fileHandles == null) {
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            for (FileHandle handle : fileHandles) {
                File copiedFile = importExternalAnimationIntoProject(handle);
                if (copiedFile == null)
                    continue;

                if (copiedFile.getName().toLowerCase().endsWith(".atlas")) {
                    ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
                    resolutionManager.resizeSpineAnimationForAllResolutions(copiedFile, currentProjectInfoVO);
                }
            }

        });
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();

    }

    public File importExternalAnimationIntoProject(FileHandle animationFileSource) {
        try {
            String fileName = animationFileSource.name();
            if (!HyperLap2DUtils.JSON_FILTER.accept(null, fileName)) {
                return null;
            }

            String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
            String sourcePath;
            String animationDataPath;
            String targetPath;
            if (HyperLap2DUtils.JSON_FILTER.accept(null, fileName)) {
                sourcePath = animationFileSource.path();

                animationDataPath = FilenameUtils.getFullPathNoEndSeparator(sourcePath);
                targetPath = currentProjectPath + "/assets/orig/spine-animations" + File.separator + fileNameWithOutExt;
                FileHandle atlasFileSource = new FileHandle(animationDataPath + File.separator + fileNameWithOutExt + ".atlas");
                if (!atlasFileSource.exists()) {
                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                            "\nCould not find '" + atlasFileSource.name() +"'.\nCheck if the file exists in the same directory.").padBottom(20).pack();
                    return null;
                }
                Array<File> imageFiles = getAtlasPages(atlasFileSource);
                for (File imageFile : new Array.ArrayIterator<>(imageFiles)) {
                    if (!imageFile.exists()) {
                        Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                "\nCould not find " + imageFile.getName() + ".\nCheck if the file exists in the same directory.").padBottom(20).pack();
                        return null;
                    }
                }

                Version spineVersion = getSpineVersion(animationFileSource);
                if (spineVersion.compareTo(SpineItemType.SUPPORTED_SPINE_VERSION) < 0) {
                    Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                            "\nCould not import Spine Animation.\nRequired version >=" + SpineItemType.SUPPORTED_SPINE_VERSION.get() + " found " + spineVersion.get()).padBottom(20).pack();
                    return null;
                }

                FileUtils.forceMkdir(new File(targetPath));
                File jsonFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".json");
                File atlasFileTarget = new File(targetPath + File.separator + fileNameWithOutExt + ".atlas");

                FileUtils.copyFile(animationFileSource.file(), jsonFileTarget);
                FileUtils.copyFile(atlasFileSource.file(), atlasFileTarget);

                for (File imageFile : new Array.ArrayIterator<>(imageFiles)) {
                    FileHandle imgFileTarget = new FileHandle(targetPath + File.separator + imageFile.getName());
                    FileUtils.copyFile(imageFile, imgFileTarget.file());
                }

                return atlasFileTarget;


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void importSpriteAnimationsIntoProject(final Array<FileHandle> fileHandles, ProgressHandler progressHandler) {
        if (fileHandles == null) {
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            String newAnimName = null;

            String rawFileName = fileHandles.get(0).name();
            String fileExtension = FilenameUtils.getExtension(rawFileName);
            if (fileExtension.equals("png")) {
                TexturePacker texturePacker = new TexturePacker(getTexturePackerSettings());

                String fileNameWithoutExt = FilenameUtils.removeExtension(rawFileName);
                String fileNameWithoutFrame = fileNameWithoutExt.replaceAll("\\d*$", "").replace("_", "");

                boolean noFileNameWithoutFrame = false;
                if (Objects.equals(fileNameWithoutFrame, "")) {
                    fileNameWithoutFrame = fileHandles.get(0).parent().name();
                    noFileNameWithoutFrame = true;
                }

                String targetPath = currentProjectPath + "/assets/orig/sprite-animations" + File.separator + fileNameWithoutFrame;

                for (FileHandle file : fileHandles) {
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
            } else {
                for (FileHandle fileHandle : fileHandles) {
                    try {
                        Array<File> imgs = getAtlasPages(fileHandle);
                        String fileNameWithoutExt = getAtlasName(fileHandle);

                        String targetPath = currentProjectPath + "/assets/orig/sprite-animations" + File.separator + fileNameWithoutExt;
                        File targetDir = new File(targetPath);
                        if (targetDir.exists()) {
                            FileUtils.deleteDirectory(targetDir);
                        }
                        for (File img : imgs) {
                            FileUtils.copyFileToDirectory(img, targetDir);
                        }
                        File atlasTargetPath = new File(targetPath + File.separator + fileNameWithoutExt + ".atlas");
                        FileUtils.copyFile(fileHandle.file(), atlasTargetPath);
                        newAnimName = fileNameWithoutExt;
                    } catch (IOException e) {
                        e.printStackTrace();
                        progressHandler.progressFailed();
                        return;
                    }
                }
            }

            if (newAnimName != null) {
                ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
                resolutionManager.resizeSpriteAnimationForAllResolutions(newAnimName, currentProjectInfoVO);
            }
        });
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
    }

    private Array<File> getAtlasPages(FileHandle fileHandle) {
        Array<File> imgs = new Array<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileHandle.read()), 64);
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                //In atlas file format the name of the png is is preceded by an empty line
                if (line.trim().length() == 0) {
                    line = reader.readLine();
                    imgs.add(new File(FilenameUtils.getFullPath(fileHandle.path()) + line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgs;
    }

    private String getAtlasName(FileHandle fileHandle) {
        String name = "atlas";
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileHandle.read()), 64);
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                if (line.trim().contains("repeat:")) {
                    line = reader.readLine();
                    name = line;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    private Array<FileHandle> getAtlasPageHandles(FileHandle fileHandle) {
        Array<File> imgs = getAtlasPages(fileHandle);

        Array<FileHandle> imgHandles = new Array<>();
        for (int i = 0; i < imgs.size; i++) {
            imgHandles.add(new FileHandle(imgs.get(i)));
        }

        return imgHandles;
    }

    private Version getSpineVersion(FileHandle fileHandle) {
        Version version;

        String regex = "\"spine\" *: *\"(\\d+\\.\\d+\\.?\\d*)\"";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(fileHandle.readString());

        if (matcher.find()) {
            version = new Version(matcher.group(1));
        } else {
            version = new Version("0.0.0");
        }

        return version;
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

    public void importParticlesIntoProject(final Array<FileHandle> fileHandles, ProgressHandler progressHandler, boolean skipRepack) {
        if (fileHandles == null) {
            return;
        }
        final String targetPath = currentProjectPath + "/assets/orig/particles";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Array<FileHandle> images = new Array<>();
            for (FileHandle fileHandle : fileHandles) {
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
                copyImageFilesForAllResolutionsIntoProject(images, false, progressHandler);
            }
            if (!skipRepack) {
                ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
                resolutionManager.rePackProjectImagesForAllResolutionsSync();
            }
        });
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
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

    public void importTalosIntoProject(final Array<FileHandle> fileHandles, ProgressHandler progressHandler, boolean skipRepack) {
        if (fileHandles == null) {
            return;
        }
        Json json = new Json();
        json.setIgnoreUnknownFields(true);
        ParticleEmitterDescriptor.registerModules();
        for (Class clazz: ParticleEmitterDescriptor.registeredModules) {
            json.addClassTag(clazz.getSimpleName(), TalosResources.Module.class);
        }
        final String targetPath = currentProjectPath + "/assets/orig/talos-vfx";
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            Array<FileHandle> images = new Array<>();
            Array<FileHandle> assetsRes = new Array<>();
            for (FileHandle fileHandle : new Array.ArrayIterator<>(fileHandles)) {
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
                copyImageFilesForAllResolutionsIntoProject(images, false, progressHandler);
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
        });
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
    }

    public void importAtlasesIntoProject(final Array<FileHandle> files, ProgressHandler progressHandler) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            for (FileHandle fileHandle : files) {
                // TODO: logic goes here
            }
        });
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
    }


    public void importImagesIntoProject(final Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        if (files == null) {
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            copyImageFilesForAllResolutionsIntoProject(files, true, progressHandler);
            if (!skipRepack) {
                ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
                resolutionManager.rePackProjectImagesForAllResolutionsSync();
            }
        });
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
    }

    private void copyImageFilesForAllResolutionsIntoProject(Array<FileHandle> files, Boolean performResize, ProgressHandler handler) {
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

                // The filename should not be changed because the particle effects contain the name in their
                // configuration. Unfortunately though, the texture packer does not support the underscore because
                // any underscore in the texture packer is considered an image index. More info here:
                // https://github.com/libgdx/libgdx/wiki/Texture-packer#image-indexes
                // So, long story short, we MUST remove the underscore.
                ImageIO.write(bufferedImage, "png", new File(targetPath + "/" + handle.name().replace("_", "")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            handler.progressChanged(perCopyPercent);
        }

        return resizeWarningsCount;
    }

    public void importFontIntoProject(Array<FileHandle> fileHandles, ProgressHandler progressHandler) {
        if (fileHandles == null) {
            return;
        }
        String targetPath = currentProjectPath + "/assets/orig/freetypefonts";
        float perCopyPercent = 95.0f / fileHandles.size;
        for (FileHandle fileHandle : fileHandles) {
            if (!HyperLap2DUtils.TTF_FILTER.accept(null, fileHandle.name())) {
                continue;
            }
            try {
                File target = new File(targetPath);
                if (!target.exists()) {
                    File newFile = new File(targetPath);
                    newFile.mkdir();
                }
                File fileTarget = new File(targetPath + "/" + fileHandle.name());
                FileUtils.copyFile(fileHandle.file(), fileTarget);
            } catch (IOException e) {
                e.printStackTrace();
            }

            progressHandler.progressChanged(perCopyPercent);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                progressHandler.progressChanged(100);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressHandler.progressComplete();
            });
            executor.shutdown();
        }
    }

    public void importStyleIntoProject(final FileHandle handle, ProgressHandler progressHandler) {
        if (handle == null) {
            return;
        }
        final String targetPath = currentProjectPath + "/assets/orig/styles";
        FileHandle fileHandle = Gdx.files.absolute(handle.path());
        final MySkin skin = new MySkin(fileHandle);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            for (int i = 0; i < skin.fontFiles.size(); i++) {
                File copyFontFile = new File(handle.path(), skin.fontFiles.get(i) + ".fnt");
                File copyImageFile = new File(handle.path(), skin.fontFiles.get(i) + ".png");
                if (!handle.isDirectory() && handle.exists() && copyFontFile.isFile() && copyFontFile.exists() && copyImageFile.isFile() && copyImageFile.exists()) {
                    File fileTarget = new File(targetPath + "/" + handle.name());
                    File fontTarget = new File(targetPath + "/" + copyFontFile.getName());
                    File imageTarget = new File(targetPath + "/" + copyImageFile.getName());
                    try {
                        FileUtils.copyFile(handle.file(), fileTarget);
                        FileUtils.copyFile(copyFontFile, fontTarget);
                        FileUtils.copyFile(copyImageFile, imageTarget);
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("SOME FILES ARE MISSING");
                }
            }
        });
        executor.execute(new Runnable() {
            @Override
            public void run() {
                progressHandler.progressChanged(100);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressHandler.progressComplete();
            }
        });
        executor.shutdown();
    }

    /**
     * @depricated
     */
    public void copyDefaultStyleIntoProject() {
        /*
        String targetPath = currentWorkingPath + "/" + currentProjectVO.projectName + "/assets/orig/styles";
        ResourceManager textureManager = facade.retrieveProxy(ResourceManager.NAME);
        File source = new File("assets/ui");
        if (!(source.exists() && source.isDirectory())) {
            try {
                JarUtils.copyResourcesToDirectory(JarUtils.getThisJar(getClass()), "ui", targetPath);
                textureManager.loadCurrentProjectSkin(targetPath);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File fileTarget = new File(targetPath);
        try {
            FileUtils.copyDirectory(source, fileTarget);
            textureManager.loadCurrentProjectSkin(targetPath);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        */
    }

    public String getFreeTypeFontPath() {
        return currentProjectPath + "/assets/orig/freetypefonts";
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
        exportFonts(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            exportFonts(currentProjectVO.projectMainExportPath);
        }
        exportStyles(defaultBuildPath);
        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
        sceneDataManager.buildScenes(defaultBuildPath);
        if (!currentProjectVO.projectMainExportPath.isEmpty()) {
            sceneDataManager.buildScenes(currentProjectVO.projectMainExportPath);
        }
    }

    private void exportStyles(String targetPath) {
        String srcPath = currentProjectPath + "/assets/orig";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle stylesDirectory = origDirectoryHandle.child("styles");
        File fileTarget = new File(targetPath + "/" + stylesDirectory.name());
        try {
            FileUtils.copyDirectory(stylesDirectory.file(), fileTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportShaders(String targetPath) {
        String srcPath = currentProjectPath + "/assets";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle shadersDirectory = origDirectoryHandle.child("shaders");
        File fileTarget = new File(targetPath + "/" + shadersDirectory.name());
        try {
            FileUtils.copyDirectory(shadersDirectory.file(), fileTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportParticles(String targetPath) {
        String srcPath = currentProjectPath + "/assets/orig";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle particlesDirectory = origDirectoryHandle.child("particles");
        File fileTarget = new File(targetPath + "/" + particlesDirectory.name());
        try {
            FileUtils.copyDirectory(particlesDirectory.file(), fileTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportTalosVFX(String targetPath) {
        String srcPath = currentProjectPath + "/assets/orig";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle particlesDirectory = origDirectoryHandle.child("talos-vfx");
        File fileTarget = new File(targetPath + "/" + particlesDirectory.name());
        try {
            FileUtils.copyDirectory(particlesDirectory.file(), fileTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportFonts(String targetPath) {
        String srcPath = currentProjectPath + "/assets/orig";
        FileHandle origDirectoryHandle = Gdx.files.absolute(srcPath);
        FileHandle fontsDirectory = origDirectoryHandle.child("freetypefonts");
        File fileTarget = new File(targetPath + "/" + fontsDirectory.name());
        try {
            FileUtils.copyDirectory(fontsDirectory.file(), fileTarget);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void exportAnimations(String targetPath) {
        exportSpineAnimationForResolution("orig", targetPath);
        exportSpriteAnimationForResolution("orig", targetPath);
        for (ResolutionEntryVO resolutionEntryVO : currentProjectInfoVO.resolutions) {
            exportSpineAnimationForResolution(resolutionEntryVO.name, targetPath);
            exportSpriteAnimationForResolution(resolutionEntryVO.name, targetPath);
        }
    }

    private void exportSpineAnimationForResolution(String res, String targetPath) {
        String spineSrcPath = currentProjectPath + "/assets/" + res + File.separator + "spine-animations";
        try {
            FileUtils.forceMkdir(new File(targetPath + File.separator + res + File.separator + "spine_animations"));
            File fileSrc = new File(spineSrcPath);
            String finalTarget = targetPath + File.separator + res + File.separator + "spine_animations";

            File fileTargetSpine = new File(finalTarget);

            FileUtils.copyDirectory(fileSrc, fileTargetSpine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exportSpriteAnimationForResolution(String res, String targetPath) {
        String spineSrcPath = currentProjectPath + "/assets/" + res + File.separator + "sprite-animations";
        try {
            FileUtils.forceMkdir(new File(targetPath + File.separator + res + File.separator + "sprite_animations"));
            File fileSrc = new File(spineSrcPath);
            String finalTarget = targetPath + File.separator + res + File.separator + "sprite_animations";

            File fileTargetSprite = new File(finalTarget);

            FileUtils.copyDirectory(fileSrc, fileTargetSprite);
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

    public void openProjectFromPath(String path) {
        File projectFile = new File(path);
        File projectFolder = projectFile.getParentFile();
        String projectName = projectFolder.getName();
        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        settingsManager.setLastOpenedPath(projectFolder.getParentFile().getPath());

        // here we load all data
        openProjectAndLoadAllData(projectFolder.getPath());
        Sandbox.getInstance().loadCurrentProject();

        facade.sendNotification(ProjectManager.PROJECT_OPENED);

        //Set title with opened file path
        setWindowTitle(getFormattedTitle(path));
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

    public void importShaderIntoProject(Array<FileHandle> files, ProgressHandler progressHandler) {
        if (files == null) {
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            for (FileHandle handle : files) {
                // check if shaders folder exists
                String shadersPath = currentProjectPath + "/assets/shaders";
                File destination = new File(currentProjectPath + "/assets/shaders/" + handle.name());
                try {
                    FileUtils.forceMkdir(new File(shadersPath));
                    FileUtils.copyFile(handle.file(), destination);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
    }

    public void importItemLibraryIntoProject(Array<FileHandle> files, ProgressHandler progressHandler) {
        if (files == null) {
            return;
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            for (FileHandle handle : files) {
                Json json = new Json();
                String projectInfoContents = null;
                try {
                    projectInfoContents = FileUtils.readFileToString(handle.file(), "utf-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                CompositeItemVO voInfo = json.fromJson(CompositeItemVO.class, projectInfoContents);
				adjustPPWCoordinates(voInfo);

                String fileNameAndExtension = handle.name();
                String fileName = FilenameUtils.removeExtension(fileNameAndExtension);
                this.currentProjectInfoVO.libraryItems.put(fileName, voInfo);
                saveCurrentProject();
            }
        });
        executor.execute(() -> {
            progressHandler.progressChanged(100);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            progressHandler.progressComplete();
        });
        executor.shutdown();
    }

	private void adjustPPWCoordinates(CompositeItemVO compositeItemVO) {
		for (MainItemVO item : compositeItemVO.composite.getAllItems()) {
			item.originX = item.originX / getCurrentProjectInfoVO().pixelToWorld;
			item.originY = item.originY / getCurrentProjectInfoVO().pixelToWorld;
			item.x = item.x / getCurrentProjectInfoVO().pixelToWorld;
			item.y = item.y / getCurrentProjectInfoVO().pixelToWorld;

			if (item instanceof CompositeItemVO) {
				((CompositeItemVO) item).width = ((CompositeItemVO) item).width / getCurrentProjectInfoVO().pixelToWorld;
				((CompositeItemVO) item).height = ((CompositeItemVO) item).height / getCurrentProjectInfoVO().pixelToWorld;
			}

			if (item instanceof Image9patchVO) {
				((Image9patchVO) item).width = ((Image9patchVO) item).width / getCurrentProjectInfoVO().pixelToWorld;
				((Image9patchVO) item).height = ((Image9patchVO) item).height / getCurrentProjectInfoVO().pixelToWorld;
			}

			if (item instanceof LabelVO) {
				((LabelVO) item).width = ((LabelVO) item).width / getCurrentProjectInfoVO().pixelToWorld;
				((LabelVO) item).height = ((LabelVO) item).height / getCurrentProjectInfoVO().pixelToWorld;
			}
		}
	}

    private ProgressHandler recursiveProgressHandler = null;
    private int recursiveProgressIndex = 0;
    public void importHyperLapLibraryIntoProject(final Array<FileHandle> files, ProgressHandler progressHandler) {
        recursiveProgressIndex = 0;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            File tmpDir = new File(getCurrentProjectPath() + "/assets/tmp/");
            try {
                for (FileHandle fileHandle : files) {
                    FileUtils.deleteDirectory(tmpDir);
                    FileUtils.forceMkdir(tmpDir);
                    FileHandle mapper = ZipUtils.saveZipContent(fileHandle.file(), tmpDir);
                    Json json = new Json();
                    json.setIgnoreUnknownFields(true);
                    ExportMapperVO exportMapperVO = json.fromJson(ExportMapperVO.class, mapper);

                    recursiveProgressHandler = new ProgressHandler() {
                        @Override
                        public void progressStarted() { }
                        @Override
                        public void progressChanged(float value) { }
                        @Override
                        public void progressComplete() {
                            recursiveProgressIndex++;
                            if (recursiveProgressIndex < exportMapperVO.mapper.size) {
                                progressHandler.progressChanged(80f * recursiveProgressIndex / (exportMapperVO.mapper.size - 1));
                                ExportMapperVO.ExportedAsset asset = exportMapperVO.mapper.get(recursiveProgressIndex);
                                AssetImporter.getInstance().startImport(asset.type, true, recursiveProgressHandler, tmpDir.getPath() + File.separator + asset.fileName);
                            } else {
                                try {
                                    FileUtils.deleteDirectory(tmpDir);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ResolutionManager resolutionManager = HyperLap2DFacade.getInstance().retrieveProxy(ResolutionManager.NAME);
                                resolutionManager.rePackProjectImagesForAllResolutionsSync();

                                progressHandler.progressChanged(100);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                progressHandler.progressComplete();
                                executor.shutdown();
                            }
                        }
                        @Override
                        public void progressFailed() { }
                    };
                    if (recursiveProgressIndex < exportMapperVO.mapper.size) {
                        ExportMapperVO.ExportedAsset asset = exportMapperVO.mapper.get(recursiveProgressIndex);
                        AssetImporter.getInstance().startImport(asset.type, true, recursiveProgressHandler, tmpDir.getPath() + File.separator + asset.fileName);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public String getCurrentProjectPath() {
        return currentProjectPath;
    }

    public String getCurrentRawImagesPath() {
        return currentProjectPath + File.separator + "assets" + File.separator + "orig" + File.separator + "images";
    }

    private boolean deleteSingleImage(String resolutionName, String imageName) {
        String imagesPath = currentProjectPath + "/assets/" + resolutionName + "/images" + File.separator;
        String filePath = imagesPath + imageName + ".png";
        if (!(new File(filePath)).delete()) {
            filePath = imagesPath + imageName + ".9.png";
            return (new File(filePath)).delete();
        }
        return true;
    }

    public boolean deleteSingleImageForAllResolutions(String imageName) {
        for (ResolutionEntryVO resolutionEntryVO : currentProjectInfoVO.resolutions) {
            if(!deleteSingleImage(resolutionEntryVO.name, imageName))
                return false;
        }
        return deleteSingleImage("orig", imageName);
    }

    public boolean deleteParticle(String particleName) {
        String particlePath = currentProjectPath + File.separator + PARTICLE_DIR_PATH + File.separator;
        String filePath = particlePath + particleName;
        return (new File(filePath)).delete();
    }

    public boolean deleteTalosVFX(String particleName) {
        String particlePath = currentProjectPath + File.separator + TALOS_VFX_DIR_PATH + File.separator;
        String filePath = particlePath + particleName;
        return (new File(filePath)).delete();
    }

    private boolean deleteSpineAnimation(String resolutionName, String spineName) {
        String spinePath = currentProjectPath + "/assets/" + resolutionName + "/spine-animations" + File.separator;
        String filePath = spinePath + spineName;
        return deleteDirectory(filePath);
    }

    public boolean deleteSpineForAllResolutions(String spineName) {
        for (ResolutionEntryVO resolutionEntryVO : currentProjectInfoVO.resolutions) {
            if(!deleteSpineAnimation(resolutionEntryVO.name, spineName))
                return false;
        }
        return deleteSpineAnimation("orig", spineName);
    }

    private boolean deleteSpriteAnimation(String resolutionName, String spineName) {
        String spritePath = currentProjectPath + "/assets/" + resolutionName + "/sprite-animations" + File.separator;
        String filePath = spritePath + spineName;
        return deleteDirectory(filePath);
    }

    public boolean deleteSpriteAnimationForAllResolutions(String spineName) {
        for (ResolutionEntryVO resolutionEntryVO : currentProjectInfoVO.resolutions) {
            if(!deleteSpriteAnimation(resolutionEntryVO.name, spineName))
                return false;
        }
        return deleteSpriteAnimation("orig", spineName);
    }

    private boolean deleteDirectory(String path) {
        File file = new File(path);
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!file.exists()) {
                return true;
            }
        }
        return false;
    }
}