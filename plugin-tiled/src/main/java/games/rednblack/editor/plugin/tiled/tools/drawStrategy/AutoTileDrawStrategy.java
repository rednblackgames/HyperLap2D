package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.AutoTileVO;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceRegionCommandBuilder;
import games.rednblack.h2d.common.factory.IFactory;

public class AutoTileDrawStrategy extends BasicDrawStrategy {

    private final ReplaceRegionCommandBuilder replaceRegionCommandBuilder = new ReplaceRegionCommandBuilder();

    private String tileToDraw;
    
	public AutoTileDrawStrategy(TiledPlugin plugin) {
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
        tileToDraw = selectTileToDraw();
        if (itemFactory.createSimpleImage(tileToDraw + TiledPlugin.AUTO_TILE_DRAW_SUFFIX, temp)) {
            int imageEntity = itemFactory.getCreatedEntity();
            postProcessEntity(imageEntity, x, y, row, column);
        }
	}

	private String selectTileToDraw() {
		String retval;
		
		AutoTileVO selectedAutoTileVO = tiledPlugin.getSelectedAutoTileVO();
		if (selectedAutoTileVO.alternativeAutoTileList.isEmpty()) {
			retval = selectedAutoTileVO.regionName;
		} else {
			retval = null;
			double d = 0d;
			double rnd = Math.random();
			int i = 0;
			while (i < selectedAutoTileVO.alternativeAutoTileList.size() && retval == null) {
				d += selectedAutoTileVO.alternativeAutoTileList.get(i).percent;
				if (rnd < d) {
					retval = selectedAutoTileVO.alternativeAutoTileList.get(i).region;
				}
				i++;
			}
			
			if (retval == null) {
				retval = selectedAutoTileVO.alternativeAutoTileList.get(i - 1).region;
			}
		}
		return retval;
	}

	@Override
	public void updateTile(int entity) {
        if (!checkValidTile(entity)) return;
        
        MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class, tiledPlugin.getAPI().getEngine());
        if (tiledPlugin.getSelectedAutoTileName().equals(mainItemComponent.customVariables.get(TiledPlugin.ORIG_AUTO_TILE))) {
        	// we only allow an update when the auto-tiles is different
        	// firstly, it does not make any sense to randomly reselect another alternative tile
        	// secondly, when dragging it constantly reselects between the alternative, making rare tiles even rarer
        	return;
        }

        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class, tiledPlugin.getAPI().getEngine());
        if (textureRegionComponent != null && textureRegionComponent.regionName != null) {
            // there is already other tile under this one
        	String selectedAutoTileName = selectTileToDraw();
        	String region = selectedAutoTileName + TiledPlugin.AUTO_TILE_DRAW_SUFFIX;
            if (!textureRegionComponent.regionName.equals(region)) {
                replaceRegionCommandBuilder.begin(entity);
                replaceRegionCommandBuilder.setRegion(tiledPlugin.getAPI().getSceneLoader().getRm().getTextureRegion(region));
                replaceRegionCommandBuilder.setRegionName(region);
                replaceRegionCommandBuilder.execute(tiledPlugin.facade);

                mainItemComponent.tags.add(TiledPlugin.AUTO_TILE_TAG);
                mainItemComponent.setCustomVars(TiledPlugin.REGION, selectedAutoTileName);
                mainItemComponent.setCustomVars(TiledPlugin.ORIG_AUTO_TILE, tiledPlugin.getSelectedAutoTileName());
            }
        }
	}

    @Override
	protected void postProcessEntity(int entity, float x, float y, int row, int column) {
    	super.postProcessEntity(entity, x, y, row, column);

        MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class, tiledPlugin.getAPI().getEngine());
        mainItemComponent.tags.add(TiledPlugin.AUTO_TILE_TAG);
        mainItemComponent.setCustomVars(TiledPlugin.REGION, tileToDraw);
        mainItemComponent.setCustomVars(TiledPlugin.ORIG_AUTO_TILE, tiledPlugin.getSelectedAutoTileName());
    }

}
