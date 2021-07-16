package games.rednblack.editor.plugin.tiled.view.tabs;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.plugin.tiled.TiledPanel;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.AutoTileVO;
import games.rednblack.editor.plugin.tiled.view.tabs.listener.GridTabInputListener;

public class AutoGridTilesTab extends AbstractGridTilesTab<AutoTileVO> {

	public AutoGridTilesTab(TiledPanel panel, String tabTitle, int tabIndex) {
		super(panel, tabTitle, tabIndex);
	}

	@Override
	protected GridTabInputListener<AutoTileVO> getGridTabInputListener(int index) {
		return new GridTabInputListener<AutoTileVO>(this, index, tiledPlugin, (VisTable) pane.getActor(), getTileSelectedNotification(), getTiledOpenDropDownNotification(), savedTiles, tiles);
	}

    @Override
	protected Array<AutoTileVO> initSavedTiles() {
    	return tiledPlugin.dataToSave.getAutoTiles();
    }

	@Override
	protected String getTileSelectedNotification() {
		return TiledPlugin.AUTO_TILE_SELECTED;
	}

    @Override
    protected String getTiledOpenDropDownNotification() {
    	return TiledPlugin.AUTO_OPEN_DROP_DOWN;
    }

    @Override
	public void selectTile(AutoTileVO tileVO) {
        tiledPlugin.setSelectedAutoTileVO(tileVO);
    }
    
    @Override
	protected void setGridSizeToFirstTileSize(String tileName, int type) {
    	super.setGridSizeToFirstTileSize(tileName + TiledPlugin.AUTO_TILE_MINI_SUFFIX, type);
    }

	@Override
	protected AutoTileVO getTextureRegionVO(String tileName) {
		return new AutoTileVO(tileName);
	}

}
