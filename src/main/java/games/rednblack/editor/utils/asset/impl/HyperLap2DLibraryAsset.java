package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.ZipUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HyperLap2DLibraryAsset extends Asset {
    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equalsIgnoreCase("h2dlib");
    }

    @Override
    public int getType() {
        return AssetsUtils.TYPE_HYPERLAP2D_LIBRARY;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        boolean exists = false;
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            String itemName = ZipUtils.getZipContent(file.file(), "lib").get(0).replace(".lib", "");
            if (projectManager.getCurrentProjectInfoVO().libraryItems.get(itemName) != null) {
                exists = true;
            }
        }
        return exists;
    }

    private final Object lock = new Object();
    private int importedLibraries = 0;
    private int scheduledLibraries = 0;

    @Override
    public void asyncImport(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        if (files == null) {
            progressHandler.progressChanged(100);
            progressHandler.progressComplete();
            return;
        }

        // save before importing
        SceneVO vo = Sandbox.getInstance().sceneVoFromItems();
        projectManager.saveCurrentProject(vo);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            File tmpDir = new File(projectManager.getCurrentProjectPath() + "/assets/tmp/");
            try {
                FileUtils.deleteDirectory(tmpDir);
                FileUtils.forceMkdir(tmpDir);

                importedLibraries = 0;
                scheduledLibraries = files.size;
                boolean importStarted = false;
                for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
                    File libTmpDir = new File(tmpDir.getPath() + File.separator + fileHandle.nameWithoutExtension());
                    FileUtils.deleteDirectory(libTmpDir);
                    FileUtils.forceMkdir(libTmpDir);
                    FileHandle mapper = ZipUtils.saveZipContent(fileHandle.file(), libTmpDir);
                    Json json = HyperJson.getJson();
                    ExportMapperVO exportMapperVO = json.fromJson(ExportMapperVO.class, mapper);
                    if (!exportMapperVO.projectVersion.equals(projectManager.currentProjectVO.projectVersion)) {
                        Dialogs.showErrorDialog(Sandbox.getInstance().getUIStage(),
                                "\nCould not import '" + fileHandle.name() + "'.\nLibrary version does not match current Project version.")
                                .padBottom(20).pack();
                        scheduledLibraries--;
                        continue;
                    }

                    ProgressHandler recursiveProgressHandler = new ProgressHandler() {
                        private int recursiveProgressIndex = 0;

                        @Override
                        public void progressStarted() { }
                        @Override
                        public void progressChanged(float value) { }
                        @Override
                        public void progressComplete() {
                            forwardImport();
                        }
                        @Override
                        public void progressFailed() {
                            forwardImport();
                        }

                        private void forwardImport() {
                            recursiveProgressIndex++;
                            if (recursiveProgressIndex < exportMapperVO.mapper.size) {
                                progressHandler.progressChanged(80f * recursiveProgressIndex / (exportMapperVO.mapper.size - 1));
                                ExportMapperVO.ExportedAsset asset = exportMapperVO.mapper.get(recursiveProgressIndex);
                                FileHandle file = new FileHandle(libTmpDir.getPath() + File.separator + asset.fileName);
                                AssetIOManager.getInstance().importInternalResource(file, this);
                            } else {
                                try {
                                    FileUtils.deleteDirectory(libTmpDir);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                synchronized (lock) {
                                    importedLibraries++;

                                    if (importedLibraries == scheduledLibraries) {
                                        try {
                                            FileUtils.deleteDirectory(tmpDir);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                        ResolutionManager resolutionManager = HyperLap2DFacade.getInstance().retrieveProxy(ResolutionManager.NAME);
                                        resolutionManager.rePackProjectImagesForAllResolutionsSync();

                                        progressHandler.progressChanged(100);

                                        progressHandler.progressComplete();
                                        executor.shutdown();
                                    }
                                }
                            }
                        }
                    };

                    if (exportMapperVO.mapper.size > 0) {
                        importStarted = true;
                        ExportMapperVO.ExportedAsset asset = exportMapperVO.mapper.get(0);
                        FileHandle file = new FileHandle(libTmpDir.getPath() + File.separator + asset.fileName);
                        AssetIOManager.getInstance().importInternalResource(file, recursiveProgressHandler);
                    }
                }

                if (!importStarted) {
                    try {
                        FileUtils.deleteDirectory(tmpDir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    progressHandler.progressChanged(100);
                    progressHandler.progressComplete();
                    executor.shutdown();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void importAsset(Array<FileHandle> files, ProgressHandler progressHandler, boolean skipRepack) {
        throw new GdxRuntimeException("Use asyncImport()");
    }

    @Override
    public boolean deleteAsset(int root, String name) {
        HashMap<String, CompositeItemVO> libraryItems = projectManager.currentProjectInfoVO.libraryItems;

        libraryItems.remove(name);

        Array<Integer> linkedEntities = EntityUtils.getByLibraryLink(name);
        for (int entity : linkedEntities) {
            MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
            mainItemComponent.libraryLink = "";
        }

        return true;
    }
}
