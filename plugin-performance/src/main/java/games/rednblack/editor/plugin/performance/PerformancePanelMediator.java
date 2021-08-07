package games.rednblack.editor.plugin.performance;

import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

public class PerformancePanelMediator extends Mediator<PerformancePanel> {
    private static final String TAG = PerformancePanelMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private PerformancePlugin performancePlugin;

    public PerformancePanelMediator(PerformancePlugin performancePlugin) {
        super(NAME, new PerformancePanel());
        this.performancePlugin = performancePlugin;

        viewComponent.initLockView();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.RENDER,
                MsgAPI.SCENE_LOADED,
                PerformancePlugin.PANEL_OPEN
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                viewComponent.initView();
                com.artemis.World engine = performancePlugin.getAPI().getEngine();
                viewComponent.setEngine(engine);
                break;
            case PerformancePlugin.PANEL_OPEN:
                viewComponent.show(performancePlugin.getAPI().getUIStage());
                break;
            case MsgAPI.RENDER:
                viewComponent.render();
                break;
        }
    }
}
