package games.rednblack.editor.renderer.systems.render.logic;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.components.label.LabelComponent;
import games.rednblack.editor.renderer.components.label.TypingLabelComponent;

public class LabelDrawableLogic implements Drawable {

	private final ComponentMapper<LabelComponent> labelComponentMapper;
	private final ComponentMapper<TypingLabelComponent> typingLabelComponentMapper;
	private final ComponentMapper<TintComponent> tintComponentMapper;
	private final ComponentMapper<DimensionsComponent> dimensionsComponentMapper;
	private final ComponentMapper<TransformComponent> transformMapper;
	private final ComponentMapper<ParentNodeComponent> parentNodeComponentComponentMapper;

	private final Color tmpColor = new Color();

	public LabelDrawableLogic() {
		labelComponentMapper = ComponentMapper.getFor(LabelComponent.class);
		tintComponentMapper = ComponentMapper.getFor(TintComponent.class);
		dimensionsComponentMapper = ComponentMapper.getFor(DimensionsComponent.class);
		transformMapper = ComponentMapper.getFor(TransformComponent.class);
		parentNodeComponentComponentMapper = ComponentMapper.getFor(ParentNodeComponent.class);
		typingLabelComponentMapper = ComponentMapper.getFor(TypingLabelComponent.class);
	}
	
	@Override
	public void draw(Batch batch, Entity entity, float parentAlpha) {
		TransformComponent entityTransformComponent = transformMapper.get(entity);
		LabelComponent labelComponent = labelComponentMapper.get(entity);
		DimensionsComponent dimensionsComponent = dimensionsComponentMapper.get(entity);
		TintComponent tint = tintComponentMapper.get(entity);
		TypingLabelComponent typingLabelComponent = typingLabelComponentMapper.get(entity);

		tmpColor.set(tint.color);

		if (labelComponent.style.background != null) {
			batch.setColor(tmpColor);
			labelComponent.style.background.draw(batch, entityTransformComponent.x, entityTransformComponent.y, dimensionsComponent.width, dimensionsComponent.height);
		}

		if(labelComponent.style.fontColor != null) tmpColor.mul(labelComponent.style.fontColor);
		tmpColor.a *= tintComponentMapper.get(parentNodeComponentComponentMapper.get(entity).parentEntity).color.a;

		if (typingLabelComponent == null) {
			labelComponent.cache.tint(tmpColor);
			labelComponent.cache.setPosition(entityTransformComponent.x, entityTransformComponent.y);
			labelComponent.cache.draw(batch);
		} else {
			typingLabelComponent.typingLabel.setColor(tmpColor);
			typingLabelComponent.typingLabel.setPosition(entityTransformComponent.x, entityTransformComponent.y);
			typingLabelComponent.typingLabel.setOrigin(entityTransformComponent.originX, entityTransformComponent.originY);
			typingLabelComponent.typingLabel.draw(batch, 1);
		}
	}

}
