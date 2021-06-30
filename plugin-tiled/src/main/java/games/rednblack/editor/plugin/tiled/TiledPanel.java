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

import org.puremvc.java.interfaces.IFacade;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.plugin.tiled.data.TileVO;
import games.rednblack.editor.plugin.tiled.manager.ResourcesManager;
import games.rednblack.editor.plugin.tiled.view.tabs.GridTilesTab;
import games.rednblack.editor.plugin.tiled.view.tabs.SettingsTab;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPane;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPaneListener;

/**
 * Created by mariam on 2/2/2016.
 */
public class TiledPanel extends UIDraggablePanel {

    public static final float GRID_WIDTH = 220f;
    public static final float GRID_HEIGHT = 250f;
    public static final float DROP_WIDTH = 220f;
    public static final float DROP_HEIGHT = 140f;
    public static final float SETTINGS_WIDTH = 220f;
    public static final float SETTINGS_HEIGHT = 150f;

    public TiledPlugin tiledPlugin;
    private IFacade facade;

    protected ImageTabbedPane tabbedPane;
    protected VisTable tabTable; //table inside of each tab
    protected Table paneTable; //table for 'tabs' row

    private GridTilesTab tilesTab;
    private SettingsTab settingsTab;
    private VisTable mainTable;
    private Engine engine;
    private ResourcesManager resourcesManager;

    public TiledPanel(TiledPlugin tiledPlugin) {
        super("Tiles");
        this.tiledPlugin = tiledPlugin;

        facade = tiledPlugin.facade;

        mainTable = new VisTable();
        add(mainTable)
                .padLeft(-2)
                .padRight(2);

        tabTable = new VisTable();
    }

    public void initView() {
        if (resourcesManager == null)
            this.resourcesManager = tiledPlugin.pluginRM;
        mainTable.clear();

        tabbedPane = new ImageTabbedPane();
        paneTable = tabbedPane.getTable();

        mainTable.add(paneTable).growX();
        mainTable.row();

        tabTable.clear();
        paneTable.row();
        paneTable.add(tabTable)
                .left()
                .top()
                .row();

        tabbedPane.addListener(new ImageTabbedPaneListener() {

            @Override
            public void switchedTab (ImageTab tab) {
                if (tab == null) {
                    return;
                }

                float WIDTH = 0;
                float HEIGHT = 0;
                if (tab instanceof SettingsTab) {
                    WIDTH = SETTINGS_WIDTH;
                    HEIGHT = SETTINGS_HEIGHT;
                } else if (tab instanceof GridTilesTab) {
                    if (GridTilesTab.isDrop) {
                        WIDTH = DROP_WIDTH;
                        HEIGHT = DROP_HEIGHT;
                    } else {
                        WIDTH = GRID_WIDTH;
                        HEIGHT = GRID_HEIGHT;
                    }
                }

                Table content = tab.getContentTable();

                tabTable.clearChildren();
                tabTable.add(content)
                        .width(WIDTH)
                        .height(HEIGHT)
                        .row();
                float prevHeight = getHeight();
                pack();
                float heightDiff = getHeight() - prevHeight;
                setY(getY() - heightDiff);
            }

            @Override
            public void removedTab(ImageTab tab) {

            }

            @Override
            public void removedAllTabs() {

            }

        });

        initTabs();

        pack();
    }

    public void setFixedPosition() {
        setPosition(56f, 765f - getPrefHeight());
    }

    public Table getDropTable() {
        return tilesTab.getContentTable();
    }

    public void reInitGridSettings() {
        settingsTab.resetGridCategory();
    }

    public void addTile(String tileName, int type) {
        tilesTab.addTile(tileName, type);
    }

    public void selectTile(TileVO tileVO) {
        tilesTab.selectTile(tileVO);
    }

    public void removeTile() {
        tilesTab.removeTile();
        reInitTabTable();
        tilesTab.scrollTiles();
    }
    
    public void removeAllTiles() {
    	tilesTab.removeAllTiles();
    	reInitTabTable();
    	tilesTab.scrollTiles();
    }

    private void initTabs() {
        tilesTab = new GridTilesTab(this, 0);
        tilesTab.initView();
        tabbedPane.insert(tilesTab.getTabIndex(), tilesTab);

        settingsTab = new SettingsTab(this, "Settings", 1);
        settingsTab.initView();
        tabbedPane.insert(settingsTab.getTabIndex(), settingsTab);

        reInitTabTable();
    }

    public void reInitTabTable() {
        float width = GridTilesTab.isDrop ? DROP_WIDTH : GRID_WIDTH;
        float height = GridTilesTab.isDrop ? DROP_HEIGHT : GRID_HEIGHT;
        tabTable.clear();
        tabTable.add(tilesTab.getContentTable())
                .width(width)
                .height(height);
        tabTable.pack();
        pack();
    }

    public void initLockView() {
        mainTable.clear();

        mainTable.add(new VisLabel("no scenes open")).right();
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public IFacade getFacade() {
        return facade;
    }
}
