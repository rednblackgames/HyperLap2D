package games.rednblack.editor.proxy;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;

/**
 * Read-only transform/dimensions accessors for the runtime ECS, exposed to the view layer
 * through {@link EntityDataProxy#transform()} (Phase 3 decoupling). View-facing replacement
 * for the read side of the static {@code EntityUtils} position/size methods.
 *
 * <p><b>No setters</b> — view writes (drag/resize in-progress mutations) go direct via
 * {@link EntityDataProxy#get(int, Class)} and are committed by a revertible command on
 * drag-end. Built on the same {@link EntityDataProxy#get(int, Class)} path {@code EntityUtils} uses.</p>
 */
public class EntityTransform {

    private final EntityDataProxy entityData;

    EntityTransform(EntityDataProxy entityData) {
        this.entityData = entityData;
    }

    public Vector2 getPosition(int entity) {
        TransformComponent transformComponent = entityData.get(entity, TransformComponent.class);
        return new Vector2(transformComponent.x, transformComponent.y);
    }

    public void getPosition(int entity, Vector2 position) {
        TransformComponent transformComponent = entityData.get(entity, TransformComponent.class);
        position.set(transformComponent.x, transformComponent.y);
    }

    public Vector2 getSize(int entity) {
        DimensionsComponent dimensionsComponent = entityData.get(entity, DimensionsComponent.class);
        return new Vector2(dimensionsComponent.width, dimensionsComponent.height);
    }

    public void getSize(int entity, Vector2 size) {
        DimensionsComponent dimensionsComponent = entityData.get(entity, DimensionsComponent.class);
        size.set(dimensionsComponent.width, dimensionsComponent.height);
    }
}