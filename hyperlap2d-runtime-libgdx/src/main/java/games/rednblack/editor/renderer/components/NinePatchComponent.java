package games.rednblack.editor.renderer.components;

import com.badlogic.gdx.graphics.g2d.NinePatch;

public class NinePatchComponent implements BaseComponent {
	public String textureRegionName;
	public NinePatch ninePatch;

	@Override
	public void reset() {
		textureRegionName = null;
		ninePatch = null;
	}
}
