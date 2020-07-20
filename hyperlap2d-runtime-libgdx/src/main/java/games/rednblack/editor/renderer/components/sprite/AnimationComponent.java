package games.rednblack.editor.renderer.components.sprite;

import java.util.HashMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;

public class AnimationComponent implements Component {
	public HashMap<String, Animation> animations = new  HashMap<>();
}
