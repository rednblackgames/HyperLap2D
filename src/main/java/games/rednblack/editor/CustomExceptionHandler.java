package games.rednblack.editor;

import games.rednblack.editor.utils.AppConfig;
import games.rednblack.editor.utils.HyperLap2DUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;

public class CustomExceptionHandler implements UncaughtExceptionHandler {

    public CustomExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        e.printStackTrace();
        String stacktrace = result.toString();
        writeToFile(stacktrace);
        printWriter.close();

        showErrorDialog(stacktrace);
    }

    public static void showErrorDialog(String stacktrace) {
        File localPath = new File(HyperLap2DUtils.getRootPath()
                + File.separator + "crash" + File.separator + "java-hyperlog.txt");
        stacktrace = stacktrace.replace("<", "");
        stacktrace = stacktrace.replace(">", "");
        stacktrace = stacktrace.replace("$", "");
        stacktrace = stacktrace.replace("'", "");
        stacktrace = stacktrace.replace("\"", "");

        TinyFileDialogs.tinyfd_messageBox("Oops! Something went wrong",
                "HyperLap2D just crashed, stacktrace saved in: " + localPath.getAbsolutePath()
                        + "\n\n System: " + SystemUtils.OS_NAME + " " + SystemUtils.OS_VERSION + " (HyperLap2D v" + AppConfig.getInstance().versionString + ")"
                        + "\n\n" + stacktrace,
                "ok", "error", true);
        System.exit(-1);
    }

    private void writeToFile(String stacktrace) {
        try {
            File localPath = new File(HyperLap2DUtils.getRootPath()
                    + File.separator + "crash" + File.separator + "java-hyperlog.txt");
            System.out.println(localPath.getAbsolutePath());
            FileUtils.writeStringToFile(localPath, stacktrace, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
