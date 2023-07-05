package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class LoadingBarDialogMediator extends Mediator<LoadingBarDialog> {
    private static final String TAG = LoadingBarDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public LoadingBarDialogMediator() {
        super(NAME, new LoadingBarDialog());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.SHOW_LOADING_DIALOG,
                MsgAPI.HIDE_LOADING_DIALOG);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case MsgAPI.SHOW_LOADING_DIALOG:
                viewComponent.show(uiStage);
                break;
            case MsgAPI.HIDE_LOADING_DIALOG:
                viewComponent.close();
                break;
        }
    }
}
