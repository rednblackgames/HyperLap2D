package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.Gdx;

/**
 * Created by ZeppLondon on 10/29/15.
 */
public class AlphaData extends TemporalData {
    public float start, end;

    public void setEnd(float end) {
        this.end = end;
    }

    @Override
    public void reset() {
        super.reset();
        start = 0;
        end = 0;
    }
}
