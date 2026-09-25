package games.rednblack.editor.plugin.performance.advisor;

/** One thing worth saying about the numbers, and how loudly to say it. */
public class Hint {

    public static final int INFO = 0;
    public static final int WARN = 1;
    public static final int BAD = 2;

    public final StringBuilder text = new StringBuilder(96);
    public int severity;

    public Hint set(int severity) {
        this.severity = severity;
        text.setLength(0);
        return this;
    }
}
