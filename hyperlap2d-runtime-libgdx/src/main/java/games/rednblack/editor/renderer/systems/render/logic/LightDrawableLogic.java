package games.rednblack.editor.renderer.systems.render.logic;


import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;

public class LightDrawableLogic implements Drawable {

    private ComponentMapper<LightObjectComponent> lightComponentMapper;
    private ComponentMapper<TintComponent> tintComponentMapper;
    private ComponentMapper<ParentNodeComponent> parentNodeComponentComponentMapper;

    private final Color tmpColor = new Color();

    public LightDrawableLogic() {
        lightComponentMapper = ComponentMapper.getFor(LightObjectComponent.class);
        tintComponentMapper = ComponentMapper.getFor(TintComponent.class);
        parentNodeComponentComponentMapper = ComponentMapper.getFor(ParentNodeComponent.class);
    }

    @Override
    public void draw(Batch batch, Entity entity, float parentAlpha) {
        LightObjectComponent lightObjectComponent = lightComponentMapper.get(entity);
        TintComponent tint = tintComponentMapper.get(entity);

        tmpColor.set(tint.color);
        tmpColor.a *= tintComponentMapper.get(parentNodeComponentComponentMapper.get(entity).parentEntity).color.a;

        lightObjectComponent.lightObject.setColor(tmpColor);
    }

}