package games.rednblack.editor.renderer.systems.action.data;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class RunnableData extends ActionData {
    public Runnable runnable;
    public boolean ran;

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void reset() {
        super.reset();

        runnable = null;
        ran = false;
    }
}
