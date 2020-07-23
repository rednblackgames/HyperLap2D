package games.rednblack.editor.renderer.components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.SnapshotArray;

public class NodeComponent implements BaseComponent {
	public SnapshotArray<Entity> children = new SnapshotArray<>(true, 1, Entity.class);

	public void removeChild(Entity entity) {
		children.removeValue(entity, false);
	}

	public void addChild(Entity entity) {
		children.add(entity);
	}

	@Override
	public void reset() {
		children.clear();
	}
}
