package games.rednblack.editor.renderer.data;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;

import java.util.Objects;

public class PhysicsBodyDataVO {

    public int bodyType = 0;

	public float mass;
	public Vector2 centerOfMass;
	public float rotationalInertia;
	public float damping;
	public float gravityScale;
	public boolean allowSleep;
	public boolean awake;
	public boolean bullet;
    public boolean sensor;

    public float density;
    public float friction;
    public float restitution;
    
    public PhysicsBodyDataVO(){
    	centerOfMass = new Vector2();
    }
    
    public PhysicsBodyDataVO(PhysicsBodyDataVO vo){
    	bodyType = vo.bodyType;
    	mass = vo.mass;
    	centerOfMass = vo.centerOfMass.cpy();
    	rotationalInertia = vo.rotationalInertia;
    	damping = vo.damping;
    	gravityScale = vo.gravityScale;
    	allowSleep = vo.allowSleep;
        sensor = vo.sensor;
    	awake = vo.awake;
    	bullet = vo.bullet;
        density = vo.density;
        friction = vo.friction;
        restitution = vo.restitution;
    }

    public void loadFromComponent(PhysicsBodyComponent physicsComponent) {
        bodyType = physicsComponent.bodyType;
        mass = physicsComponent.mass;
        centerOfMass = physicsComponent.centerOfMass.cpy();
        rotationalInertia = physicsComponent.rotationalInertia;
        damping = physicsComponent.damping;
        gravityScale = physicsComponent.gravityScale;
        allowSleep = physicsComponent.allowSleep;
        sensor = physicsComponent.sensor;
        awake = physicsComponent.awake;
        bullet = physicsComponent.bullet;
        density = physicsComponent.density;
        friction = physicsComponent.friction;
        restitution = physicsComponent.restitution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicsBodyDataVO that = (PhysicsBodyDataVO) o;
        return bodyType == that.bodyType &&
                Float.compare(that.mass, mass) == 0 &&
                Float.compare(that.rotationalInertia, rotationalInertia) == 0 &&
                Float.compare(that.damping, damping) == 0 &&
                Float.compare(that.gravityScale, gravityScale) == 0 &&
                allowSleep == that.allowSleep &&
                awake == that.awake &&
                bullet == that.bullet &&
                sensor == that.sensor &&
                Float.compare(that.density, density) == 0 &&
                Float.compare(that.friction, friction) == 0 &&
                Float.compare(that.restitution, restitution) == 0 &&
                centerOfMass.equals(that.centerOfMass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bodyType, mass, centerOfMass, rotationalInertia, damping, gravityScale, allowSleep, awake, bullet, sensor, density, friction, restitution);
    }
}
