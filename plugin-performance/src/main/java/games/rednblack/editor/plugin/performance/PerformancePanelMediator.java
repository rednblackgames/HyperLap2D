package games.rednblack.editor.plugin.performance;

import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

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
    public void listNotificationInterests(Interests interests) {
        interests.add(MsgAPI.RENDER,
                MsgAPI.SCENE_LOADED,
                PerformancePlugin.PANEL_OPEN);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                viewComponent.initView();
                Engine engine = performancePlugin.getAPI().getEngine();
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
