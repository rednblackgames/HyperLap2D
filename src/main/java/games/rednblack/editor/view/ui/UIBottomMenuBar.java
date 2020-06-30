package games.rednblack.editor.view.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.box.*;

public class UIBottomMenuBar extends VisTable {
	private final HyperLap2DFacade facade;

	public UIBottomMenuBar() {
		Skin skin = VisUI.getSkin();
		facade = HyperLap2DFacade.getInstance();

		setBackground(skin.getDrawable("sub-menu-bg"));

		//grid
		UIGridBoxMediator uiGridBoxMediator = facade.retrieveMediator(UIGridBoxMediator.NAME);
		UIGridBox uiGridBox = uiGridBoxMediator.getViewComponent();
		add(uiGridBox);

		//grid
		UIZoomBoxMediator uiZoomBoxMediator = facade.retrieveMediator(UIZoomBoxMediator.NAME);
		UIZoomBox uiZoomBox = uiZoomBoxMediator.getViewComponent();
		add(uiZoomBox);

		//resolution box
		UIResolutionBoxMediator uiResolutionBoxMediator = facade.retrieveMediator(UIResolutionBoxMediator.NAME);
		UIResolutionBox uiResolutionBox = uiResolutionBoxMediator.getViewComponent();
		add(uiResolutionBox);

		//live preview
		UILivePreviewBoxMediator uiLivePreviewBoxMediator = facade.retrieveMediator(UILivePreviewBoxMediator.NAME);
		UILivePreviewBox uiLivePreviewBox = uiLivePreviewBoxMediator.getViewComponent();
		add(uiLivePreviewBox);
	}
}
