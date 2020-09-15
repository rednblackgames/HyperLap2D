package games.rednblack.editor.renderer.systems.action.data;

public class DelegateData extends ActionData {
    public ActionData delegatedData;

    public void setDelegatedAction(ActionData actionData) {
        this.delegatedData = actionData;
    }

    @Override
    public void restart() {
        super.restart();

        if (delegatedData != null)
            delegatedData.restart();
    }

    @Override
    public void reset() {
        super.reset();

        if (delegatedData != null && delegatedData.getPool() != null)
            delegatedData.getPool().free(delegatedData);

        delegatedData = null;
    }
}
