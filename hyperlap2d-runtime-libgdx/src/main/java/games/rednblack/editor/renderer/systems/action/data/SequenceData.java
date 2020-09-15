package games.rednblack.editor.renderer.systems.action.data;

/**
 * Created by ZeppLondon on 10/23/15.
 */
public class SequenceData extends ParallelData {
    public int index;

    @Override
    public void restart() {
        super.restart();

        index = 0;
    }

    @Override
    public void reset() {
        super.reset();

        index = 0;
    }
}
