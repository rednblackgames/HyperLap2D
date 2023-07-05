package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class NodeEditorDialogMediator extends Mediator<NodeEditorDialog> {
    private static final String TAG = NodeEditorDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public NodeEditorDialogMediator() {
        super(NAME, new NodeEditorDialog());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.OPEN_NODE_EDITOR);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case MsgAPI.OPEN_NODE_EDITOR:
                viewComponent.show(uiStage);
                viewComponent.loadData(notification.getBody());
                break;
        }
    }
}
