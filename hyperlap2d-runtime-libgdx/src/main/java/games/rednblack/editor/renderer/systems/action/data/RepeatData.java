package games.rednblack.editor.renderer.systems.action.data;

public class RepeatData extends DelegateData {
    public static final int FOREVER = -1;

    public int repeatCount, executedCount;

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    @Override
    public void restart() {
        super.restart();

        executedCount = 0;
    }

    @Override
    public void reset() {
        super.reset();

        repeatCount = 0;
        executedCount = 0;
    }
}
