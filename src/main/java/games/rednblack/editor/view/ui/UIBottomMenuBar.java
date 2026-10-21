package games.rednblack.editor.view.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.view.ui.box.bottom.*;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

public class UIBottomMenuBar extends VisTable {
	/**
	 * A rounded bar floating over the bottom of the canvas, centred and as wide as what it holds: when the window
	 * is narrower than that it shrinks, down to {@link #MIN_WIDTH}, and its content scrolls.
	 */
	public static final int HEIGHT = 38;
	public static final int MARGIN = 8;
	public static final int MIN_WIDTH = 320;
	private static final float ENTER_DURATION = 0.35f;

	/** How far into its entrance the bar is: done until a project opens and it comes in. */
	private float enterTime = ENTER_DURATION;

	private final Facade facade;

	public UIBottomMenuBar() {
		Skin skin = VisUI.getSkin();
		facade = Facade.getInstance();

		setBackground(skin.getDrawable("status-bar-bg"));

		VisTable mainGroup = new VisTable();
		VisScrollPane scrollPane = StandardWidgetsFactory.createScrollPane(mainGroup);
		add(scrollPane).fill().padLeft(10).padRight(10);

		//scene
		UISceneBoxMediator uiSceneBoxMediator = facade.retrieveMediator(UISceneBoxMediator.NAME);
		UISceneBox uiSceneBox = uiSceneBoxMediator.getViewComponent();
		mainGroup.add(uiSceneBox);

		//grid
		UIGridBoxMediator uiGridBoxMediator = facade.retrieveMediator(UIGridBoxMediator.NAME);
		UIGridBox uiGridBox = uiGridBoxMediator.getViewComponent();
		mainGroup.add(uiGridBox);

		//zoom
		UIZoomBoxMediator uiZoomBoxMediator = facade.retrieveMediator(UIZoomBoxMediator.NAME);
		UIZoomBox uiZoomBox = uiZoomBoxMediator.getViewComponent();
		mainGroup.add(uiZoomBox);

		//pan
		UIPanBoxMediator uiPanBoxMediator = facade.retrieveMediator(UIPanBoxMediator.NAME);
		UIPanBox uiPanBox = uiPanBoxMediator.getViewComponent();
		mainGroup.add(uiPanBox);

		//resolution box
		UIResolutionBoxMediator uiResolutionBoxMediator = facade.retrieveMediator(UIResolutionBoxMediator.NAME);
		UIResolutionBox uiResolutionBox = uiResolutionBoxMediator.getViewComponent();
		mainGroup.add(uiResolutionBox);

		//live preview
		UILivePreviewBoxMediator uiLivePreviewBoxMediator = facade.retrieveMediator(UILivePreviewBoxMediator.NAME);
		UILivePreviewBox uiLivePreviewBox = uiLivePreviewBoxMediator.getViewComponent();
		mainGroup.add(uiLivePreviewBox);

		// hidden until a project is open
		setVisible(false);
	}

	/** Slides up from below the window and fades in; only the first time, when the first project opens. */
	public void enter() {
		if (isVisible()) return;
		setVisible(true);
		enterTime = 0f;
		getColor().a = 0f;
		addAction(Actions.fadeIn(ENTER_DURATION * 0.7f));
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if (enterTime < ENTER_DURATION) enterTime = Math.min(ENTER_DURATION, enterTime + delta);
	}

	/**
	 * The slide is an offset added while drawing, on top of wherever the layout puts the bar: a move action would
	 * be undone, or added to, by a layout landing in the middle of it.
	 */
	@Override
	public void draw(Batch batch, float parentAlpha) {
		float offset = (HEIGHT + MARGIN) * (1f - Interpolation.pow3Out.apply(enterTime / ENTER_DURATION));
		if (offset == 0f) {
			super.draw(batch, parentAlpha);
			return;
		}
		moveBy(0f, -offset);
		super.draw(batch, parentAlpha);
		moveBy(0f, offset);
	}
}
