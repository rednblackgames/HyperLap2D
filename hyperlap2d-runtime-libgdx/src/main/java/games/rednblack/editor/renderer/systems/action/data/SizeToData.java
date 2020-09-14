package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/28/15.
 */
public class SizeToData extends TemporalData{
    public float startWidth, startHeight;
    public float endWidth, endHeight;

    public void setEndHeight(float endHeight) {
        this.endHeight = endHeight;
    }

    public void setEndWidth(float endWidth) {
        this.endWidth = endWidth;
    }

    @Override
    public void reset() {
        super.reset();

        startHeight = 0;
        startWidth = 0;
        endHeight = 0;
        endWidth = 0;
    }
}
