package games.rednblack.editor.renderer.data;


import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;

public class ParticleEffectVO extends MainItemVO {
	public String particleName = "";
	public float particleWidth = 100;
	public float particleHeight = 100;
	//TODO add other ParticleEffect properties 
	
	public ParticleEffectVO() {
		super();
	}
	
	public ParticleEffectVO(ParticleEffectVO vo) {
		super(vo);
		particleName = new String(vo.particleName);
	}

	@Override
	public void loadFromEntity(Entity entity) {
		super.loadFromEntity(entity);

		ParticleComponent particleComponent = entity.getComponent(ParticleComponent.class);
		particleName = particleComponent.particleName;
	}
}
