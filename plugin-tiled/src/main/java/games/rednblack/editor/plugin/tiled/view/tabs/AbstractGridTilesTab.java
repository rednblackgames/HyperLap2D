package games.rednblack.editor.plugin.tiled.view.tabs;

import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.plugin.tiled.TiledPanel;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.plugin.tiled.data.TextureRegionVO;
import games.rednblack.editor.plugin.tiled.manager.ResourcesManager;
import games.rednblack.editor.plugin.tiled.view.SpineDrawable;
import games.rednblack.editor.plugin.tiled.view.tabs.listener.GridTabInputListener;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.extension.spine.SpineItemType;

public abstract class AbstractGridTilesTab<T extends TextureRegionVO> extends DefaultTab {

    public boolean isDrop;

    private int tilesCount = 19;
    protected Array<VisImageButton> tiles;
    protected Array<T> savedTiles;
    private int tileIndex;
    protected VisScrollPane pane;
    private boolean isBottomEdge;

    protected TiledPlugin tiledPlugin;
    private ResourcesManager resourcesManager;

    public AbstractGridTilesTab(TiledPanel panel, String title, int tabIndex) {
        super(panel, title, tabIndex);

        tiledPlugin = panel.tiledPlugin;
        resourcesManager = tiledPlugin.pluginRM;
        tiles = new Array<>();
        savedTiles = initSavedTiles();
        tileIndex = savedTiles.size;
    }

    protected abstract Array<T> initSavedTiles();

    @Override
    public void initView() {
        if (isDrop = savedTiles.size == 0) {
            VisImageButton.VisImageButtonStyle dropBoxStyle = new VisImageButton.VisImageButtonStyle();
            dropBoxStyle.up = new TextureRegionDrawable(resourcesManager.getTextureRegion("tiles-drop-here-normal", -1));
            dropBoxStyle.imageOver = new TextureRegionDrawable(resourcesManager.getTextureRegion("tiles-drop-here-over", -1));
            VisImageButton dropRegion = new VisImageButton(dropBoxStyle);
            content.clear();
            content.add(dropRegion)
                    .center()
                    .padRight(6)
                    .padBottom(6)
                    .padTop(10)
                    .row();
            content.add(new VisLabel("Drop an image from resources box"))
                    .expandX()
                    .center()
                    .padBottom(5);
            content.pack();
        } else {
            if (tileIndex > tilesCount) {
                tilesCount = tileIndex;
            }
            initTiles();
        }
    }

    public void addTile(String tileName, int type) {
        if (pane != null) isBottomEdge = pane.isBottomEdge();
        if (tileIndex == 0) {
            setGridSizeToFirstTileSize(tileName, type);
            isDrop = false;
            panel.reInitTabTable(this);
        }
        initTiles(tileName, type);
        panel.pack();
        scrollTiles();
        tiles.get(tileIndex).setChecked(true);
        tiledPlugin.facade.sendNotification(TiledPlugin.TILE_SELECTED, getTextureRegionVO(tileName));
        tileIndex++;
    }

	public void removeTile() {
        if (pane != null) isBottomEdge = pane.isBottomEdge();
        tileIndex = --tileIndex < 0 ? 0 : tileIndex;
        tilesCount = --tilesCount < 19 ? 19 : tilesCount;
        tiles.clear();
        initView();
    }

    public void removeAllTiles() {
        if (pane != null) isBottomEdge = pane.isBottomEdge();
        tileIndex = 0;
        tilesCount = 19;
        tiles.clear();
        initView();
    }

    public void scrollTiles() {
        if(savedTiles.size + 1 >= tilesCount) {
            pane.layout();
            pane.setSmoothScrolling(!isBottomEdge);
            pane.setScrollY(100);
        }
    }

