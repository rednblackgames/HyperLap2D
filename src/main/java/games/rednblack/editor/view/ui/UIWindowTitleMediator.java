package games.rednblack.editor.view.ui;


import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class UIWindowTitleMediator  extends Mediator<UIWindowTitle> {

    private static final String TAG = UIWindowTitleMediator.class.getCanonicalName();
    public static final String NAME = TAG;


    public UIWindowTitleMediator() {
        super(NAME, new UIWindowTitle());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.WINDOW_TITLE_CHANGED);
    }

    @Override
    public void handleNotification(INotification notification) {
        if (MsgAPI.WINDOW_TITLE_CHANGED.equals(notification.getName())) {
            viewComponent.setTitle(notification.getBody());
        }
    }
}