package games.rednblack.editor.view.ui.dialog;
import games.rednblack.editor.proxy.PluginUIBridge;

import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class LoadingBarDialogMediator extends Mediator<LoadingBarDialog> {
    private static final String TAG = LoadingBarDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private boolean shown;

    public LoadingBarDialogMediator() {
        super(NAME, new LoadingBarDialog());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.SHOW_LOADING_DIALOG,
                MsgAPI.HIDE_LOADING_DIALOG,
                LoadingBarDialog.SET_MESSAGE,
                LoadingBarDialog.SET_PROGRESS);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = PluginUIBridge.get().getSandbox();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case MsgAPI.SHOW_LOADING_DIALOG:
                if (shown) break;
                shown = true;
                // A job asking for the dialog right after the previous one closed it catches the
                // fade out mid flight: stop it and put the dialog back to full opacity.
                viewComponent.clearActions();
                viewComponent.getColor().a = 1;
                viewComponent.reset();
                if (viewComponent.getStage() == null)
                    viewComponent.show(uiStage);
                break;
            case MsgAPI.HIDE_LOADING_DIALOG:
                shown = false;
                viewComponent.close();
                break;
            case LoadingBarDialog.SET_MESSAGE:
                viewComponent.setMessage(notification.getBody());
                break;
            case LoadingBarDialog.SET_PROGRESS:
                viewComponent.setProgress(notification.getBody());
                break;
        }
    }
}
