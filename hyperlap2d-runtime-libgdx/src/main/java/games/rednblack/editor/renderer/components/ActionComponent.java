package games.rednblack.editor.renderer.components;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.systems.action.data.ActionData;

/**
 * Created by ZeppLondon on 10/13/2015.
 */
public class ActionComponent implements BaseComponent {
    public Array<ActionData> dataArray = new Array<>(true, 0);

    @Override
    public void reset() {
        for (int i = 0; i < dataArray.size; i++) {
            ActionData data = dataArray.get(i);
            if (data.getPool() != null)
                data.getPool().free(data);
        }
        dataArray.clear();
    }
}
