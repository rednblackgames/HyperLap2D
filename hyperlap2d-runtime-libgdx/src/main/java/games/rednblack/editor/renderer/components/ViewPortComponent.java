package games.rednblack.editor.renderer.components;

import com.badlogic.gdx.utils.viewport.Viewport;

public class ViewPortComponent implements BaseComponent {
	public Viewport viewPort;

	@Override
	public void reset() {
		viewPort = null;
	}
}
