package games.rednblack.h2d.extention.spine;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.esotericsoftware.spine.SkeletonRenderer;
import games.rednblack.editor.renderer.systems.render.logic.Drawable;

public class SpineDrawableLogic implements Drawable {

    private final ComponentMapper<SpineObjectComponent> spineMapper;
    private final SkeletonRenderer skeletonRenderer;

    public SpineDrawableLogic() {
        spineMapper = ComponentMapper.getFor(SpineObjectComponent.class);
        skeletonRenderer = new SkeletonRenderer();
    }

    @Override
    public void draw(Batch batch, Entity entity, float parentAlpha) {
        SpineObjectComponent spineObjectComponent = spineMapper.get(entity);

        skeletonRenderer.draw((PolygonSpriteBatch)batch, spineObjectComponent.skeleton);
        //TODO parent alpha thing
    }
}