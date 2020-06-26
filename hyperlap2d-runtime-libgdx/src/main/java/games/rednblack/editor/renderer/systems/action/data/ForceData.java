package games.rednblack.editor.renderer.systems.action.data;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by aurel on 19/02/16.
 */
public class ForceData extends ComponentData {
    public Vector2 force;
    public Vector2 relativePoint;

    public ForceData(Vector2 force) {
        this(force, new Vector2(0, 0));
    }

    public ForceData(Vector2 force, Vector2 relativePoint) {
        super();
        this.force = force;
        this.relativePoint = relativePoint;
    }
}
