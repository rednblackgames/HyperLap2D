package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/16/2015.
 */
public class RotateToData extends TemporalData {
    public float start;
    public float end;

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
