package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/28/15.
 */
public class ScaleByData extends RelativeTemporalData {
    public float amountX, amountY;

    public void setAmountY(float amountY) {
        this.amountY = amountY;
    }

    public void setAmountX(float amountX) {
        this.amountX = amountX;
    }

    @Override
    public void reset() {
        super.reset();

        amountX = 0;
        amountY = 0;
    }
}
