package games.rednblack.editor.controller.commands.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.controller.commands.NonRevertibleCommand;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.AssetImporter;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.utils.ZipUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import games.rednblack.h2d.common.vo.ExportMapperVO.ExportedAsset;
import games.rednblack.h2d.extension.spine.SpineVO;
import games.rednblack.h2d.extension.talos.TalosVO;
import org.apache.commons.io.FileUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportLibraryItemCommand extends NonRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.ExportLibraryItemCommand";
    public static final String DONE = CLASS_NAME + "DONE";
    private final Json json = HyperJson.getJson();

    private final String currentProjectPath;
    private final ResourceManager resourceManager;
    private final ExportMapperVO exportMapperVO;

    public ExportLibraryItemCommand() {
        cancel();
        setShowConfirmDialog(false);
        currentProjectPath = projectManager.getCurrentProjectPath() + File.separator;
        resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        exportMapperVO = new ExportMapperVO();
    }

    @Override
    public void doAction() {
        exportMapperVO.mapper.clear();
		exportMapperVO.projectVersion = projectManager.currentProjectVO.projectVersion;

        String libraryItemName = notification.getBody();

        facade.sendNotification(MsgAPI.SHOW_BLACK_OVERLAY);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer aFilterPatterns = stack.mallocPointer(1);
                aFilterPatterns.put(stack.UTF8("*.h2dlib"));
                aFilterPatterns.flip();

                FileHandle workspacePath = (settingsManager.getWorkspacePath() == null || !settingsManager.getWorkspacePath().exists()) ?
                        Gdx.files.absolute(System.getProperty("user.home")) : settingsManager.getWorkspacePath();

                String fileName = TinyFileDialogs.tinyfd_saveFileDialog("Export Library Item...",
                        workspacePath.path() + File.separator + libraryItemName, aFilterPatterns,
                        "HyperLap2D Library (*.h2dlib)");
                Gdx.app.postRunnable(() -> {
                    facade.sendNotification(MsgAPI.HIDE_BLACK_OVERLAY);
                    if (fileName != null) {
                        String fullFileName = fileName.endsWith(".h2dlib") ? fileName : fileName + ".h2dlib";

                        FileHandle file = new FileHandle(new File(fullFileName));
                        try {
                            if (file.exists()) {
                                FileUtils.forceDelete(file.file());
                            }
                            doExport(libraryItemName, file.pathWithoutExtension());

                            facade.sendNotification(DONE, libraryItemName);
                            facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "'" + libraryItemName +"' successfully exported");
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                FileUtils.deleteDirectory(new File(file.pathWithoutExtension() + "TMP"));
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
        executor.shutdown();
    }

    private void doExport(String libraryItemName, String destFile) throws IOException  {
        File tempDir = new File(destFile + "TMP");
        FileUtils.forceMkdir(tempDir);

        CompositeItemVO compositeItemVO = libraryItems.get(libraryItemName).clone();
		adjustPPWCoordinates(compositeItemVO);

        FileUtils.writeStringToFile(new File(tempDir.getPath() + File.separator + libraryItemName + ".lib"), json.toJson(compositeItemVO), "utf-8");

        exportAllAssets(compositeItemVO, tempDir);

        exportMapperVO.mapper.add(new ExportedAsset(ImportUtils.TYPE_HYPERLAP2D_INTERNAL_LIBRARY, libraryItemName + ".lib"));

        FileUtils.writeStringToFile(new File(tempDir.getPath() + File.separator + "mapper"), json.toJson(exportMapperVO), "utf-8");

        ZipUtils.pack(tempDir.getPath(), destFile + ".h2dlib");
        FileUtils.deleteDirectory(tempDir);
    }

    private void exportAllAssets(CompositeItemVO compositeVO, File tmpDir) throws IOException {
        for (SimpleImageVO imageVO : compositeVO.getElementsArray(SimpleImageVO.class)) {
            AssetImporter.getInstance().exportAsset(ImportUtils.TYPE_IMAGE, imageVO, exportMapperVO, tmpDir);
        }

        for (Image9patchVO imageVO : compositeVO.getElementsArray(Image9patchVO.class)) {
            AssetImporter.getInstance().exportAsset(ImportUtils.TYPE_IMAGE, imageVO, exportMapperVO, tmpDir);
        }

        for (SpineVO imageVO : compositeVO.getElementsArray(SpineVO.class)) {
            AssetImporter.getInstance().exportAsset(ImportUtils.TYPE_SPINE_ANIMATION, imageVO, exportMapperVO, tmpDir);
        }

        for (SpriteAnimationVO imageVO : compositeVO.getElementsArray(SpriteAnimationVO.class)) {
            AssetImporter.getInstance().exportAsset(ImportUtils.TYPE_SPRITE_ANIMATION_ATLAS, imageVO, exportMapperVO, tmpDir);
        }

        for (ParticleEffectVO imageVO : compositeVO.getElementsArray(ParticleEffectVO.class)) {
            AssetImporter.getInstance().exportAsset(ImportUtils.TYPE_PARTICLE_EFFECT, imageVO, exportMapperVO, tmpDir);
        }

        for (TalosVO imageVO : compositeVO.getElementsArray(TalosVO.class)) {
            AssetImporter.getInstance().exportAsset(ImportUtils.TYPE_TALOS_VFX, imageVO, exportMapperVO, tmpDir);
        }

        for (CompositeItemVO compositeItemVO : compositeVO.getElementsArray(CompositeItemVO.class)) {
            exportAllAssets(compositeItemVO, tmpDir);
        }
    }

	private void adjustPPWCoordinates(CompositeItemVO compositeItemVO) {
		for (MainItemVO item : compositeItemVO.getAllItems()) {
			item.originX = item.originX * projectManager.getCurrentProjectInfoVO().pixelToWorld;
			item.originY = item.originY * projectManager.getCurrentProjectInfoVO().pixelToWorld;
			item.x = item.x * projectManager.getCurrentProjectInfoVO().pixelToWorld;
			item.y = item.y * projectManager.getCurrentProjectInfoVO().pixelToWorld;

			if (item instanceof CompositeItemVO) {
				((CompositeItemVO) item).width = ((CompositeItemVO) item).width * projectManager.getCurrentProjectInfoVO().pixelToWorld;
				((CompositeItemVO) item).height = ((CompositeItemVO) item).height * projectManager.getCurrentProjectInfoVO().pixelToWorld;
			}

			if (item instanceof Image9patchVO) {
				((Image9patchVO) item).width = ((Image9patchVO) item).width * projectManager.getCurrentProjectInfoVO().pixelToWorld;
				((Image9patchVO) item).height = ((Image9patchVO) item).height * projectManager.getCurrentProjectInfoVO().pixelToWorld;
			}

			if (item instanceof LabelVO) {
				((LabelVO) item).width = ((LabelVO) item).width * projectManager.getCurrentProjectInfoVO().pixelToWorld;
				((LabelVO) item).height = ((LabelVO) item).height * projectManager.getCurrentProjectInfoVO().pixelToWorld;
			}
		}
	}
}
