package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.utils.StandardWidgetsFactory;

public class UILivePreviewBox extends UIBaseBox {

	private static final String prefix = "games.rednblack.editor.view.ui.box.UILivePreviewBox";
	public static final String LIVE_PREVIEW_CLICKED = prefix + ".LIVE_PREVIEW_CLICKED";

	public UILivePreviewBox() {
		init();
		setVisible(false);
	}

	@Override
	public void update() {
		setVisible(true);
	}

	private void init() {
		addSeparator(true).padRight(13).padLeft(13);

		VisTextButton liveButton = StandardWidgetsFactory.createTextButton("Live Preview", "red");
		add(liveButton).width(110);
		liveButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				super.clicked(event, x, y);
				facade.sendNotification(LIVE_PREVIEW_CLICKED);
			}
		});
	}
}
