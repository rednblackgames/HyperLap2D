package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class SaveProjectDialogMediator extends Mediator<SaveProjectDialog> {
    private static final String TAG = SaveProjectDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public SaveProjectDialogMediator() {
        super(NAME, new SaveProjectDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.CHECK_EDITS_ACTION
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
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
