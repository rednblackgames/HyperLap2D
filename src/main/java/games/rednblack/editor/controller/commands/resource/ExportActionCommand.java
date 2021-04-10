package games.rednblack.editor.controller.commands.resource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import games.rednblack.editor.controller.commands.NonRevertibleCommand;
import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.io.FileUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExportActionCommand extends NonRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.ExportActionCommand";
    public static final String DONE = CLASS_NAME + "DONE";
    private final Json json = new Json(JsonWriter.OutputType.json);

    public ExportActionCommand() {
        cancel();
        setShowConfirmDialog(false);
    }

    @Override
    public void doAction() {
        String actionName = notification.getBody();

        facade.sendNotification(MsgAPI.SHOW_BLACK_OVERLAY);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer aFilterPatterns = stack.mallocPointer(1);
                aFilterPatterns.put(stack.UTF8("*.h2daction"));
                aFilterPatterns.flip();

                FileHandle workspacePath = (settingsManager.getWorkspacePath() == null || !settingsManager.getWorkspacePath().exists()) ?
                        Gdx.files.absolute(System.getProperty("user.home")) : settingsManager.getWorkspacePath();

                String fileName = TinyFileDialogs.tinyfd_saveFileDialog("Export Action Item...",
                        workspacePath.path() + File.separator + actionName, aFilterPatterns,
                        "HyperLap2D Action (*.h2daction)");

                if (fileName != null) {
                    String fullFileName = fileName.endsWith(".h2daction") ? fileName : fileName + ".h2daction";

                    FileHandle file = new FileHandle(new File(fullFileName));

                    try {
                        if (file.exists()) {
                            FileUtils.forceDelete(file.file());
                        }

                        doExport(actionName, file);

                        Gdx.app.postRunnable(() -> {
                            facade.sendNotification(DONE, actionName);
                            facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "'" + actionName +"' successfully exported");
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Gdx.app.postRunnable(() -> {
                    facade.sendNotification(MsgAPI.HIDE_BLACK_OVERLAY);
                });
            }
        });
        executor.shutdown();
    }

    private void doExport(String actionName, FileHandle destFile) throws IOException  {
        GraphVO action = projectManager.getCurrentProjectInfoVO().libraryActions.get(actionName);
        if (action != null) {
            FileUtils.writeStringToFile(destFile.file(), json.toJson(action), "utf-8");
        } else {
            throw new IOException("Action not found.");
        }
    }
}
