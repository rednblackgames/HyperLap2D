package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.utils.Null;
import com.badlogic.gdx.utils.Pool;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class ActionData implements Pool.Poolable {
    public String logicClassName;
    public boolean detached;

    private @Null
    Pool pool;

    @Override
    public void reset() {
        logicClassName = null;
        detached = false;
        pool = null;
    }

    public void setPool(@Null Pool pool) {
        this.pool = pool;
    }

    public @Null
    Pool getPool() {
        return pool;
    }
}
