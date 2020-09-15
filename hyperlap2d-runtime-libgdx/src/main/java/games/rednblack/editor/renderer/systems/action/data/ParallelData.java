package games.rednblack.editor.renderer.systems.action.data;

/**
 * Created by ZeppLondon on 10/23/15.
 */
public class ParallelData extends ActionData {
    public ActionData[] actionsData;
    public boolean complete;

    public void setActionsData(ActionData[] actionsData) {
        this.actionsData = actionsData;
    }

    @Override
    public void restart() {
        super.restart();

        for (ActionData data : actionsData) {
            data.restart();
        }
        complete = false;
    }

    @Override
    public void reset() {
        super.reset();

        for (ActionData data : actionsData) {
            if (data.getPool() != null)
                data.getPool().free(data);
        }

        actionsData = null;
        complete = false;
    }
}
