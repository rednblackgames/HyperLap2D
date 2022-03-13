package games.rednblack.editor.controller.commands.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.controller.commands.NonRevertibleCommand;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.utils.ZipUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.vo.ExportMapperVO;
import games.rednblack.h2d.common.vo.ExportMapperVO.ExportedAsset;
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

    private final ExportMapperVO exportMapperVO;

    public ExportLibraryItemCommand() {
        cancel();
        setShowConfirmDialog(false);
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

        for (MainItemVO itemVO : compositeItemVO.getAllItems()) {
            if (itemVO instanceof CompositeItemVO) continue;
            AssetIOManager.getInstance().exportAsset(itemVO, exportMapperVO, tempDir);
        }

        exportMapperVO.mapper.add(new ExportedAsset(AssetsUtils.TYPE_HYPERLAP2D_INTERNAL_LIBRARY, libraryItemName + ".lib"));

        FileUtils.writeStringToFile(new File(tempDir.getPath() + File.separator + "mapper"), json.toJson(exportMapperVO), "utf-8");

        ZipUtils.pack(tempDir.getPath(), destFile + ".h2dlib");
        FileUtils.deleteDirectory(tempDir);
    }

	private void adjustPPWCoordinates(CompositeItemVO compositeItemVO) {
        int ppwu = projectManager.getCurrentProjectInfoVO().pixelToWorld;
        compositeItemVO.width *= ppwu;
        compositeItemVO.height *= ppwu;

		for (MainItemVO item : compositeItemVO.getAllItems()) {
			item.originX = item.originX * ppwu;
			item.originY = item.originY * ppwu;
			item.x = item.x * ppwu;
			item.y = item.y * ppwu;

            if (item.shape != null) {
                PolygonShapeVO shapeVO = item.shape;
                if (shapeVO.vertices != null) {
                    for (Vector2 vector2 : shapeVO.vertices) {
                        vector2.x *= ppwu;
                        vector2.y *= ppwu;
                    }
                }

                if (shapeVO.polygonizedVertices != null) {
                    for (Vector2[] array : shapeVO.polygonizedVertices) {
                        for (Vector2 vector2 : array) {
                            vector2.x *= ppwu;
                            vector2.y *= ppwu;
                        }
                    }
                }
            }

            if (item.physics != null) {
                PhysicsBodyDataVO physicsBodyDataVO = item.physics;
                physicsBodyDataVO.centerOfMass.x *= ppwu;
                physicsBodyDataVO.centerOfMass.y *= ppwu;
            }

            if (item.light != null) {
                LightBodyDataVO lightBodyDataVO = item.light;
                lightBodyDataVO.distance *= ppwu;
            }

            if (item.circle != null) {
                item.circle.x *= ppwu;
                item.circle.y *= ppwu;
                item.circle.radius *= ppwu;
            }

			if (item instanceof CompositeItemVO) {
				((CompositeItemVO) item).width = ((CompositeItemVO) item).width * ppwu;
				((CompositeItemVO) item).height = ((CompositeItemVO) item).height * ppwu;
			}

			if (item instanceof Image9patchVO) {
				((Image9patchVO) item).width = ((Image9patchVO) item).width * ppwu;
				((Image9patchVO) item).height = ((Image9patchVO) item).height * ppwu;
			}

			if (item instanceof LabelVO) {
				((LabelVO) item).width = ((LabelVO) item).width * ppwu;
				((LabelVO) item).height = ((LabelVO) item).height * ppwu;
			}

            if (item instanceof LightVO) {
                ((LightVO) item).distance *= ppwu;
            }
		}
	}
}
