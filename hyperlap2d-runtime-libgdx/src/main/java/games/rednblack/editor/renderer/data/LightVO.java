package games.rednblack.editor.renderer.data;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.renderer.physics.PhysicsBodyLoader;

import java.util.Objects;

public class LightVO extends MainItemVO {
	//public int itemId = -1;
	public static enum LightType {POINT, CONE}
	public LightType type;
	public int rays = 12;
	public float distance = 10;
	public float directionDegree = 0;
	public float coneDegree = 30;
	public float softnessLength = -1f;
	public boolean isStatic = true;
	public boolean isXRay = true;
	public boolean isSoft = true;
	public boolean isActive = true;
	
	public LightVO() {
		tint = new float[4];
		tint[0] = 1f;
		tint[1] = 1f;
		tint[2] = 1f;
		tint[3] = 1f;
	}
	
	public LightVO(LightVO vo) {
		super(vo);
		type = vo.type;
		rays = vo.rays;
		distance = vo.distance;
		directionDegree = vo.directionDegree;
		coneDegree = vo.coneDegree;
		isStatic = vo.isStatic;
		isXRay = vo.isXRay;
		softnessLength = vo.softnessLength;
		isActive = vo.isActive;
		isSoft = vo.isSoft;
	}

	@Override
	public void loadFromEntity(Entity entity) {
		super.loadFromEntity(entity);

		LightObjectComponent lightObjectComponent = entity.getComponent(LightObjectComponent.class);
		type = lightObjectComponent.getType();
		rays = lightObjectComponent.rays;
		distance = lightObjectComponent.distance;
		directionDegree = lightObjectComponent.directionDegree;
		coneDegree = lightObjectComponent.coneDegree;
		isStatic = lightObjectComponent.isStatic;
		isXRay = lightObjectComponent.isXRay;
		softnessLength = lightObjectComponent.softnessLength;
		isSoft = lightObjectComponent.isSoft;
		isActive = lightObjectComponent.isActive;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		LightVO lightVO = (LightVO) o;
		return rays == lightVO.rays &&
				Float.compare(lightVO.distance, distance) == 0 &&
				Float.compare(lightVO.directionDegree, directionDegree) == 0 &&
				Float.compare(lightVO.coneDegree, coneDegree) == 0 &&
				Float.compare(lightVO.softnessLength, softnessLength) == 0 &&
				isStatic == lightVO.isStatic &&
				isXRay == lightVO.isXRay &&
				isSoft == lightVO.isSoft &&
				isActive == lightVO.isActive &&
				type == lightVO.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, rays, distance, directionDegree, coneDegree, softnessLength, isStatic, isXRay, isSoft, isActive);
	}
}
