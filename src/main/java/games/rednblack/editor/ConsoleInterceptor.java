package games.rednblack.editor;

import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.patterns.facade.Facade;

import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleInterceptor extends PrintStream {
    private final Facade facade;

    public ConsoleInterceptor(OutputStream out) {
        super(out, true);
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public void print(String s) {
        super.print(s);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, s);
    }

    @Override
    public void print(boolean b) {
        super.print(b);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, String.valueOf(b));
    }

    @Override
    public void print(int i) {
        super.print(i);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, String.valueOf(i));
    }

    @Override
    public void print(char c) {
        super.print(c);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, String.valueOf(c));
    }

    @Override
    public void print(long l) {
        super.print(l);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, String.valueOf(l));
    }

    @Override
    public void print(float f) {
        super.print(f);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, String.valueOf(f));
    }

    @Override
    public void print(char[] s) {
        super.print(s);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, String.valueOf(s));
    }

    @Override
    public void print(double d) {
        super.print(d);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, String.valueOf(d));
    }

    @Override
    public void print(Object obj) {
        super.print(obj);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, String.valueOf(obj));
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
