package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Gdx;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;

public class SaveDocumentDialogMediator extends SimpleMediator<SaveDocumentDialog> {
    private static final String TAG = SaveDocumentDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public SaveDocumentDialogMediator() {
        super(NAME, new SaveDocumentDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.APP_EXIT
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);

        switch (notification.getName()) {
            case MsgAPI.APP_EXIT:
                if (HyperLap2DApp.getInstance().hyperlap2D.hasUnsavedStuff() && projectManager.currentProjectVO != null) {
                    viewComponent.updateMessage(projectManager.currentProjectVO.projectName);
                    viewComponent.show(uiStage);
                } else {
                    Gdx.app.exit();
                }
                break;
        }
    }
}
