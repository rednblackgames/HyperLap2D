package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class NodeEditorDialogMediator extends Mediator<NodeEditorDialog> {
    private static final String TAG = NodeEditorDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public NodeEditorDialogMediator() {
        super(NAME, new NodeEditorDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.OPEN_NODE_EDITOR
        };
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
