package games.rednblack.editor.plugin.tiled.view.tabs.listener;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.TextureRegionVO;
import games.rednblack.editor.plugin.tiled.view.tabs.AbstractGridTilesTab;

public class GridTabInputListener<T extends TextureRegionVO> extends InputListener {

	private final int index;

	private AbstractGridTilesTab<T> tab;
	private String tileSelectedNotification;
	private String openDropDownNotification;

	private TiledPlugin tiledPlugin;

	private VisTable table;

	private Array<VisImageButton> tiles;

	private boolean isDragging = false;
	private Actor draggingSource;

	private Array<T> savedTiles;

	public GridTabInputListener(AbstractGridTilesTab<T> tab, int index, TiledPlugin tiledPlugin, VisTable table, String tileSelectedNotification, String openDropDownNotification, Array<T> savedTiles, Array<VisImageButton> tiles) {
		this.tab = tab;
		this.index = index;
		this.tiledPlugin = tiledPlugin;
		this.tileSelectedNotification = tileSelectedNotification;
		this.openDropDownNotification = openDropDownNotification;
		this.table = table;
		this.savedTiles = savedTiles;
		this.tiles = tiles;
	}

	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		if (button == Input.Buttons.LEFT) {
			draggingSource = event.getListenerActor();
		}

		if (index >= savedTiles.size) return true;

		for (VisImageButton tile : tiles) {
			if (tile.isChecked()) {
				tile.setChecked(false);
			}
		}

		return true;
	}

	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		super.touchUp(event, x, y, pointer, button);
		if (isDragging && button == Input.Buttons.LEFT && draggingSource != null) {
			// finished dragging
			isDragging = false;

			handleDrop(x, y);
		} else {
			if(button == Input.Buttons.RIGHT && index < savedTiles.size) {
				tiledPlugin.facade.sendNotification(openDropDownNotification, savedTiles.get(index).getRegionName());
				return;
			}

			if (index >= savedTiles.size) {
				tiles.get(index).setChecked(false);
				return;
			}

			tiledPlugin.facade.sendNotification(tileSelectedNotification, savedTiles.get(index));
		}
	}

	@Override
	public void touchDragged (InputEvent event, float x, float y, int pointer) {
		isDragging = true;
		if (draggingSource != null) {
			// draggingSource can be null when dragging with the right mouse button
			draggingSource.setColor(new Color(0 / 255f, 0 / 255f, 0f / 255f, 0.5f));
		}
	}

	/**
	 * Handles the drop of a VisImageButton.
	 * 
	 * @param x The coordinates relative to the source. Comes from the touchUp event.
	 * @param y The coordinates relative to the source. Comes from the touchUp event.
	 */
	private void handleDrop(float x, float y) {
		Actor draggingTarget = table.hit(draggingSource.getX() + x, draggingSource.getY() + y, false);
		if (draggingTarget instanceof Image) {
			for (VisImageButton imgButton : tiles) {
				if (imgButton.getImage() == draggingTarget) {
					draggingTarget = imgButton;
					break;
				}
			}
		}
		if (draggingTarget != null) {
			String sourceRegionName = String.valueOf(draggingSource.getUserObject());
			String targetRegionName = String.valueOf(draggingTarget.getUserObject());
			int sourceIndex = -1;
			int targetIndex = -1;
			for (int i = 0; i < savedTiles.size; i++) {
				if (sourceRegionName.equals(savedTiles.get(i).getRegionName())) {
					sourceIndex = i;
				}
				if (targetRegionName.equals(savedTiles.get(i).getRegionName())) {
					targetIndex = i;
				}
			}
			// if targetIndex < 0 we dropped at the end
			// we already know that we hit another VisImageButton but could not find the name
			// thus, an empty button, which are always after the ones with an image
			if (targetIndex < 0) {
				targetIndex = savedTiles.size - 1;
			}
			if (sourceIndex >= 0) {
				T sourceTileVO = savedTiles.removeIndex(sourceIndex);
				savedTiles.insert(targetIndex, sourceTileVO);
				tab.initTiles();
				// select the dropped button
				tiledPlugin.facade.sendNotification(TiledPlugin.TILE_SELECTED, sourceTileVO);
				tiles.get(targetIndex).setChecked(true);
			}
			
		}
	}

}
