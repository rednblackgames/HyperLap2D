package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class TemporalData extends ActionData {
    public float duration;
    public float passedTime;
    public boolean began;
    public boolean complete;
    public Interpolation interpolation;

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    @Override
    public void restart() {
        super.restart();

        passedTime = 0;
        complete = false;
    }

    @Override
    public void reset() {
        super.reset();

        began = false;
        complete = false;
        passedTime = 0;
        interpolation = null;
        duration = 0;
    }
}
