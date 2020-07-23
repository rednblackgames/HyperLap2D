package games.rednblack.editor.renderer.components.sprite;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Animation;
import games.rednblack.editor.renderer.components.BaseComponent;
import games.rednblack.editor.renderer.data.FrameRange;

public class SpriteAnimationComponent implements BaseComponent {
	public String animationName = "";
	public int fps = 24;
	public HashMap<String, FrameRange> frameRangeMap = new HashMap<>();
    public String currentAnimation;
    public Animation.PlayMode playMode = Animation.PlayMode.LOOP;

	@Override
	public void reset() {
		animationName = "";
		fps = 24;
		frameRangeMap.clear();
		currentAnimation = null;
		playMode = Animation.PlayMode.LOOP;
	}
}
