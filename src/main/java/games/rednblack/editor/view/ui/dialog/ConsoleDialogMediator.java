package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class ConsoleDialogMediator extends Mediator<ConsoleDialog> {

    private static final String TAG = ConsoleDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public ConsoleDialogMediator() {
        super(NAME, new ConsoleDialog());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.OPEN_CONSOLE,
                MsgAPI.WRITE_TO_CONSOLE);
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
