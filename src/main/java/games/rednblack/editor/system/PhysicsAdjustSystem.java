package games.rednblack.editor.system;

import com.artemis.annotations.All;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.systems.PhysicsSystem;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;

@All(PhysicsBodyComponent.class)
public class PhysicsAdjustSystem extends PhysicsSystem {

	private final Vector2 transformVec = new Vector2();
	
	public PhysicsAdjustSystem() {
		setPhysicsOn(false);
	}

	@Override
	protected void process(int entity) {
		TransformComponent transformComponent = transformComponentMapper.get(entity);
		super.process(entity);

		PhysicsBodyComponent physicsBodyComponent = SandboxComponentRetriever.get(entity, PhysicsBodyComponent.class);

		if(physicsBodyComponent.body == null) return;

		transformVec.x = transformComponent.x + transformComponent.originX;
		transformVec.y = transformComponent.y + transformComponent.originY;
		physicsBodyComponent.body.setTransform(transformVec, transformComponent.rotation * MathUtils.degreesToRadians);
	}
}
