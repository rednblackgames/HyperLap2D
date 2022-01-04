/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.plugin.tiled;

import java.io.File;
import java.util.Set;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageButton;

import games.rednblack.editor.plugin.tiled.data.AutoTileVO;
import games.rednblack.editor.plugin.tiled.data.TileVO;
import games.rednblack.editor.plugin.tiled.manager.ResourcesManager;
import games.rednblack.editor.plugin.tiled.offset.OffsetPanel;
import games.rednblack.editor.plugin.tiled.offset.OffsetPanelMediator;
import games.rednblack.editor.plugin.tiled.save.DataToSave;
import games.rednblack.editor.plugin.tiled.save.SaveDataManager;
import games.rednblack.editor.plugin.tiled.tools.DeleteTileTool;
import games.rednblack.editor.plugin.tiled.tools.DrawTileTool;
import games.rednblack.editor.plugin.tiled.view.dialog.AlternativeAutoTileDialogMediator;
import games.rednblack.editor.plugin.tiled.view.dialog.ImportTileSetDialogMediator;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.MenuAPI;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.plugins.H2DPluginAdapter;
import net.mountainblade.modular.annotations.Implementation;

/**
 * Created by mariam on 2/2/2016.
 */
@Implementation(authors = "azakhary", version = "0.0.1")
public class TiledPlugin extends H2DPluginAdapter {

    //-------notifications---------//
    public static final String CLASS_NAME = "games.rednblack.editor.plugin.tiled";
    public static final String TILE_ADDED                     = CLASS_NAME + ".TILE_ADDED";
    public static final String TILE_SELECTED                  = CLASS_NAME + ".TILE_SELECTED";
    public static final String AUTO_TILE_SELECTED             = CLASS_NAME + ".AUTO_TILE_SELECTED";
    public static final String AUTO_FILL_TILES                 = CLASS_NAME + ".FILL_AUTO_TILE";
    public static final String OPEN_DROP_DOWN                 = CLASS_NAME + ".OPEN_DROP_DOWN";
    public static final String AUTO_OPEN_DROP_DOWN            = CLASS_NAME + ".AUTO_OPEN_DROP_DOWN";
    public static final String GRID_CHANGED                   = CLASS_NAME + ".GRID_CHANGED";
    public static final String IMPORT_TILESET_PANEL_OPEN      = CLASS_NAME + ".IMPORT_TILESET_PANEL_OPEN";
    public static final String ACTION_DELETE_TILE             = CLASS_NAME + ".ACTION_DELETE_TILE";
    public static final String ACTION_DELETE_AUTO_TILE        = CLASS_NAME + ".ACTION_DELETE_AUTO_TILE";
    public static final String ACTION_DELETE_TILE_ALL         = CLASS_NAME + ".ACTION_DELETE_TILE_ALL";
    public static final String ACTION_SET_OFFSET              = CLASS_NAME + ".ACTION_SET_OFFSET";
    public static final String ACTION_OPEN_OFFSET_PANEL       = CLASS_NAME + ".ACTION_OPEN_OFFSET_PANEL";
    public static final String TILE_GRID_OFFSET_ADDED         = CLASS_NAME + ".TILE_GRID_OFFSET_ADDED";
    public static final String ACTION_SET_GRID_SIZE_FROM_ITEM = CLASS_NAME + ".ACTION_SET_GRID_SIZE_FROM_ITEM";
    public static final String ACTION_SET_GRID_SIZE_FROM_LIST = CLASS_NAME + ".ACTION_SET_GRID_SIZE_FROM_LIST";
    public static final String ACTION_SAVE_ALTERNATIVES_AUTO_TILE = CLASS_NAME + ".ACTION_SAVE_ALTERNATIVES_AUTO_TILE";
    public static final String ACTION_SETUP_ALTERNATIVES_AUTO_TILE = CLASS_NAME + ".ACTION_SETUP_ALTERNATIVES_AUTO_TILE";
    public static final String ACTION_RECALC_PERCENT_ALTERNATIVES_AUTO_TILE = CLASS_NAME + ".ACTION_RECALC_PERCENT_ALTERNATIVES_AUTO_TILE";
    //-------end--------//

    public static final String TILE_TAG = "TILE";
    public static final String ROW = "ROW";
    public static final String COLUMN = "COLUMN";
    public static final String REGION = "REGION";
    public static final String ORIG_AUTO_TILE = "ORIG_AUTO_TILE";

