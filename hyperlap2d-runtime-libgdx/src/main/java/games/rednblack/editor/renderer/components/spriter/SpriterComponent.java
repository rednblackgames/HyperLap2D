package games.rednblack.editor.renderer.components.spriter;

import java.util.ArrayList;

import com.brashmonkey.spriter.Data;
import com.brashmonkey.spriter.Player;
import games.rednblack.editor.renderer.components.BaseComponent;

public class SpriterComponent implements BaseComponent {
	public Player player;
	public Data data;
	public ArrayList<String> animations = new ArrayList<>();
	public ArrayList<String> entities = new ArrayList<>();
	public int currentEntityIndex = 0;
	public int currentAnimationIndex = 0;
	
	public int entity;
	public int animation;
	public String animationName = "";
	public float scale = 1f;

	@Override
	public void reset() {
		player = null;
		data = null;

		animations.clear();
		entities.clear();

		currentEntityIndex = 0;
		currentAnimationIndex = 0;

		entity = 0;
		animation = 0;
		animationName = "";
		scale = 1f;
	}
}
