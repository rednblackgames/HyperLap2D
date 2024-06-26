package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.utils.Align;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class MiniMapDialogMediator extends Mediator<MiniMapDialog> {
    private static final String TAG = MiniMapDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public MiniMapDialogMediator() {
        super(NAME, new MiniMapDialog());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.SHOW_MINI_MAP, MsgAPI.HIDE_MINI_MAP);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case MsgAPI.SHOW_MINI_MAP:
                viewComponent.setSize(uiStage.getWidth() * 0.6f, uiStage.getHeight() * 0.6f);
                viewComponent.setOrigin(Align.center);
                viewComponent.update();
                uiStage.addActor(viewComponent);
                viewComponent.setPosition((uiStage.getWidth() - viewComponent.getWidth()) / 2, (uiStage.getHeight() - viewComponent.getHeight()) / 2);
                break;
            case MsgAPI.HIDE_MINI_MAP:
                viewComponent.remove();
                break;
        }
    }
}
