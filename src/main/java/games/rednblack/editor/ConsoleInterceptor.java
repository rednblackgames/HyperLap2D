package games.rednblack.editor;

import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.patterns.facade.Facade;

import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleInterceptor extends PrintStream {
    private final Facade facade;
    private String prefix = null, suffix = null;

    public ConsoleInterceptor(OutputStream out) {
        super(out, true);
        facade = HyperLap2DFacade.getInstance();
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void print(String s) {
        super.print(s);

        sendToConsole(s);
    }

    @Override
    public void print(boolean b) {
        super.print(b);

        sendToConsole(String.valueOf(b));
    }

    @Override
    public void print(int i) {
        super.print(i);

        sendToConsole(String.valueOf(i));
    }

    @Override
    public void print(char c) {
        super.print(c);

        sendToConsole(String.valueOf(c));
    }

    @Override
    public void print(long l) {
        super.print(l);

        sendToConsole(String.valueOf(l));
    }

    @Override
    public void print(float f) {
        super.print(f);

        sendToConsole(String.valueOf(f));
    }

    @Override
    public void print(char[] s) {
        super.print(s);

        sendToConsole(String.valueOf(s));
    }

    @Override
    public void print(double d) {
        super.print(d);

        sendToConsole(String.valueOf(d));
    }

    @Override
    public void print(Object obj) {
        super.print(obj);

        sendToConsole(String.valueOf(obj));
    }

    private void sendToConsole(String s) {
        if (prefix != null) {
            facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, prefix);
        }

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, s);

        if (suffix != null) {
            facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, suffix);
        }
    }

    @Override
    public void println() {
        super.println();
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(String s) {
        super.println(s);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(int x) {
        super.println(x);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(char x) {
        super.println(x);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(long x) {
        super.println(x);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(float x) {
        super.println(x);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(char[] x) {
        super.println(x);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(double x) {
        super.println(x);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(Object x) {
        super.println(x);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }

    @Override
    public void println(boolean x) {
        super.println(x);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "\n");
    }
}
