package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class MoveByData extends RelativeTemporalData {
    public float amountX;
    public float amountY;

    public void setAmountX(float amountX) {
        this.amountX = amountX;
    }

    public void setAmountY(float amountY) {
        this.amountY = amountY;
    }

    @Override
    public void reset() {
        super.reset();

        amountX = 0;
        amountY = 0;
    }
}
