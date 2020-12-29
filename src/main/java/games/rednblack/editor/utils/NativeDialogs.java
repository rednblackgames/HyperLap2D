package games.rednblack.editor.utils;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NativeDialogs {

    public static void showError(String message) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> TinyFileDialogs.tinyfd_messageBox("Oops! Something went wrong", message,
                "ok", "error", true));
        executor.shutdown();
    }
}
