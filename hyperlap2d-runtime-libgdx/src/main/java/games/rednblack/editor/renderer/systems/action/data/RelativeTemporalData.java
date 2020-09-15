package games.rednblack.editor.renderer.systems.action.data;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class RelativeTemporalData extends TemporalData {
    public float lastPercent;

    @Override
    public void restart() {
        super.restart();

        lastPercent = 0;
        began = false;
    }

    @Override
    public void reset() {
        super.reset();

        lastPercent = 0;
    }
}
