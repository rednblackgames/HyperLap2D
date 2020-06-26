package games.rednblack.editor.renderer.components.sprite;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.data.FrameRange;

public class SpriteAnimationComponent implements Component {
	public String animationName = "";
	public int fps = 24;
	public HashMap<String, FrameRange> frameRangeMap = new HashMap<String, FrameRange>();
    public String currentAnimation;
    public Animation.PlayMode playMode = Animation.PlayMode.LOOP;
	
}
