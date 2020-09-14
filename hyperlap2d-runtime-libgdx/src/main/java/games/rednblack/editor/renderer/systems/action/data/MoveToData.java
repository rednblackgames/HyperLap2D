package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class MoveToData extends TemporalData {
    public float startX;
    public float startY;
    public float endX;
    public float endY;

    public void setEndX(float endX) {
        this.endX = endX;
    }

    public void setEndY(float endY) {
        this.endY = endY;
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
