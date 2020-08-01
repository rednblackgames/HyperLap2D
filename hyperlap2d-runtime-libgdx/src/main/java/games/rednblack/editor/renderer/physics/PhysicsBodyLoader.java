package games.rednblack.editor.renderer.physics;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.utils.TransformMathUtils;

/**
 * Created by azakhary on 9/28/2014.
 */
public class PhysicsBodyLoader {

    private static PhysicsBodyLoader instance;
    private final Vector2 bodyPosition = new Vector2();

    public static PhysicsBodyLoader getInstance() {
        if(instance == null) {
            instance = new PhysicsBodyLoader();
        }
        return instance;
    }

    public float scale;

    private PhysicsBodyLoader() {
    }

    public void setScaleFromPPWU(float pixelPerWU) {
        scale = 1f/(pixelPerWU);
    }

    public static float getScale() {
        return getInstance().scale;
    }

    public Body createBody(World world, Entity entity, PhysicsBodyComponent physicsComponent, Vector2[][] minPolygonData, TransformComponent transformComponent) {
        if(physicsComponent == null) {
            return null;
        }

        FixtureDef fixtureDef = new FixtureDef();

        fixtureDef.density = physicsComponent.density;
        fixtureDef.friction = physicsComponent.friction;
        fixtureDef.restitution = physicsComponent.restitution;

        fixtureDef.isSensor = physicsComponent.sensor;

        fixtureDef.filter.maskBits = physicsComponent.filter.maskBits;
        fixtureDef.filter.groupIndex = physicsComponent.filter.groupIndex;
        fixtureDef.filter.categoryBits = physicsComponent.filter.categoryBits;

        BodyDef bodyDef = new BodyDef();
        bodyPosition.set(transformComponent.originX, transformComponent.originY);
        TransformMathUtils.localToSceneCoordinates(entity, bodyPosition);
        bodyDef.position.set(bodyPosition.x * getScale(), bodyPosition.y * getScale());
        bodyDef.angle = transformComponent.rotation * MathUtils.degreesToRadians;

        bodyDef.gravityScale = physicsComponent.gravityScale;
        bodyDef.linearDamping = physicsComponent.damping < 0 ? 0 : physicsComponent.damping;
        bodyDef.angularDamping = physicsComponent.angularDamping < 0 ? 0 : physicsComponent.angularDamping;

        bodyDef.awake = physicsComponent.awake;
        bodyDef.allowSleep = physicsComponent.allowSleep;
        bodyDef.bullet = physicsComponent.bullet;
        bodyDef.fixedRotation = physicsComponent.fixedRotation;

        if(physicsComponent.bodyType == 0) {
            bodyDef.type = BodyDef.BodyType.StaticBody;
        } else if (physicsComponent.bodyType == 1){
            bodyDef.type = BodyDef.BodyType.KinematicBody;
        } else {
            bodyDef.type = BodyDef.BodyType.DynamicBody;
        }

        Body body = world.createBody(bodyDef);

        PolygonShape polygonShape = new PolygonShape();

        for (Vector2[] minPolygonDatum : minPolygonData) {
            float[] verts = new float[minPolygonDatum.length * 2];
            for (int j = 0; j < verts.length; j += 2) {
                float tempX = minPolygonDatum[j / 2].x;
                float tempY = minPolygonDatum[j / 2].y;

                minPolygonDatum[j / 2].x -= transformComponent.originX;
                minPolygonDatum[j / 2].y -= transformComponent.originY;

                minPolygonDatum[j / 2].x *= transformComponent.scaleX;
                minPolygonDatum[j / 2].y *= transformComponent.scaleY;

                verts[j] = minPolygonDatum[j / 2].x * scale;
                verts[j + 1] = minPolygonDatum[j / 2].y * scale;

                minPolygonDatum[j / 2].x = tempX;
                minPolygonDatum[j / 2].y = tempY;

            }
            polygonShape.set(verts);
            fixtureDef.shape = polygonShape;
            body.createFixture(fixtureDef);
        }

        polygonShape.dispose();

        if (physicsComponent.mass != 0) {
            MassData massData = new MassData();
            massData.mass = physicsComponent.mass;
            massData.center.set(physicsComponent.centerOfMass);
            massData.I = physicsComponent.rotationalInertia;

            body.setMassData(massData);
        }

        return body;
    }

}
