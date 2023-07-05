package games.rednblack.editor.view.ui;

import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class UIWindowActionMediator extends Mediator<UIWindowAction> {
    private static final String TAG = UIWindowActionMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIWindowActionMediator() {
        super(NAME, new UIWindowAction());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.WINDOW_MAXIMIZED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case MsgAPI.WINDOW_MAXIMIZED:
                viewComponent.setMaximized(notification.getBody());
                break;
        }
    }
}
