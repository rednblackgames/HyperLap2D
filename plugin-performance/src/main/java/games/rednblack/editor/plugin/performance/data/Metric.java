package games.rednblack.editor.plugin.performance.data;

/**
 * One measurement over a rolling window of frames, with the few statistics a panel actually reads.
 * <p>
 * Newest sample is 0 and the window counts backwards from there. The statistics are worked out on demand
 * and cached until the next sample, so a tile and a chart asking for the same average in the same frame
 * pay for it once.
 */
public class Metric {

    private final float[] values;
    private int head = -1;
    private int count;

    private boolean dirty = true;
    private float average, max, min, worstPercent;

    public Metric(int capacity) {
        values = new float[capacity];
    }

    public void add(float value) {
        head = head + 1 == values.length ? 0 : head + 1;
        values[head] = value;
        if (count < values.length) count++;
        dirty = true;
    }

    /** @param framesAgo 0 is the newest sample */
    public float get(int framesAgo) {
        if (framesAgo < 0 || framesAgo >= count) return 0f;
        int index = head - framesAgo;
        return values[index < 0 ? index + values.length : index];
    }

    public float last() {
        return get(0);
    }

    public int count() {
        return count;
    }

    public void clear() {
        head = -1;
        count = 0;
        dirty = true;
    }

    public float average() {
        recompute();
        return average;
    }

    public float max() {
        recompute();
        return max;
    }

    public float min() {
        recompute();
        return min;
    }

    /**
     * The mean of the worst hundredth of the window - the "1% low" of frame time charts. An average hides
     * the stutter that makes a game feel bad; this is the number that shows it.
     */
    public float worstPercent() {
        recompute();
        return worstPercent;
    }

    private void recompute() {
        if (!dirty) return;
        dirty = false;

        if (count == 0) {
            average = max = min = worstPercent = 0f;
            return;
        }

        //the worst hundredth, kept as we go: at this window size that is the top three
        float first = Float.NEGATIVE_INFINITY, second = Float.NEGATIVE_INFINITY, third = Float.NEGATIVE_INFINITY;
        float sum = 0f;
        float lowest = Float.POSITIVE_INFINITY;

        for (int i = 0; i < count; i++) {
            float value = get(i);
            sum += value;
            if (value < lowest) lowest = value;

            if (value > first) {
                third = second;
                second = first;
                first = value;
            } else if (value > second) {
                third = second;
                second = value;
            } else if (value > third) {
                third = value;
            }
        }

        average = sum / count;
        max = first;
        min = lowest;

        int worstCount = Math.max(1, Math.min(3, count));
        float worstSum = first;
        if (worstCount > 1) worstSum += second;
        if (worstCount > 2) worstSum += third;
        worstPercent = worstSum / worstCount;
    }
}
