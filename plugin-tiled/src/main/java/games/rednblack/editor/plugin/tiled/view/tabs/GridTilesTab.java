package games.rednblack.editor.plugin.tiled.view.tabs;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.plugin.tiled.TiledPanel;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.TileVO;
import games.rednblack.editor.plugin.tiled.view.tabs.listener.GridTabInputListener;

/**
 * Created by mariam on 2/11/16.
 */
public class GridTilesTab extends AbstractGridTilesTab<TileVO> {

    public GridTilesTab(TiledPanel panel, int tabIndex) {
    	super(panel, "Tiles", tabIndex);
    }

    @Override
	protected Array<TileVO> initSavedTiles() {
    	return tiledPlugin.dataToSave.getTiles();
    }

    @Override
	protected GridTabInputListener<TileVO> getGridTabInputListener(int index) {
    	return new GridTabInputListener<TileVO>(this, index, tiledPlugin, (VisTable) pane.getActor(), getTileSelectedNotification(), getTiledOpenDropDownNotification(), savedTiles, tiles);
    }

    @Override
	protected String getTileSelectedNotification() {
    	return TiledPlugin.TILE_SELECTED;
    }

    @Override
    protected String getTiledOpenDropDownNotification() {
    	return TiledPlugin.OPEN_DROP_DOWN;
    }

    @Override
	public void selectTile(TileVO tileVO) {
        tiledPlugin.setSelectedTileVO(tileVO);
    }

	@Override
	protected TileVO getTextureRegionVO(String tileName) {
		return new TileVO(tileName);
	}

}
