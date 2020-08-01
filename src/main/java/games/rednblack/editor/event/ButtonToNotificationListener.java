package games.rednblack.editor.event;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import games.rednblack.editor.HyperLap2DFacade;

/**
 * Created by azakhary on 7/2/2015.
 */
public class ButtonToNotificationListener extends ClickListener{

    private String notificationName;
    private Object payload;

    public ButtonToNotificationListener(String notificationName) {
        this.notificationName = notificationName;
    }

    public ButtonToNotificationListener(String notificationName, Object payload) {
        this.notificationName = notificationName;
        this.payload = payload;
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
        super.touchUp(event, x, y, pointer, button);
        HyperLap2DFacade.getInstance().sendNotification(notificationName, payload);
    }
}
