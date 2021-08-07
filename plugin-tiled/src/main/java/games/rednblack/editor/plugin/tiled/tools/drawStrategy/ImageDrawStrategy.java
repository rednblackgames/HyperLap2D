package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceRegionCommandBuilder;
import games.rednblack.h2d.common.factory.IFactory;

public class ImageDrawStrategy extends BasicDrawStrategy {
    private final ReplaceRegionCommandBuilder replaceRegionCommandBuilder = new ReplaceRegionCommandBuilder();

    public ImageDrawStrategy(TiledPlugin plugin) {
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
        if (itemFactory.createSimpleImage(tiledPlugin.getSelectedTileName(), temp)) {
            int imageEntity = itemFactory.getCreatedEntity();
            postProcessEntity(imageEntity, x, y, row, column);
        }
    }

    @Override
    public void updateTile(int entity) {
        if (!checkValidTile(entity)) return;

        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class, tiledPlugin.getAPI().getEngine());
        if (textureRegionComponent != null && textureRegionComponent.regionName != null) {
            // there is already other tile under this one
            if (!textureRegionComponent.regionName.equals(tiledPlugin.getSelectedTileName())) {
                String region = tiledPlugin.getSelectedTileName();
                replaceRegionCommandBuilder.begin(entity);
                replaceRegionCommandBuilder.setRegion(tiledPlugin.getAPI().getSceneLoader().getRm().getTextureRegion(region));
                replaceRegionCommandBuilder.setRegionName(region);
                replaceRegionCommandBuilder.execute(tiledPlugin.facade);

                MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class, tiledPlugin.getAPI().getEngine());
                mainItemComponent.tags.remove(TiledPlugin.AUTO_TILE_TAG);
                mainItemComponent.removeCustomVars(TiledPlugin.REGION);
            }
        }
    }
}
