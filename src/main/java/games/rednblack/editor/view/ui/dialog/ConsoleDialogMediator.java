package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class ConsoleDialogMediator extends Mediator<ConsoleDialog> {

    private static final String TAG = ConsoleDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public ConsoleDialogMediator() {
        super(NAME, new ConsoleDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.OPEN_CONSOLE,
                MsgAPI.WRITE_TO_CONSOLE
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case MsgAPI.OPEN_CONSOLE:
                if (!viewComponent.hasParent())
                    viewComponent.show(uiStage);
                else
                    viewComponent.close();
                break;
            case MsgAPI.WRITE_TO_CONSOLE:
                try {
                    viewComponent.write(notification.getBody());
                } catch (Exception e) {
                    //Ignore any exception
                }
                break;
        }
    }
}
