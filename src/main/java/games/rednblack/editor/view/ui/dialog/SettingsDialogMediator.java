package games.rednblack.editor.view.ui.dialog;

import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.menu.FileMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;

public class SettingsDialogMediator extends SimpleMediator<SettingsDialog> {

    private static final String TAG = SettingsDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public SettingsDialogMediator() {
        super(NAME, new SettingsDialog());
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                FileMenu.SETTINGS
        };
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case FileMenu.SETTINGS:
                viewComponent.show(uiStage);
                break;
        }
    }
}
