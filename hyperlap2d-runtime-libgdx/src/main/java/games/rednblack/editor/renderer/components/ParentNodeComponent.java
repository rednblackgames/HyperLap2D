package games.rednblack.editor.renderer.components;

import com.badlogic.ashley.core.Entity;

public class ParentNodeComponent implements BaseComponent {
	public Entity parentEntity = null;

	@Override
	public void reset() {
		parentEntity = null;
	}
}
