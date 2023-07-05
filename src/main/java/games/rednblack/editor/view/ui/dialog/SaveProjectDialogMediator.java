package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class SaveProjectDialogMediator extends Mediator<SaveProjectDialog> {
    private static final String TAG = SaveProjectDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public SaveProjectDialogMediator() {
        super(NAME, new SaveProjectDialog());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.CHECK_EDITS_ACTION);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        Facade facade = Facade.getInstance();
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);

        switch (notification.getName()) {
            case MsgAPI.CHECK_EDITS_ACTION:
                if (HyperLap2DApp.getInstance().hyperlap2D.hasUnsavedStuff() && projectManager.currentProjectVO != null) {
                    if (viewComponent.getStage() == null) {
                        viewComponent.updateDialog(projectManager.currentProjectVO.projectName, notification.getBody());
                        viewComponent.show(uiStage);
                    }
                } else {
                    Runnable action = notification.getBody();
                    action.run();
                }
                break;
        }
    }
}
