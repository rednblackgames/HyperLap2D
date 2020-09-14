package games.rednblack.editor.renderer.systems.action.data;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class DelayData extends DelegateData {
    public float duration;
    public float passedTime;

    public void setDuration(float duration) {
        this.duration = duration;
    }

    @Override
    public void reset() {
        super.reset();
        duration = 0;
        passedTime = 0;
    }
}
