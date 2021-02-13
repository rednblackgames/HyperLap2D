package games.rednblack.editor.system;

import com.badlogic.ashley.core.Entity;
import com.talosvfx.talos.runtime.ParticleEffectInstance;
import games.rednblack.h2d.extension.talos.TalosComponent;
import games.rednblack.h2d.extension.talos.TalosSystem;

/**
 * Some particle panels might not be continuous, so they will stop after first iteration, which is ok
 * This system will make sure they look continuous while in editor, so user will find and see them easily.
 *
 */
public class TalosContinuousSystem extends TalosSystem {

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        super.processEntity(entity, deltaTime);

        TalosComponent talosComponent = particleComponentMapper.get(entity);
        ParticleEffectInstance effect = talosComponent.effect;

        if (!effect.isContinuous() && effect.isComplete()) {
            effect.restart();
        }
    }
}
