package games.rednblack.editor.plugin.performance;

import com.badlogic.ashley.core.Engine;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;

public class PerformancePanelMediator extends SimpleMediator<PerformancePanel> {
    private static final String TAG = PerformancePanelMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public static final String SCENE_LOADED = "games.rednblack.editor.proxy.SceneDataManager.SCENE_LOADED";
    public static final String NEW_ITEM_ADDED = "games.rednblack.editor.factory.ItemFactory.NEW_ITEM_ADDED";
    public static final String ACTION_DELETE = "games.rednblack.editor.controller.commands.DeleteItemsCommandDONE";

    private PerformancePlugin performancePlugin;

    public PerformancePanelMediator(PerformancePlugin performancePlugin) {
        super(NAME, new PerformancePanel());
        this.performancePlugin = performancePlugin;

        viewComponent.initLockView();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                SCENE_LOADED,
                NEW_ITEM_ADDED,
                ACTION_DELETE,
                PerformancePlugin.PANEL_OPEN
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case SCENE_LOADED:
                viewComponent.initView();
                Engine engine = performancePlugin.getAPI().getEngine();
                viewComponent.setEngine(engine);
                break;
            case PerformancePlugin.PANEL_OPEN:
                viewComponent.show(performancePlugin.getAPI().getUIStage());
                break;
        }
    }
}
