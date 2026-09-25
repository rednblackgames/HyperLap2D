package games.rednblack.editor.plugin.performance.data;

/**
 * Numbers into a builder without making a string on the way. A label that is rewritten sixty times a second
 * is exactly the kind of thing a profiler should not be doing to the heap it is reporting on.
 */
public final class Fmt {

    private static final int[] POW10 = {1, 10, 100, 1000, 10000};

    private Fmt() {
    }

    public static StringBuilder clear(StringBuilder builder) {
        builder.setLength(0);
        return builder;
    }

    public static void number(StringBuilder builder, float value, int decimals) {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            builder.append('-');
            return;
        }

        if (value < 0f) {
            builder.append('-');
            value = -value;
        }

        int scale = POW10[Math.max(0, Math.min(decimals, POW10.length - 1))];
        long scaled = (long) (value * scale + 0.5f);

        builder.append(scaled / scale);
        if (decimals <= 0) return;

        builder.append('.');
        long fraction = scaled % scale;
        for (int i = decimals - 1; i > 0; i--) {
            if (fraction < POW10[i]) builder.append('0');
        }
        builder.append(fraction);
    }

    /** Thousands separated with a thin space, so six-figure vertex counts stay readable. */
    public static void grouped(StringBuilder builder, long value) {
        if (value < 0) {
            builder.append('-');
            value = -value;
        }

        if (value >= 1000) {
            grouped(builder, value / 1000);
            builder.append(' ');
            long rest = value % 1000;
            if (rest < 100) builder.append('0');
            if (rest < 10) builder.append('0');
            builder.append(rest);
        } else {
            builder.append(value);
        }
    }
}