    public static final String AUTO_TILE_TAG = "AUTO_TILE";
    public static final String AUTO_TILE_ATLAS_SUFFIX = "-autotile";
    public static final String AUTO_TILE_MINI_SUFFIX = "-mini";
	public static final String AUTO_TILE_DRAW_SUFFIX = "18";

	public static final int AUTO_TILE_ROWS = 5;
	public static final int AUTO_TILE_COLS = 11;

    public DataToSave dataToSave;
    public SaveDataManager saveDataManager;
    public boolean isSceneLoaded = false;
    public DrawTileTool drawTileTool;
    public DeleteTileTool deleteTileTool;
    public ResourcesManager pluginRM;
    public OffsetPanel offsetPanel;

    private TileVO selectedTileVO;
    private AutoTileVO selectedAutoTileVO;
    private ObjectMap<String, String> currentEntityCustomVariables;
    private MainItemComponent currentEntityMainItemComponent;
    private TransformComponent currentEntityTransformComponent;

    private boolean isAutoGridTabSelected;

    public TiledPlugin() {
        super(CLASS_NAME);
        selectedTileVO = new TileVO();
        selectedAutoTileVO = new AutoTileVO();
        currentEntityCustomVariables = new ObjectMap<>();
    }

    @Override
    public void initPlugin() {
        facade.registerMediator(new TiledPanelMediator(this));
        facade.registerMediator(new ImportTileSetDialogMediator(pluginAPI, facade));
        facade.registerMediator(new AlternativeAutoTileDialogMediator(this));

        pluginRM = new ResourcesManager(this);
        offsetPanel = new OffsetPanel(this);

        facade.registerMediator(new OffsetPanelMediator(this));

        initTools();

        Skin skin = VisUI.getSkin();
        VisImageButton.VisImageButtonStyle tileAddButtonStyle = new VisImageButton.VisImageButtonStyle();
        tileAddButtonStyle.up = skin.getDrawable("toolbar-normal");
        tileAddButtonStyle.down = skin.getDrawable("toolbar-down");
        tileAddButtonStyle.checked = skin.getDrawable("toolbar-down");
        tileAddButtonStyle.over = skin.getDrawable("toolbar-over");
        tileAddButtonStyle.imageUp = new TextureRegionDrawable(pluginRM.getTextureRegion("tool-tilebrush", -1));
        pluginAPI.addTool(DrawTileTool.NAME, tileAddButtonStyle, true, drawTileTool);

        VisImageButton.VisImageButtonStyle tileDeleteButtonStyle = new VisImageButton.VisImageButtonStyle();
        tileDeleteButtonStyle.up = skin.getDrawable("toolbar-normal");
        tileDeleteButtonStyle.down = skin.getDrawable("toolbar-down");
        tileDeleteButtonStyle.checked = skin.getDrawable("toolbar-down");
        tileDeleteButtonStyle.over = skin.getDrawable("toolbar-over");
        tileDeleteButtonStyle.imageUp = new TextureRegionDrawable(pluginRM.getTextureRegion("tool-tileeraser", -1));
        pluginAPI.addTool(DeleteTileTool.NAME, tileDeleteButtonStyle, false, deleteTileTool);

        pluginAPI.setDropDownItemName(ACTION_SET_GRID_SIZE_FROM_ITEM, "Set tile grid size");

        pluginAPI.addMenuItem(MenuAPI.RESOURCE_MENU, "Import Tile Set...", IMPORT_TILESET_PANEL_OPEN);

        facade.sendNotification(MsgAPI.ADD_RESOURCES_BOX_FILTER, new TilesResourceFilter(this));
    }

    @Override
    public void onDropDownOpen(Set<Integer> selectedEntities, Array<String> actionsSet) {
        if(selectedEntities.size() == 1) {
            actionsSet.add(ACTION_SET_GRID_SIZE_FROM_ITEM);
        }
    }

    public void initSaveData() {
        saveDataManager = new SaveDataManager(pluginAPI.getProjectPath());
        dataToSave = saveDataManager.dataToSave;
    }

    private void initTools() {
        drawTileTool = new DrawTileTool(this);
        deleteTileTool = new DeleteTileTool(this);
    }

