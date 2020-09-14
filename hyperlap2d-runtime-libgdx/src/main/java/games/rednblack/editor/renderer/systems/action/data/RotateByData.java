package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/16/2015.
 */
public class RotateByData extends RelativeTemporalData {
    public float amount;

    public void setAmount(float amount) {
        this.amount = amount;
    }

    @Override
    public void reset() {
        super.reset();

        amount = 0;
    }
}
