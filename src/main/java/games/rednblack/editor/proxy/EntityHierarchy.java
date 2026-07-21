package games.rednblack.editor.proxy;

import games.rednblack.editor.renderer.components.NodeComponent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Read-only hierarchy accessors for the runtime ECS, exposed to the view layer through
 * {@link EntityDataProxy#hierarchy()} (Phase 3 decoupling). View-facing replacement for the
 * hierarchy side of the static {@code EntityUtils} methods. Built on the same
 * {@link EntityDataProxy#get(int, Class)} path {@code EntityUtils} uses.
 */
public class EntityHierarchy {

    private final EntityDataProxy entityData;

    EntityHierarchy(EntityDataProxy entityData) {
        this.entityData = entityData;
    }

    public HashSet<Integer> getChildren(int entity) {
        NodeComponent nodeComponent = entityData.get(entity, NodeComponent.class);
        if (nodeComponent == null)
            return null;
        Integer[] children = nodeComponent.children.toArray();
        return new HashSet<>(Arrays.asList(children));
    }

    /** Walks the live ECS child tree depth-first, applying {@code action} to each entity. */
    public void applyActionRecursively(int root, Consumer<Integer> action) {
        action.accept(root);
        NodeComponent nodeComponent = entityData.get(root, NodeComponent.class);
        if (nodeComponent != null && nodeComponent.children != null) {
            for (int targetEntity : nodeComponent.children) {
                applyActionRecursively(targetEntity, action);
            }
        }
    }
}