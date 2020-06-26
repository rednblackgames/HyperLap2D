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

package games.rednblack.editor.view.ui;

import com.badlogic.gdx.Gdx;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.menu.HyperLap2DMenuBar;
import games.rednblack.editor.view.menu.HyperLap2DMenuBarMediator;
import games.rednblack.editor.view.ui.box.UIAlignBox;
import games.rednblack.editor.view.ui.box.UIAlignBoxMediator;
import games.rednblack.editor.view.ui.box.UIItemsTreeBox;
import games.rednblack.editor.view.ui.box.UIItemsTreeBoxMediator;
import games.rednblack.editor.view.ui.box.UILayerBox;
import games.rednblack.editor.view.ui.box.UILayerBoxMediator;
import games.rednblack.editor.view.ui.box.UIMultiPropertyBox;
import games.rednblack.editor.view.ui.box.UIMultiPropertyBoxMediator;
import games.rednblack.editor.view.ui.box.UIResourcesBox;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.box.UIToolBox;
import games.rednblack.editor.view.ui.box.UIToolBoxMediator;
import games.rednblack.editor.view.ui.widget.H2DLogo;

/**
 * Created by sargis on 9/10/14.
 */
public class UIMainTable extends VisTable {
    private final VisTable topTable;
    private final VisTable middleTable;
    private final HyperLap2DFacade facade;

    //TODO: fuck! make this private!!!!
    public UISubmenuBar compositePanel;
    public UIItemsTreeBox itemsBox;

    public UIMainTable() {
        facade = HyperLap2DFacade.getInstance();
        //debug();
        setFillParent(true);
        top();
        topTable = new VisTable();
//        topTable.debug();
        middleTable = new VisTable();
//        middleTable.debug();
        add(topTable).fillX().expandX();
        row();
        add(middleTable).fillX().padTop(1);
        //
        initMenuBar();
        topTable.row();
        initCompisitePanel();
        initToolsPanel();
        initLeftBoxesPanel();
        initRightBoxesPanel();
    }

    private void initLeftBoxesPanel() {
        VisTable leftBoxesPanel = new VisTable();
        UIAlignBoxMediator uiAlignBoxMediator = facade.retrieveMediator(UIAlignBoxMediator.NAME);
        UIAlignBox uiAlignBox = uiAlignBoxMediator.getViewComponent();
        leftBoxesPanel.add(uiAlignBox).expandX().fillX();
        leftBoxesPanel.row();
        UIItemsTreeBoxMediator uiItemsTreeBoxMediator = facade.retrieveMediator(UIItemsTreeBoxMediator.NAME);
        itemsBox = uiItemsTreeBoxMediator.getViewComponent();
        leftBoxesPanel.add(itemsBox).expandX().fillX().maxHeight(600).top();
        middleTable.add(leftBoxesPanel).top().left().expand().padTop(15).padLeft(16);
    }

    private void initCompisitePanel() {
        compositePanel = new UISubmenuBar();
        topTable.add(compositePanel).fillX().expandX().colspan(2).height(32);
    }

    private void initRightBoxesPanel() {
        VisTable rightPanel = new VisTable();

        //PropertyBox
        UIMultiPropertyBoxMediator multiPropertyBoxMediator = facade.retrieveMediator(UIMultiPropertyBoxMediator.NAME);
        UIMultiPropertyBox multiPropertyBox = multiPropertyBoxMediator.getViewComponent();
        rightPanel.add(multiPropertyBox).top();
        rightPanel.row();

        //ResourcesBox
        UIResourcesBoxMediator resourceBoxMediator = facade.retrieveMediator(UIResourcesBoxMediator.NAME);
        UIResourcesBox resourceBox = resourceBoxMediator.getViewComponent();
        rightPanel.add(resourceBox).top();
        rightPanel.row();

        //LayerBox
        UILayerBoxMediator layerBoxMediator = facade.retrieveMediator(UILayerBoxMediator.NAME);
        UILayerBox layerBox = layerBoxMediator.getViewComponent();
        rightPanel.add(layerBox).top();

        //
        middleTable.add(rightPanel).top().right().expand().padTop(15);
    }

    private void initToolsPanel() {
        //
        VisTable toolsPanel = new VisTable();
        toolsPanel.background("toolbar-bg");
        //
        UIToolBoxMediator uiToolBoxMediator = facade.retrieveMediator(UIToolBoxMediator.NAME);
        UIToolBox uiToolBox = uiToolBoxMediator.getViewComponent();
        toolsPanel.add(uiToolBox).top().expandY().padTop(4);
        //
        middleTable.add(toolsPanel).top().left().width(40).height(Gdx.graphics.getHeight()).expandY();
    }


    private void initMenuBar() {
        HyperLap2DMenuBarMediator hyperlap2DMenuBarMediator = facade.retrieveMediator(HyperLap2DMenuBarMediator.NAME);
        HyperLap2DMenuBar menuBar = hyperlap2DMenuBarMediator.getViewComponent();
        topTable.add(new H2DLogo()).left().fillY();
        topTable.add(menuBar.getTable().padLeft(0)).fillX().height(32).expandX();
    }
}
