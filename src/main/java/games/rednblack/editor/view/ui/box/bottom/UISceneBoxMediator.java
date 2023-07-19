package games.rednblack.editor.view.ui.box.bottom;

import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.util.dialog.InputDialogListener;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SceneDataManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class UISceneBoxMediator extends Mediator<UISceneBox> {
    private static final String TAG = UISceneBoxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UISceneBoxMediator() {
        super(NAME, new UISceneBox());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(ProjectManager.PROJECT_OPENED,
                MsgAPI.SCENE_LOADED,
                UISceneBox.CHANGE_SCENE_BTN_CLICKED,
                UISceneBox.CREATE_NEW_SCENE_BTN_CLICKED);
        interests.add(UISceneBox.DELETE_CURRENT_SCENE_BTN_CLICKED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
		Sandbox sandbox = Sandbox.getInstance();

        switch (notification.getName()) {
            case ProjectManager.PROJECT_OPENED:
            case MsgAPI.SCENE_LOADED:
                viewComponent.update();
                break;
            case UISceneBox.CHANGE_SCENE_BTN_CLICKED:
                String newScene = notification.getBody();
                facade.sendNotification(MsgAPI.CHECK_EDITS_ACTION, (Runnable) () -> sandbox.loadScene(newScene));
                break;
            case UISceneBox.CREATE_NEW_SCENE_BTN_CLICKED:
                Dialogs.showInputDialog(sandbox.getUIStage(), "Create New Scene", "Scene Name : ", false, new StringNameValidator(), new InputDialogListener() {
                    @Override
                    public void finished(String input) {
                        if (input == null || input.equals("")) {
                            viewComponent.setCurrentScene();
                            return;
                        }
                        SceneDataManager sceneDataManager = facade.retrieveProxy(SceneDataManager.NAME);
                        sceneDataManager.createNewScene(input);
                        facade.sendNotification(MsgAPI.CHECK_EDITS_ACTION, (Runnable) () -> sandbox.loadScene(input));
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
                        }).padBottom(20).pack();
                break;
            default:
                break;
        }
    }
}
