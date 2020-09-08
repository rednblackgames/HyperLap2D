package games.rednblack.editor.view.ui;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class UIWindowActionMediator extends Mediator<UIWindowAction> {
    private static final String TAG = UIWindowActionMediator.class.getCanonicalName();
    public static final String NAME = TAG;


    public UIWindowActionMediator() {
        super(NAME, new UIWindowAction());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.WINDOW_MAXIMIZED
        };
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
