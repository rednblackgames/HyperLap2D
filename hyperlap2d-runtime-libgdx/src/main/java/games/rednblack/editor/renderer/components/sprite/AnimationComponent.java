package games.rednblack.editor.renderer.components.sprite;

import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Animation;
import games.rednblack.editor.renderer.components.BaseComponent;

//TODO This is probably useless
public class AnimationComponent implements BaseComponent {
	public HashMap<String, Animation> animations = new  HashMap<>();

	@Override
	public void reset() {
		animations.clear();
	}
}
