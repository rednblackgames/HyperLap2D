package games.rednblack.editor.renderer.components.spriter;

import com.brashmonkey.spriter.gdx.Drawer;
import games.rednblack.editor.renderer.components.BaseComponent;

public class SpriterDrawerComponent implements BaseComponent {
	public Drawer drawer;

	@Override
	public void reset() {
		drawer = null;
	}
}
