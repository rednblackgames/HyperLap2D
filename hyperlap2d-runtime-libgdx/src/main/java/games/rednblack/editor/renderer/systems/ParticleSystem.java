package games.rednblack.editor.renderer.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;

public class ParticleSystem extends IteratingSystem {

	private ComponentMapper<ParticleComponent> particleComponentMapper = ComponentMapper.getFor(ParticleComponent.class);
    //private ComponentMapper<TransformComponent> transformComponentMapper = ComponentMapper.getFor(TransformComponent.class);
    //private ComponentMapper<ParentNodeComponent> parentNodeComponentMapper = ComponentMapper.getFor(ParentNodeComponent.class);
    //private ComponentMapper<NodeComponent> nodeComponentMapper = ComponentMapper.getFor(NodeComponent.class);
	
	public ParticleSystem() {
		super(Family.all(ParticleComponent.class).get());
	}

	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		//System.out.println("DELTAING  " + entity.getId() +"  " +deltaTime);
		ParticleComponent particleComponent = particleComponentMapper.get(entity);

		//DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
		ParticleEffect particleEffect = particleComponent.particleEffect;
		particleEffect.update(deltaTime);
		//ParentNodeComponent parentNodeComponent = parentNodeComponentMapper.get(entity);
		
//		Entity parentEntity = parentNodeComponent.parentEntity;
//		while (parentEntity != null) {
//			parentNodeComponent = nodeComponentMapper.get(parentEntity);
//			parentEntity = parentNodeComponent.parentEntity;
//		}
		
	}

}
