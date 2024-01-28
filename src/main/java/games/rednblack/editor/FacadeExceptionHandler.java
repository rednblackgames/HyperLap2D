package games.rednblack.editor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class FacadeExceptionHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();

        CustomExceptionHandler.showErrorDialog(null, stacktrace);
    }
}
