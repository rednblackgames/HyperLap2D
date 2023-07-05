package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.view.menu.HelpMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class AboutDialogMediator extends Mediator<AboutDialog> {
    private static final String TAG = AboutDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public AboutDialogMediator() {
        super(NAME, new AboutDialog());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(HelpMenu.ABOUT_DIALOG_OPEN);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case HelpMenu.ABOUT_DIALOG_OPEN:
                viewComponent.show(uiStage);
                break;
        }
    }
}
