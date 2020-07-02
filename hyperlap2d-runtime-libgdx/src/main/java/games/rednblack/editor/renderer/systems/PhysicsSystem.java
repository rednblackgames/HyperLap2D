package games.rednblack.editor.renderer.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.components.ScriptComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.physics.PhysicsBodyLoader;
import games.rednblack.editor.renderer.physics.PhysicsContact;
import games.rednblack.editor.renderer.scripts.IScript;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.TransformMathUtils;

public class PhysicsSystem extends IteratingSystem implements ContactListener {

	protected ComponentMapper<TransformComponent> transformComponentMapper = ComponentMapper.getFor(TransformComponent.class);

	private final float TIME_STEP = 1f/60;
	private final World world;
	private boolean isPhysicsOn = true;
	private float accumulator = 0;

	public PhysicsSystem(World world) {
		super(Family.all(PhysicsBodyComponent.class).get());
		this.world = world;
	}

	@Override
	public void update (float deltaTime) {
		for (int i = 0; i < getEntities().size(); ++i) {
			processEntity(getEntities().get(i), deltaTime);
		}

		if (world != null && isPhysicsOn) {
			doPhysicsStep(deltaTime);
		}
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		TransformComponent transformComponent =  transformComponentMapper.get(entity);

		processBody(entity);

		PhysicsBodyComponent physicsBodyComponent = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
		Body body = physicsBodyComponent.body;
		transformComponent.x = 0;
		transformComponent.y = 0;
		transformComponent.rotation = 0;
		Vector2 localCoords = TransformMathUtils.sceneToLocalCoordinates(entity, body.getPosition().cpy().scl(1 / PhysicsBodyLoader.getScale()));
		transformComponent.x = localCoords.x - transformComponent.originX;
		transformComponent.y = localCoords.y - transformComponent.originY;
		transformComponent.rotation = body.getAngle() * MathUtils.radiansToDegrees;
	}

	protected void processBody(Entity entity) {
		PhysicsBodyComponent physicsBodyComponent = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
		PolygonComponent polygonComponent = ComponentRetriever.get(entity, PolygonComponent.class);
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);

		if(polygonComponent == null && physicsBodyComponent.body != null) {
			world.destroyBody(physicsBodyComponent.body);
			physicsBodyComponent.body = null;
		}

		if((physicsBodyComponent.body == null && polygonComponent != null) || physicsBodyComponent.needToRefreshBody) {
			if(polygonComponent == null || polygonComponent.vertices == null) return;

			if (physicsBodyComponent.needToRefreshBody && physicsBodyComponent.body != null) {
				world.destroyBody(physicsBodyComponent.body);
				physicsBodyComponent.body = null;
				physicsBodyComponent.needToRefreshBody = false;
			}

            DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);

            physicsBodyComponent.centerX = dimensionsComponent.width/2;
            physicsBodyComponent.centerY = dimensionsComponent.height/2;

			PhysicsBodyComponent bodyPropertiesComponent = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
			physicsBodyComponent.body = PhysicsBodyLoader.getInstance().createBody(world, entity, bodyPropertiesComponent, polygonComponent.vertices, transformComponent);

			physicsBodyComponent.body.setUserData(entity);
		}
	}

	private void doPhysicsStep(float deltaTime) {
		// fixed time step
		// max frame time to avoid spiral of death (on slow devices)
		float frameTime = Math.min(deltaTime, 0.25f);
		accumulator += frameTime;
		while (accumulator >= TIME_STEP) {
			world.step(TIME_STEP, 6, 2);
			accumulator -= TIME_STEP;
		}
	}

	public void setPhysicsOn(boolean isPhysicsOn) {
		this.isPhysicsOn = isPhysicsOn;
	}

	private void processCollision(Contact contact, boolean in) {
		// Get both fixtures
		Fixture f1 = contact.getFixtureA();
		Fixture f2 = contact.getFixtureB();
		// Get both bodies
		Body b1 = f1.getBody();
		Body b2 = f2.getBody();

		// Get our objects that reference these bodies
		Object o1 = b1.getUserData();
		Object o2 = b2.getUserData();

		if (!(o1 instanceof Entity) || !(o2 instanceof Entity))
			return;

		// cast to entity
		Entity et1 = (Entity) o1;
		Entity et2 = (Entity) o2;
		// get script comp
		ScriptComponent ic1 = ComponentRetriever.get(et1, ScriptComponent.class);
		ScriptComponent ic2 = ComponentRetriever.get(et2, ScriptComponent.class);

		// cast script to contacts, if scripts implement contacts
		for (IScript sc : ic1.scripts) {
			if (sc instanceof PhysicsContact) {
				PhysicsContact ct = (PhysicsContact) sc;
				if (in)
					ct.beginContact(et2);
				else
					ct.endContact(et2);
			}
		}

		for (IScript sc : ic2.scripts) {
			if (sc instanceof PhysicsContact) {
				PhysicsContact ct = (PhysicsContact) sc;
				if (in)
					ct.beginContact(et1);
				else
					ct.endContact(et1);
			}
		}
	}

	@Override
	public void beginContact(Contact contact) {
		processCollision(contact, true);
	}

	@Override
	public void endContact(Contact contact) {
		processCollision(contact, false);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {

	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {

	}
}
