package games.rednblack.editor.view.ui.box;

import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.h2d.common.MsgAPI;

public class UILivePreviewBoxMediator extends SimpleMediator<UILivePreviewBox> {
	private static final String TAG = UILivePreviewBoxMediator.class.getCanonicalName();
	public static final String NAME = TAG;

	public UILivePreviewBoxMediator() {
		super(NAME, new UILivePreviewBox());
	}

	@Override
	public String[] listNotificationInterests() {
		return new String[]{
				ProjectManager.PROJECT_OPENED,
				MsgAPI.SCENE_LOADED
		};
	}

	@Override
	public void handleNotification(Notification notification) {
		super.handleNotification(notification);

		switch (notification.getName()) {
			case ProjectManager.PROJECT_OPENED:
				viewComponent.update();
				break;
			case MsgAPI.SCENE_LOADED:
				//TODO
				break;
			default:
				break;
		}
	}
}
