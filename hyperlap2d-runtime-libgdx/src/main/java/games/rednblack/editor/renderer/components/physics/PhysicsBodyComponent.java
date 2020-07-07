package games.rednblack.editor.renderer.components.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import games.rednblack.editor.renderer.components.RemovableComponent;

public class PhysicsBodyComponent implements RemovableComponent {
	public int bodyType;

	public float mass;
	public Vector2 centerOfMass;
	public float rotationalInertia;
	public float damping;
    public float angularDamping;
	public float gravityScale;

	public boolean allowSleep;
	public boolean awake;
	public boolean bullet;
    public boolean sensor;
    public boolean fixedRotation;

	public float density;
	public float friction;
	public float restitution;
    public Filter filter;

    public float centerX;
    public float centerY;

    public Body body;
    public boolean needToRefreshBody = false;

    public PhysicsBodyComponent() {
        // putting default values
        bodyType = 0;
        mass = 1;
        centerOfMass = new Vector2(0, 0);
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
        filter = new Filter();
    }

    @Override
    public void onRemove() {
        if (body != null && body.getWorld() != null) {
            body.getWorld().destroyBody(body);
            body = null;
        }
    }
}
