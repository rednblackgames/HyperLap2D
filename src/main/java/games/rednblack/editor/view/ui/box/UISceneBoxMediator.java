package games.rednblack.editor.view.ui.box;

import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.MsgAPI;

public class UISceneBoxMediator extends SimpleMediator<UISceneBox> {
    private static final String TAG = UISceneBoxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UISceneBoxMediator() {
        super(NAME, new UISceneBox());
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ProjectManager.PROJECT_OPENED,
                MsgAPI.SCENE_LOADED,
                UISceneBox.CHANGE_SCENE_BTN_CLICKED,
                UISceneBox.CREATE_NEW_SCENE_BTN_CLICKED,
                UISceneBox.DELETE_CURRENT_SCENE_BTN_CLICKED
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
		Sandbox sandbox = Sandbox.getInstance();

        switch (notification.getName()) {
            case ProjectManager.PROJECT_OPENED:
            case MsgAPI.SCENE_LOADED:
                viewComponent.update();
                break;
            case UISceneBox.CHANGE_SCENE_BTN_CLICKED:
                sandbox.loadScene(notification.getBody());
                break;
            case UISceneBox.CREATE_NEW_SCENE_BTN_CLICKED:
                Dialogs.showInputDialog(sandbox.getUIStage(), "Create New Scene", "Scene Name : ", new StringNameValidator(), new InputDialogListener() {
                    @Override
                    public void finished(String input) {
                        if (input == null || input.equals("")) {
                            viewComponent.setCurrentScene();
                            return;
                        }
                        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
                        sceneDataManager.createNewScene(input);
                        sandbox.loadScene(input);
                    }

                    @Override
                    public void canceled() {
                        viewComponent.setCurrentScene();
                    }
                });
                break;
            case UISceneBox.DELETE_CURRENT_SCENE_BTN_CLICKED:
                Dialogs.showConfirmDialog(sandbox.getUIStage(),
                        "Delete Scene", "Do you really want to delete '" + notification.getBody() + "' scene?",
                        new String[]{"Cancel", "Delete"}, new Integer[]{0, 1}, result -> {
                            if (result == 1) {
                                SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
                                sceneDataManager.deleteCurrentScene();
                                sandbox.loadScene("MainScene");
                            }
                        });
                break;
            default:
                break;
        }
    }
}
