package games.rednblack.editor.view.ui.box.bottom;

import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.tools.PanTool;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

public class UIPanBoxMediator extends Mediator<UIPanBox> {
    private static final String TAG = UIPanBoxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIPanBoxMediator() {
        super(NAME, new UIPanBox());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(ProjectManager.PROJECT_OPENED, PanTool.SCENE_PANNED, UIPanBox.PAN_VALUE_CHANGED);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        switch (notification.getName()) {
            case ProjectManager.PROJECT_OPENED:
                viewComponent.update();
                viewComponent.setCameraCoordinates(sandbox.getCameraPosition().x, sandbox.getCameraPosition().y);
                break;
            case PanTool.SCENE_PANNED:
                viewComponent.setCameraCoordinates(sandbox.getCameraPosition().x, sandbox.getCameraPosition().y);
                break;
            case UIPanBox.PAN_VALUE_CHANGED:
                sandbox.panSceneTo(viewComponent.getCameraX(), viewComponent.getCameraY());
                break;
        }
    }
}
