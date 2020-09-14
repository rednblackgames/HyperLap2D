package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class RelativeTemporalData extends TemporalData {
    public float lastPercent;

    @Override
    public void reset() {
        super.reset();

        lastPercent = 0;
    }
}
