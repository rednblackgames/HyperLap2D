package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;

/**
 * Created by ZeppLondon on 10/28/15.
 */
public class ColorData extends TemporalData {
    public float startR, startG, startB, startA;
    public Color endColor = new Color();

    public void setEndColor(Color endColor) {
        this.endColor.set(endColor);
    }

    public void setEndColor(float r, float g, float b, float a) {
        this.endColor.set(r, g, b, a);
    }

    @Override
    public void reset() {
        super.reset();
        endColor.set(0,0,0,0);
        startA = 0;
        startR = 0;
        startG = 0;
        startB = 0;
    }
}
