package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/28/15.
 */
public class SizeByData extends RelativeTemporalData {
    public float amountWidth, amountHeight;

    public void setAmountHeight(float amountHeight) {
        this.amountHeight = amountHeight;
    }

    public void setAmountWidth(float amountWidth) {
        this.amountWidth = amountWidth;
    }

    @Override
    public void reset() {
        super.reset();

        amountHeight = 0;
        amountWidth = 0;
    }
}
