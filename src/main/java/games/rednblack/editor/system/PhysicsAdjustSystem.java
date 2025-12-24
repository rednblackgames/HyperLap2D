package games.rednblack.editor.system;

import com.artemis.annotations.All;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.systems.PhysicsSystem;
import games.rednblack.editor.renderer.utils.TransformMathUtils;
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
		DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

		if(physicsBodyComponent.body == null) return;

		transformVec.x = transformComponent.x + transformComponent.originX;
		transformVec.y = transformComponent.y + transformComponent.originY;

		applyPolygonOffset(dimensionsComponent, transformComponent);

		int parentEntity = parentNodeComponentMapper.get(entity).parentEntity;
		if (parentEntity != -1) {
			TransformMathUtils.localToAscendantCoordinates(-1, parentEntity, transformVec, transformComponentMapper, parentNodeComponentMapper);
		}

		float rotation = TransformMathUtils.localToSceneRotation(entity, transformComponentMapper, parentNodeComponentMapper);

		physicsBodyComponent.body.setTransform(transformVec, rotation * MathUtils.degreesToRadians);
	}

	private void applyPolygonOffset(DimensionsComponent dimensionsComponent, TransformComponent transformComponent) {
		if (dimensionsComponent.polygon != null) {
			Rectangle rect = dimensionsComponent.polygon.getBoundingRectangle();
			if (rect.x != 0 || rect.y != 0) {
				float sX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
				float sY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);

				float targetX = rect.x * sX;
				float targetY = rect.y * sY;

				float rad = transformComponent.rotation * MathUtils.degreesToRadians;
				float cos = MathUtils.cos(rad);
				float sin = MathUtils.sin(rad);

				float rotatedX = targetX * cos - targetY * sin;
				float rotatedY = targetX * sin + targetY * cos;

				float diffX = rect.x - rotatedX;
				float diffY = rect.y - rotatedY;

				transformVec.add(diffX, diffY);
			}
		}
	}
}