    public int getPluginEntityWithParams(int row, int column) {
        for (int entity : pluginAPI.getProjectEntities()) {
            if(!isTile(entity)) continue;
            boolean isEntityVisible = pluginAPI.isEntityVisible(entity);
            if (!isEntityVisible || !isOnCurrentSelectedLayer(entity)) continue;

            currentEntityMainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class, getAPI().getEngine());
            currentEntityCustomVariables = currentEntityMainItemComponent.customVariables;
            if (Integer.parseInt(currentEntityCustomVariables.get(ROW)) == row
                    && Integer.parseInt(currentEntityCustomVariables.get(COLUMN)) == column) {
                return entity;
            }
        }
        return -1;
    }

    public int getPluginEntityWithCoords(float x, float y) {
        for (int entity : pluginAPI.getProjectEntities()) {
            if (!isTile(entity)) continue;
            boolean isEntityVisible = pluginAPI.isEntityVisible(entity);
            if (!isEntityVisible || !isOnCurrentSelectedLayer(entity)) continue;

            currentEntityTransformComponent = ComponentRetriever.get(entity, TransformComponent.class, getAPI().getEngine());
            Rectangle tmp = new Rectangle(
                    currentEntityTransformComponent.x,
                    currentEntityTransformComponent.y,
                    dataToSave.getParameterVO().gridWidth,
                    dataToSave.getParameterVO().gridHeight);

            if (tmp.contains(x, y)) {
                return entity;
            }
        }
        return -1;
    }

    public float getPixelToWorld() {
        return pluginAPI.getSceneLoader().getRm().getProjectVO().pixelToWorld;
    }

    public boolean isTile(int entity) {
        if (entity == -1)
            return false;
        return ComponentRetriever.get(entity, MainItemComponent.class, getAPI().getEngine()).tags.contains(TILE_TAG);
    }

    public boolean isOnCurrentSelectedLayer(int entity) {
        ZIndexComponent entityZComponent = ComponentRetriever.get(entity, ZIndexComponent.class, getAPI().getEngine());
        return entityZComponent.layerName.equals(pluginAPI.getCurrentSelectedLayerName());
    }

    public void setSelectedTileName (String regionName) {
        selectedTileVO.regionName = regionName;
    }

    public String getSelectedTileName() {
    	if (isAutoGridTabSelected) {
    		return selectedAutoTileVO.regionName;
    	}
        return selectedTileVO.regionName;
    }
    
    /**
     * Explicitly returns the selected auto tile name, regardless of whether the auto grid tab is shown or any other.
     * 
     * @return The selected auto tile name.
     */
    public String getSelectedAutoTileName() {
    	return selectedAutoTileVO.regionName;
    }

    public int getSelectedTileType() {
        return selectedTileVO.entityType;
    }

    public Vector2 getSelectedTileGridOffset() {
        return selectedTileVO.gridOffset;
    }

    public void setSelectedTileGridOffset (Vector2 gridOffset) {
        selectedTileVO.gridOffset = gridOffset;
    }

    public TileVO getSelectedTileVO() {
        return selectedTileVO;
    }

    public void setSelectedTileVO(TileVO selectedTileVO) {
        this.selectedTileVO = selectedTileVO;
    }

    public AutoTileVO getSelectedAutoTileVO() {
        return selectedAutoTileVO;
    }

    public void setSelectedAutoTileVO(AutoTileVO selectedAutoTileVO) {
        this.selectedAutoTileVO = selectedAutoTileVO;
    }

    public void applySelectedTileGridOffset() {
        pluginAPI.getProjectEntities().forEach(entity -> {
            if (!(isTile(entity))) return;
            TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class, getAPI().getEngine());
            TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class, getAPI().getEngine());
            if (selectedTileVO.regionName.equals(textureRegionComponent.regionName)) {
                transformComponent.x -= selectedTileVO.gridOffset.x;
                transformComponent.y -= selectedTileVO.gridOffset.y;
            }
        });
        saveOffsetChanges();
    }

    public boolean isAutoGridTilesTabSelected() {
    	return isAutoGridTabSelected;
    }

    public void setAutoGridTilesTabSelected(boolean isAutoGridTabSelected) {
    	this.isAutoGridTabSelected = isAutoGridTabSelected;
    }

    private void saveOffsetChanges() {
        dataToSave.setTileGridOffset(selectedTileVO);
        saveDataManager.save();
    }

    public String getCurrentRawImagesPath() {
        return getAPI().getProjectPath() + File.separator + "assets" + File.separator + "orig" + File.separator + "images";
    }

}
