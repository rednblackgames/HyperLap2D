package games.rednblack.editor.renderer.systems.action;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.systems.action.data.ForceData;
import games.rednblack.editor.renderer.systems.action.logic.ActionLogic;
import games.rednblack.editor.renderer.systems.action.logic.ForceAction;

/**
 * Created by aurel on 02/04/16.
 */
public class PhysicsActions {


    private static void initialize(Class<? extends ActionLogic> type) {
        try {
            Actions.registerActionClass(type);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Apply a force to an entity with physics component. The force is applied as long as
     * the corresponding entity as a physics component.
     * @param force The world force vector, usually in Newtons (N)
     * @return The games.rednblack.editor.renderer.systems.action.data.ForceData object
     */
    public static ForceData force(Vector2 force) {
        initialize(ForceAction.class);
        ForceData forceData = new ForceData(force);

        forceData.logicClassName = ForceAction.class.getName();
        return forceData;
    }

    /**
     * Apply a force to an entity with physics component. The force is applied as long as
     * the corresponding entity as a physics component.
     * @param force The world force vector, usually in Newtons (N)
     * @param relativePoint The point where the force is applied relative to the body origin
     * @return The games.rednblack.editor.renderer.systems.action.data.ForceData object
     */
    public static ForceData force(Vector2 force, Vector2 relativePoint) {
        initialize(ForceAction.class);
        ForceData forceData = new ForceData(force, relativePoint);

        forceData.logicClassName = ForceAction.class.getName();
        return forceData;
    }

    /**
     * Apply a force to an entity with physics component.
     * @param force The world force vector, usually in Newtons (N)
     * @param relativePoint The point where the force is applied relative to the body origin
     * @param linkedComponent The force is applied as long as the corresponding entity
     *                        has this component
     * @return The games.rednblack.editor.renderer.systems.action.data.ForceData object
     */
    public static ForceData force(Vector2 force, Vector2 relativePoint, Class<? extends Component> linkedComponent) {
        ForceData forceData = force(force, relativePoint);

        forceData.linkedComponentMapper = ComponentMapper.getFor(linkedComponent);
        return forceData;
    }
}
