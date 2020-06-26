package games.rednblack.editor.renderer.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/**
 * Created by aurel on 19/02/16.
 */
public class ForceUtils {



    public static void applyImpulse(Vector2 impulsePosition, float strength, float influenceArea, Body body) {
        Vector2 v = body.getPosition().cpy().sub(impulsePosition);

        float length = MathUtils.clamp(v.len(), 0, influenceArea);
        v.nor().scl(influenceArea - length).scl(strength);

        applyForce(v, body, false, false, new Vector2(0, 0));
    }

    public static void applyForce(Vector2 force, Body body) {
        applyForce(force, body, false, false, new Vector2(0, 0));
    }

    public static void applyForce(Vector2 force, Body body, Vector2 relativePoint) {
        applyForce(force, body, false, false, relativePoint);
    }

    public static void applyForce(Vector2 force, Body body, boolean relativeToVelocity, boolean relativeToMass, Vector2 relativePoint) {
        Vector2 forceToApply = force.cpy();

        if (relativeToVelocity) forceToApply.sub(body.getLinearVelocity());
        if (relativeToMass) forceToApply.scl(body.getMass());

        body.applyForce(forceToApply, relativePoint.cpy().add(body.getPosition()), true);
    }
}
