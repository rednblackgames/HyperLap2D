package games.rednblack.editor.renderer.components.light;

import box2dLight.ConeLight;
import box2dLight.Light;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import games.rednblack.editor.renderer.components.BaseComponent;
import games.rednblack.editor.renderer.data.LightVO;
import games.rednblack.editor.renderer.data.LightVO.LightType;

public class LightObjectComponent implements BaseComponent {
	private LightType type;

	public int rays = 12;
	public float distance = 10;
	public float directionDegree = 0;
	public float coneDegree = 30;
	public float softnessLength = 1f;

	public boolean isStatic = true;
	public boolean isXRay = true;
	public boolean isSoft = true;
	public boolean isActive = true;

	public Light lightObject = null;

	public LightObjectComponent() {
	}

	public void setType(LightType type) {
		this.type = type;
	}

	public LightType getType(){
		return type;
	}

	public Light rebuildRays(RayHandler rayHandler) {
		if (rayHandler == null)
			return lightObject;

		lightObject.remove();

		if (getType() == LightVO.LightType.POINT) {
			lightObject = new PointLight(rayHandler, rays);
		} else {
			lightObject = new ConeLight(rayHandler, rays, Color.WHITE, 1, 0, 0, 0, 0);
		}

		return lightObject;
	}

	@Override
	public void reset() {
		type = null;

		rays = 12;
		distance = 10;
		directionDegree = 0;
		coneDegree = 30;
		softnessLength = 1f;

		isStatic = true;
		isXRay = true;
		isSoft = true;
		isActive = true;

		lightObject = null;
	}
}
