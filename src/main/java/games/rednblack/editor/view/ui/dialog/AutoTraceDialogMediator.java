package games.rednblack.editor.view.ui.dialog;

import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.menu.HelpMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;

public class AutoTraceDialogMediator extends SimpleMediator<AutoTraceDialog> {

    private static final String TAG = AutoTraceDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public AutoTraceDialogMediator() {
        super(NAME, new AutoTraceDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{

        };
    }

    @Override
    public void handleNotification(Notification notification) {
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
