package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.renderer.data.LightBodyDataVO;

/**
 * Typed payload for {@link UpdateLightBodyDataCommand}, replacing the
 * positional {@code Object[]{entity, LightBodyDataVO}} (Phase 1 typed payloads).
 */
public record LightDataPayload(int entity, LightBodyDataVO vo) {
}