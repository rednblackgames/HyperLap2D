package games.rednblack.editor.view.ui.box;

import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.utils.StandardWidgetsFactory;

public class UILivePreviewBox extends UIBaseBox {

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

		VisTextButton liveButton = StandardWidgetsFactory.createTextButton("Live Preview", "orange");
		add(liveButton).width(110);
	}
}
