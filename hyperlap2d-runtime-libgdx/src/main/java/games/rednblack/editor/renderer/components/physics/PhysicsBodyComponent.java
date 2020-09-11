package games.rednblack.editor.renderer.components.physics;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.World;
import games.rednblack.editor.renderer.commons.RefreshableObject;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.components.RemovableComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.physics.PhysicsBodyLoader;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public class PhysicsBodyComponent extends RefreshableObject implements RemovableComponent {
	public int bodyType = 0;

	public float mass = 0;
	public Vector2 centerOfMass = new Vector2(0, 0);
	public float rotationalInertia = 1;
	public float damping = 0;
    public float angularDamping = 0;
	public float gravityScale = 1;

	public boolean allowSleep = true;
	public boolean awake = true;
	public boolean bullet = false;
    public boolean sensor = false;
    public boolean fixedRotation = false;

	public float density = 1;
	public float friction = 1;
	public float restitution = 0;
    public Filter filter = new Filter();

    public float centerX;
    public float centerY;

    public Body body;
    private World world;

    public PhysicsBodyComponent() {

    }

    @Override
    public void onRemove() {
        if (body != null && body.getWorld() != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }

    @Override
    public void reset() {
        centerX = 0;
        centerY = 0;

        bodyType = 0;
        mass = 0;
        centerOfMass.set(0, 0);
        rotationalInertia = 1;
        damping = 0;
        gravityScale = 1;
        allowSleep = true;
        sensor = false;
        awake = true;
        bullet = false;
        density = 1;
        friction = 1;
        restitution = 0;
        fixedRotation = false;
        angularDamping = 0;

        filter.categoryBits = 0x0001;
        filter.maskBits = -1;
        filter.groupIndex = 0;

        needsRefresh = false;
        body = null;
    }

    public void setWorld(World world) {
        if (this.world == null)
            this.world = world;
    }

    @Override
    protected void refresh(Entity entity) {
        PolygonComponent polygonComponent = ComponentRetriever.get(entity, PolygonComponent.class);
        if(polygonComponent == null || polygonComponent.vertices == null) return;

        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);

        if (body != null) {
            world.destroyBody(body);
        }

        centerX = transformComponent.originX;
        centerY = transformComponent.originY;

        body = PhysicsBodyLoader.getInstance().createBody(world, entity, this, polygonComponent.vertices, transformComponent);
        body.setUserData(entity);
    }
}
