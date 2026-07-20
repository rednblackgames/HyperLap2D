package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.ecs.Component;

/**
 * Typed payload for {@link UpdateEntityComponentsCommand}, replacing the
 * positional {@code Object[]{entity, Array<Component>}} (Phase 1 typed payloads).
 */
public record EntityComponentsPayload(int entity, Array<Component> components) {
}