package games.rednblack.editor.view.ui;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

/** The bottom bar has nothing to show before a project is open: it comes in once one is. */
public class UIBottomMenuBarMediator extends Mediator<UIBottomMenuBar> {
    private static final String TAG = UIBottomMenuBarMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIBottomMenuBarMediator() {
        super(NAME, new UIBottomMenuBar());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(ProjectManager.PROJECT_OPENED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        if (ProjectManager.PROJECT_OPENED.equals(notification.getName())) {
            viewComponent.enter();
        }
    }
}
