package games.rednblack.editor.view.ui.box.resourcespanel;

import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class UIFilterMenuMediator extends Mediator<UIFilterMenu> {

    private static final String TAG = UIFilterMenuMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIFilterMenuMediator() {
        super(NAME, new UIFilterMenu());
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                UIFilterMenu.SHOW_FILTER_MENU,
                MsgAPI.ADD_RESOURCES_BOX_FILTER
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        switch (notification.getName()) {
            case UIFilterMenu.SHOW_FILTER_MENU:
                UIStage uiStage = Sandbox.getInstance().getUIStage();
                Object[] payload = notification.getBody();
                viewComponent.showMenu(uiStage, (float) payload[0], (float) payload[1]);
                break;
            case MsgAPI.ADD_RESOURCES_BOX_FILTER:
                viewComponent.addFilter(notification.getBody());
                break;
        }
    }
}
