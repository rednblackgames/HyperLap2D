package games.rednblack.editor.system;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;
import games.rednblack.editor.renderer.systems.ParticleSystem;

/**
 * Some particle panels might not be continuous, so they will stop after first iteration, which is ok
 * This system will make sure they look continuous while in editor, so user will find and see them easily.
 *
 */
public class ParticleContinuousSystem extends ParticleSystem {

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        super.processEntity(entity, deltaTime);

        ParticleComponent particleComponent = particleComponentMapper.get(entity);
        ParticleEffect particleEffect = particleComponent.particleEffect;

        if (particleEffect.isComplete()) {
            particleEffect.reset();
        }
    }
}
