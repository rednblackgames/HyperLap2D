package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceSpriteAnimationCommandBuilder;
import games.rednblack.h2d.common.factory.IFactory;

public class SpriteDrawStrategy extends BasicDrawStrategy {
    ReplaceSpriteAnimationCommandBuilder replaceSpriteAnimationCommandBuilder = new ReplaceSpriteAnimationCommandBuilder();

    public SpriteDrawStrategy(TiledPlugin plugin) {
        super(plugin);
    }

    @Override
    public void drawTile(float x, float y, int row, int column) {
        int underneathTile = tiledPlugin.getPluginEntityWithParams(row, column);
        if (underneathTile != -1) {
            updateTile(underneathTile);
            return;
        }

        IFactory itemFactory =  tiledPlugin.getAPI().getItemFactory();
        temp.set(x, y);

        if (itemFactory.createSpriteAnimation(tiledPlugin.getSelectedTileName(), temp)) {
            int imageEntity = itemFactory.getCreatedEntity();
            postProcessEntity(imageEntity, x, y, row, column);
        }
    }

    @Override
    public void updateTile(int entity) {
        if (!checkValidTile(entity)) return;

        SpriteAnimationComponent spriteAnimationComponent = ComponentRetriever.get(entity, SpriteAnimationComponent.class, tiledPlugin.getAPI().getEngine());

        if (!spriteAnimationComponent.animationName.equals(tiledPlugin.getSelectedTileName())) {
            Array<TextureAtlas.AtlasRegion> regions = getRegions(tiledPlugin.getSelectedTileName());

            replaceSpriteAnimationCommandBuilder.begin(entity);
            replaceSpriteAnimationCommandBuilder.setAnimationName(tiledPlugin.getSelectedTileName());
            replaceSpriteAnimationCommandBuilder.setRegion(regions);
            replaceSpriteAnimationCommandBuilder.execute(tiledPlugin.facade);
        }
    }

    private Array<TextureAtlas.AtlasRegion> getRegions(String filter) {
        // filtering regions by name
        Array<TextureAtlas.AtlasRegion> allRegions = tiledPlugin.getAPI().getSceneLoader().getRm().getSpriteAnimation(filter);
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        for(TextureAtlas.AtlasRegion region: allRegions) {
            if(region.name.contains(filter)) {
                regions.add(region);
            }
        }

        return regions;
    }
}
