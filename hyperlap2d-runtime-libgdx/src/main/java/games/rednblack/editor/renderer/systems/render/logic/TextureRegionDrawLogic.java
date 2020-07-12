package games.rednblack.editor.renderer.systems.render.logic;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.*;

public class TextureRegionDrawLogic implements Drawable {

	final private ComponentMapper<TintComponent> tintComponentComponentMapper;
	final private ComponentMapper<TextureRegionComponent> textureRegionMapper;
	final private ComponentMapper<TransformComponent> transformMapper;
	final private ComponentMapper<DimensionsComponent> dimensionsComponentComponentMapper;

	final private Vector2 atlasCoordsVector = new Vector2();
	final private Vector2 atlasSizeVector = new Vector2();

	public TextureRegionDrawLogic() {
		tintComponentComponentMapper = ComponentMapper.getFor(TintComponent.class);
		textureRegionMapper = ComponentMapper.getFor(TextureRegionComponent.class);
		transformMapper = ComponentMapper.getFor(TransformComponent.class);
		dimensionsComponentComponentMapper = ComponentMapper.getFor(DimensionsComponent.class);
	}

	@Override
	public void draw(Batch batch, Entity entity, float parentAlpha) {
        TextureRegionComponent entityTextureRegionComponent = textureRegionMapper.get(entity);

        if(entityTextureRegionComponent.polygonSprite != null) {
            drawTiledPolygonSprite(batch, entity);
        } else {
            drawSprite(batch, entity, parentAlpha);
        }
	}

    public void drawSprite(Batch batch, Entity entity, float parentAlpha) {
        TintComponent tintComponent = tintComponentComponentMapper.get(entity);
        TransformComponent entityTransformComponent = transformMapper.get(entity);
        TextureRegionComponent entityTextureRegionComponent = textureRegionMapper.get(entity);
        DimensionsComponent dimensionsComponent = dimensionsComponentComponentMapper.get(entity);
        batch.setColor(tintComponent.color.r, tintComponent.color.g, tintComponent.color.b, tintComponent.color.a * parentAlpha);

        batch.draw(entityTextureRegionComponent.region,
                entityTransformComponent.x, entityTransformComponent.y,
                entityTransformComponent.originX, entityTransformComponent.originY,
                dimensionsComponent.width, dimensionsComponent.height,
                entityTransformComponent.scaleX, entityTransformComponent.scaleY,
                entityTransformComponent.rotation);
    }

    public void drawPolygonSprite(Batch batch, Entity entity) {
        TintComponent tintComponent = tintComponentComponentMapper.get(entity);
        TransformComponent entityTransformComponent = transformMapper.get(entity);
        TextureRegionComponent entityTextureRegionComponent = textureRegionMapper.get(entity);

        DimensionsComponent dimensionsComponent = dimensionsComponentComponentMapper.get(entity);

        entityTextureRegionComponent.polygonSprite.setBounds(entityTransformComponent.x, entityTransformComponent.y, dimensionsComponent.width, dimensionsComponent.height);
        entityTextureRegionComponent.polygonSprite.setRotation(entityTransformComponent.rotation);
        entityTextureRegionComponent.polygonSprite.setOrigin(entityTransformComponent.originX, entityTransformComponent.originY);
        entityTextureRegionComponent.polygonSprite.setColor(tintComponent.color);
        entityTextureRegionComponent.polygonSprite.setScale(entityTransformComponent.scaleX, entityTransformComponent.scaleY);
        entityTextureRegionComponent.polygonSprite.draw((PolygonSpriteBatch) batch);
    }

    public void drawTiledPolygonSprite(Batch batch, Entity entity) {
    	batch.flush();
        TintComponent tintComponent = tintComponentComponentMapper.get(entity);
        TransformComponent entityTransformComponent = transformMapper.get(entity);
        TextureRegionComponent entityTextureRegionComponent = textureRegionMapper.get(entity);
        
        DimensionsComponent dimensionsComponent = dimensionsComponentComponentMapper.get(entity);
        float ppwu = dimensionsComponent.width/entityTextureRegionComponent.region.getRegionWidth();
        atlasCoordsVector.set(entityTextureRegionComponent.region.getU(), entityTextureRegionComponent.region.getV());
        atlasSizeVector.set(entityTextureRegionComponent.region.getU2()-entityTextureRegionComponent.region.getU(), entityTextureRegionComponent.region.getV2()-entityTextureRegionComponent.region.getV());

        batch.getShader().setUniformi("isRepeat", entityTextureRegionComponent.isRepeat ? 1 : 0);
        batch.getShader().setUniformf("atlasCoord", atlasCoordsVector);
    	batch.getShader().setUniformf("atlasSize", atlasSizeVector);
        //System.out.println(entityTransformComponent.originX);
        //batch.setColor(tintComponent.color);
        entityTextureRegionComponent.polygonSprite.setColor(tintComponent.color);
        float originX = entityTransformComponent.originX * entityTransformComponent.scaleX / ppwu;
        float originY = entityTransformComponent.originY * entityTransformComponent.scaleY / ppwu;
        entityTextureRegionComponent.polygonSprite.setOrigin(originX, originY);
        entityTextureRegionComponent.polygonSprite.setPosition(entityTransformComponent.x - originX + dimensionsComponent.width / 2, entityTransformComponent.y - originY + dimensionsComponent.height / 2);
        entityTextureRegionComponent.polygonSprite.setRotation(entityTransformComponent.rotation);
        entityTextureRegionComponent.polygonSprite.setScale(ppwu);
        entityTextureRegionComponent.polygonSprite.draw((PolygonSpriteBatch) batch);
        batch.flush();
        batch.getShader().setUniformi("isRepeat", 0);
    }
}
