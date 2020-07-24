package games.rednblack.editor.renderer.systems.render.logic;

import java.util.HashMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
import games.rednblack.editor.renderer.factory.EntityFactory;

public class DrawableLogicMapper {

	private HashMap<Integer, Drawable> logicClassMap;

	public DrawableLogicMapper() {
		logicClassMap = new HashMap<>();
		logicClassMap.put(EntityFactory.IMAGE_TYPE, 	 new TextureRegionDrawLogic());
		logicClassMap.put(EntityFactory.LABEL_TYPE, 	 new LabelDrawableLogic());
		logicClassMap.put(EntityFactory.NINE_PATCH, 	 new NinePatchDrawableLogic());
		logicClassMap.put(EntityFactory.PARTICLE_TYPE, 	 new ParticleDrawableLogic());
		logicClassMap.put(EntityFactory.SPRITE_TYPE, 	 new SpriteDrawableLogic());
		logicClassMap.put(EntityFactory.SPRITER_TYPE, 	 new SpriterDrawableLogic());
		logicClassMap.put(EntityFactory.COLOR_PRIMITIVE, new TextureRegionDrawLogic());
		logicClassMap.put(EntityFactory.LIGHT_TYPE,      new LightDrawableLogic());
	}

	public void addDrawableToMap(int type, Drawable drawable) {
		logicClassMap.put(type, drawable);
	}

	public Drawable getDrawable(int type){
		return logicClassMap.get(type);
	}
}
