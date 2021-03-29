package games.rednblack.editor.utils.asset.impl;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetImporter;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.ZipUtils;
import games.rednblack.editor.utils.asset.Asset;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HyperLap2DLibraryAsset extends Asset {
    @Override
    protected boolean matchMimeType(FileHandle file) {
        return file.extension().equalsIgnoreCase("h2dlib");
    }

    @Override
    protected int getType() {
        return ImportUtils.TYPE_HYPERLAP2D_LIBRARY;
    }

    @Override
    public boolean checkExistence(Array<FileHandle> files) {
        boolean exists = false;
        for (FileHandle file : new Array.ArrayIterator<>(files)) {
            String itemName = ZipUtils.getZipContent(file.file(), "lib").get(0).replace(".lib", "");
            ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
            if (projectManager.getCurrentProjectInfoVO().libraryItems.get(itemName) != null) {
                exists = true;
            }
        }
        return exists;
    }

    private ProgressHandler recursiveProgressHandler = null;
    private int recursiveProgressIndex = 0;

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

        recursiveProgressIndex = 0;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            File tmpDir = new File(projectManager.getCurrentProjectPath() + "/assets/tmp/");
            try {
                for (FileHandle fileHandle : new Array.ArrayIterator<>(files)) {
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
                                FileHandle file = new FileHandle(tmpDir.getPath() + File.separator + asset.fileName);
                                AssetImporter.getInstance().importInternalResource(file, recursiveProgressHandler);
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
                        FileHandle file = new FileHandle(tmpDir.getPath() + File.separator + asset.fileName);
                        AssetImporter.getInstance().importInternalResource(file, recursiveProgressHandler);
                    }
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
}
