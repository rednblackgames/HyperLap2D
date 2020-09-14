package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by aurel on 19/02/16.
 */
public class ForceData extends ComponentData {
    public Vector2 force = new Vector2();
    public Vector2 relativePoint = new Vector2();

    public void setForce(Vector2 force) {
        setForce(force, null);
    }

    public void setForce(Vector2 force, Vector2 relativePoint) {
        this.force.set(force);
        
        if (relativePoint == null)
            this.relativePoint.set(0, 0);
        else
            this.relativePoint.set(relativePoint);
    }

    @Override
    public void reset() {
        super.reset();

        force.set(0, 0);
        relativePoint.set(0, 0);
    }
}