    protected void setGridSizeToFirstTileSize(String tileName, int type) {
        float width = 0;
        float height = 0;
        if (type == SpineItemType.SPINE_TYPE) {
            SpineDrawable spineDrawable = tiledPlugin.pluginRM.getSpineDrawable(tileName);
            width = spineDrawable.width;
            height = spineDrawable.height;
        } else {
            TextureRegion r = tiledPlugin.pluginRM.getTextureRegion(tileName, type);
            width = r.getRegionWidth();
            height = r.getRegionHeight();
        }
        float gridWidth = width / tiledPlugin.getPixelToWorld();
        float gridHeight = height / tiledPlugin.getPixelToWorld();
        tiledPlugin.dataToSave.setGrid(gridWidth, gridHeight);
        tiledPlugin.facade.sendNotification(TiledPlugin.GRID_CHANGED);
    }

    private void initTiles(String tileName, int type) {
        content.clear();
        tiles.clear();

        VisTable listTable = new VisTable();
        pane = StandardWidgetsFactory.createScrollPane(listTable);
        pane.setScrollingDisabled(true, false);
        content.add(pane)
                .padTop(10);
        listTable.top();

        if(tileIndex >= tilesCount && !tileName.equals("")) {
            tilesCount = tileIndex + 1;
        }

        for (int i = 0; i < tilesCount + 1; i++) {
            VisImageButton ct;
            VisImageButton.VisImageButtonStyle imageBoxStyle = new VisImageButton.VisImageButtonStyle();
            NinePatchDrawable inactive = new NinePatchDrawable(new NinePatch(resourcesManager.getPluginNinePatch("image-Box-inactive")));
            NinePatchDrawable active = new NinePatchDrawable(new NinePatch(resourcesManager.getPluginNinePatch("image-Box-active")));
            imageBoxStyle.up = inactive;
            imageBoxStyle.down = active;
            imageBoxStyle.checked = active;
            imageBoxStyle.over = active;
            Drawable tileDrawable = null;
            if (i < savedTiles.size) {
                int t =  savedTiles.get(i).getEntityType();
                if (t == SpineItemType.SPINE_TYPE) {
                    tileDrawable = resourcesManager.getSpineDrawable(savedTiles.get(i).getRegionName());
                } else {
                    tileDrawable = new TextureRegionDrawable(resourcesManager.getTextureRegion(savedTiles.get(i).getRegionName(), t));
                }
            } else if (!tileName.equals("")) {
                if (i == tileIndex) {
                    if (type == SpineItemType.SPINE_TYPE) {
                        tileDrawable = resourcesManager.getSpineDrawable(tileName);
                    } else {
                        tileDrawable = new TextureRegionDrawable(resourcesManager.getTextureRegion(tileName, type));
                    }
                }
            }
            imageBoxStyle.imageUp = tileDrawable;
            imageBoxStyle.imageDown = tileDrawable;
            imageBoxStyle.imageChecked = tileDrawable;
            imageBoxStyle.imageOver = tileDrawable;
            ct = new VisImageButton(imageBoxStyle);
            if (i < savedTiles.size) {
            	ct.setUserObject(savedTiles.get(i).getRegionName());
            }

            int index = i;
            ct.addListener(getGridTabInputListener(index));
            listTable.add(ct)
                    .width(40)
                    .height(40)
                    .pad(3);
            if((i+1) % 4 == 0) {
                listTable.row();
            }
            tiles.add(ct);
        }

        content.pack();
    }

    public void initTiles() {
        initTiles("", -1);
    }

    /**
     * Returns the newly initialized input listener for the given tile index.
     * 
     * @param index The index of the tile.
     * 
     * @return The new input listener.
     */
    protected abstract GridTabInputListener<T> getGridTabInputListener(int index);

    /**
     * Returns the notification string to send when selecting a tile.
     * 
     * @return The notification when selecting a tile.
     */
    protected abstract String getTileSelectedNotification();

    /**
     * Returns the notification string to send when opening a drop down.
     * 
     * @return The notification when opening a drop down.
     */
    protected abstract String getTiledOpenDropDownNotification();

    /**
     * Selects the given tile.
     * 
     * @param tileVO The tile to select
     */
    public abstract void selectTile(T tileVO);

    /**
     * Returns the corresponding {@link TextureRegionVO} for the given tile name.
     * 
     * @param tileName The tilename for the texture region.
     * 
     * @return The texture region.
     */
    protected abstract T getTextureRegionVO(String tileName);

}
