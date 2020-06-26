package games.rednblack.editor.renderer.systems.render.logic;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;

public class ParticleDrawableLogic implements Drawable {

	private ComponentMapper<ParticleComponent> particleComponentMapper = ComponentMapper.getFor(ParticleComponent.class);
	private ComponentMapper<TransformComponent> transformComponentMapper = ComponentMapper.getFor(TransformComponent.class);

	public ParticleDrawableLogic() {
	}
	
	@Override
	public void draw(Batch batch, Entity entity, float parentAlpha) {
		ParticleComponent particleComponent = particleComponentMapper.get(entity);
		//Matrix4 matrix = batch.getTransformMatrix().scl(particleComponent.worldMultiplyer);
		//batch.setTransformMatrix(matrix);
		TransformComponent transformComponent = transformComponentMapper.get(entity);
		//particleEffect.setPosition(transformComponent.x/particleComponent.worldMultiplyer, transformComponent.y/particleComponent.worldMultiplyer);
		particleComponent.particleEffect.setPosition(transformComponent.x, transformComponent.y);
		particleComponent.particleEffect.draw(batch);
		//batch.setTransformMatrix(batch.getTransformMatrix().scl(1f/particleComponent.worldMultiplyer));
	}

}
