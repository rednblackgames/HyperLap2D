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

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import games.rednblack.editor.plugin.tiled.view.SpineDrawable;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.h2d.common.vo.CursorData;
import games.rednblack.editor.plugin.tiled.data.TileVO;
import games.rednblack.editor.plugin.tiled.tools.DeleteTileTool;
import games.rednblack.editor.plugin.tiled.tools.DrawTileTool;
import games.rednblack.editor.plugin.tiled.view.tabs.SettingsTab;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ResourcePayloadObject;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.HashMap;

/**
 * Created by mariam on 2/2/2016.
 */
public class TiledPanelMediator extends Mediator<TiledPanel> {
    private static final String TAG = TiledPanelMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private TiledPlugin tiledPlugin;
    private DragAndDrop.Target target;

    public TiledPanelMediator(TiledPlugin tiledPlugin) {
        super(NAME, new TiledPanel(tiledPlugin));
        this.tiledPlugin = tiledPlugin;

        viewComponent.initLockView();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.SCENE_LOADED,
                TiledPlugin.TILE_ADDED,
                TiledPlugin.TILE_SELECTED,
                TiledPlugin.ACTION_DELETE_TILE,
                TiledPlugin.ACTION_SET_GRID_SIZE_FROM_LIST,
                TiledPlugin.ACTION_SET_OFFSET,
                TiledPlugin.OPEN_DROP_DOWN,
                TiledPlugin.GRID_CHANGED,
                SettingsTab.OK_BTN_CLICKED,
                TiledPlugin.ACTION_SET_GRID_SIZE_FROM_ITEM,
                MsgAPI.ACTION_DELETE_IMAGE_RESOURCE,
                MsgAPI.TOOL_SELECTED,
                MsgAPI.ACTION_KEY_DOWN
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        String tileName;

        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                tiledPlugin.isSceneLoaded = true;

                tiledPlugin.initSaveData();
                viewComponent.initView();

                if (target != null)
                    tiledPlugin.facade.sendNotification(MsgAPI.REMOVE_TARGET, target);
                target = new DragAndDrop.Target(viewComponent.getDropTable()) {
                    @Override
                    public boolean drag(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                        return true;
                    }

                    @Override
                    public void drop(DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
                        ResourcePayloadObject resourcePayloadObject = (ResourcePayloadObject) payload.getObject();
                        int type = mapClassNameToEntityType(resourcePayloadObject.className);
                        if (type == EntityFactory.UNKNOWN_TYPE) return; //only some resources can become a tile!

                        String tileName = resourcePayloadObject.name;
                        if (tiledPlugin.dataToSave.containsTile(tileName)) return;

                        tiledPlugin.facade.sendNotification(TiledPlugin.TILE_ADDED, new Object[]{tileName, type});

                    }
                };
                tiledPlugin.facade.sendNotification(MsgAPI.ADD_TARGET, target);
                Engine engine = tiledPlugin.getAPI().getEngine();
                viewComponent.setEngine(engine);
                viewComponent.setFixedPosition();
                break;
            case TiledPlugin.TILE_ADDED:
                Object[] payload = notification.getBody();
                tileName = (String) payload[0];
                int type = (int) payload[1];
                viewComponent.addTile(tileName, type);

                tiledPlugin.dataToSave.addTile(tileName, type);
                tiledPlugin.saveDataManager.save();
                break;
            case TiledPlugin.TILE_SELECTED:
                viewComponent.selectTile(notification.getBody());
                break;
            case TiledPlugin.OPEN_DROP_DOWN:
                tileName = notification.getBody();
                HashMap<String, String> actionsSet = new HashMap<>();
                actionsSet.put(TiledPlugin.ACTION_SET_GRID_SIZE_FROM_LIST, "Set grid size");
                actionsSet.put(TiledPlugin.ACTION_DELETE_TILE, "Delete");
                actionsSet.put(TiledPlugin.ACTION_OPEN_OFFSET_PANEL, "Set offset");
                tiledPlugin.facade.sendNotification(TiledPlugin.TILE_SELECTED, tiledPlugin.dataToSave.getTile(tileName));
                tiledPlugin.getAPI().showPopup(actionsSet, tileName);
                break;
            case MsgAPI.ACTION_DELETE_IMAGE_RESOURCE:
                tileName = notification.getBody();
                tiledPlugin.facade.sendNotification(TiledPlugin.ACTION_DELETE_TILE, tileName);
                break;
            case TiledPlugin.ACTION_SET_GRID_SIZE_FROM_LIST:
                float width = 0;
                float height = 0;
                TileVO t = tiledPlugin.dataToSave.getTile(notification.getBody());
                if (t.entityType == EntityFactory.SPINE_TYPE) {
                    SpineDrawable spineDrawable = tiledPlugin.pluginRM.getSpineDrawable(t.regionName);
                    width = spineDrawable.width;
                    height = spineDrawable.height;
                } else {
                    TextureRegion r = tiledPlugin.pluginRM.getTextureRegion(t.regionName, t.entityType);
                    width = r.getRegionWidth();
                    height = r.getRegionHeight();
                }

                tiledPlugin.dataToSave.setGrid(width / tiledPlugin.getPixelToWorld(), height / tiledPlugin.getPixelToWorld());
                tiledPlugin.facade.sendNotification(TiledPlugin.GRID_CHANGED);
                break;
            case TiledPlugin.ACTION_DELETE_TILE:
                String tn = notification.getBody();
                if (!tiledPlugin.dataToSave.containsTile(tn)) return;
                tiledPlugin.dataToSave.removeTile(tn);
                tiledPlugin.saveDataManager.save();
                tiledPlugin.setSelectedTileVO(new TileVO());

                viewComponent.removeTile();
                break;
            case MsgAPI.TOOL_SELECTED:
                String body = notification.getBody();
                switch (body) {
                    case DeleteTileTool.NAME:
                    case DrawTileTool.NAME:
                        if(viewComponent.isOpen) {
                            break;
                        }

                        viewComponent.show(tiledPlugin.getAPI().getUIStage());
                        if(tiledPlugin.isSceneLoaded) {
                            viewComponent.setFixedPosition();
                        }

                        break;
                    default:
                        viewComponent.hide();
                        break;
                }
                break;
            case SettingsTab.OK_BTN_CLICKED:
                tiledPlugin.dataToSave.setParameterVO(notification.getBody());
                tiledPlugin.saveDataManager.save();
                break;
            case TiledPlugin.GRID_CHANGED:
                viewComponent.reInitGridSettings();
                tiledPlugin.saveDataManager.save();
                break;
            case TiledPlugin.ACTION_SET_GRID_SIZE_FROM_ITEM:
                Entity observable = notification.getBody();
                DimensionsComponent dimensionsComponent = ComponentRetriever.get(observable, DimensionsComponent.class);
                tiledPlugin.dataToSave.setGrid(dimensionsComponent.width, dimensionsComponent.height);
                tiledPlugin.facade.sendNotification(TiledPlugin.GRID_CHANGED);
                break;
            case MsgAPI.ACTION_KEY_DOWN:
                int keyCode = notification.getBody();
                if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                    if (keyCode == Input.Keys.B) {
                        facade.sendNotification(MsgAPI.TOOL_CLICKED, DrawTileTool.NAME);
                    }
                }
                break;
        }
    }

    private int mapClassNameToEntityType(String className) {
        if (className.endsWith(".ImageResource"))
            return EntityFactory.IMAGE_TYPE;
        else if (className.endsWith(".SpriteResource"))
            return EntityFactory.SPRITE_TYPE;
        else if (className.endsWith(".SpineResource"))
            return EntityFactory.SPINE_TYPE;

        return EntityFactory.UNKNOWN_TYPE;
    }
}
