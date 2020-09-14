package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/28/15.
 */
public class ScaleToData extends TemporalData {
    public float startX, startY;
    public float endX, endY;

    public void setEndY(float endY) {
        this.endY = endY;
    }

    public void setEndX(float endX) {
        this.endX = endX;
    }

    @Override
    public void reset() {
        super.reset();

        startX = 0;
        startY = 0;
        endX = 0;
        endY = 0;
    }
}
