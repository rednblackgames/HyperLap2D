package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import com.badlogic.ashley.core.Entity;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceRegionCommandBuilder;
import games.rednblack.h2d.common.factory.IFactory;

public class AutoTileDrawStrategy extends BasicDrawStrategy {

    private final ReplaceRegionCommandBuilder replaceRegionCommandBuilder = new ReplaceRegionCommandBuilder();

	public AutoTileDrawStrategy(TiledPlugin plugin) {
		super(plugin);
	}

	@Override
	public void drawTile(float x, float y, int row, int column) {
        Entity underneathTile = tiledPlugin.getPluginEntityWithParams(row, column);
        if (underneathTile != null) {
            updateTile(underneathTile);
            return;
        }

        IFactory itemFactory =  tiledPlugin.getAPI().getItemFactory();
        temp.set(x, y);
        if (itemFactory.createSimpleImage(tiledPlugin.getSelectedTileName() + TiledPlugin.AUTO_TILE_DRAW_SUFFIX, temp)) {
            Entity imageEntity = itemFactory.getCreatedEntity();
            postProcessEntity(imageEntity, x, y, row, column);
        }
	}

	@Override
	public void updateTile(Entity entity) {
        if (!checkValidTile(entity)) return;

        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
        if (textureRegionComponent != null && textureRegionComponent.regionName != null) {
            // there is already other tile under this one
        	String selectedAutoTileName = tiledPlugin.getSelectedTileName();
        	String region = selectedAutoTileName + TiledPlugin.AUTO_TILE_DRAW_SUFFIX;
            if (!textureRegionComponent.regionName.equals(region)) {
                replaceRegionCommandBuilder.begin(entity);
                replaceRegionCommandBuilder.setRegion(tiledPlugin.getAPI().getSceneLoader().getRm().getTextureRegion(region));
                replaceRegionCommandBuilder.setRegionName(region);
                replaceRegionCommandBuilder.execute(tiledPlugin.facade);

                MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
                mainItemComponent.tags.add(TiledPlugin.AUTO_TILE_TAG);
                mainItemComponent.setCustomVars(TiledPlugin.REGION, selectedAutoTileName);
            }
        }
	}

    @Override
	protected void postProcessEntity(Entity entity, float x, float y, int row, int column) {
    	super.postProcessEntity(entity, x, y, row, column);

        MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
        mainItemComponent.tags.add(TiledPlugin.AUTO_TILE_TAG);
        mainItemComponent.setCustomVars(TiledPlugin.REGION, tiledPlugin.getSelectedTileName());
    }

}
