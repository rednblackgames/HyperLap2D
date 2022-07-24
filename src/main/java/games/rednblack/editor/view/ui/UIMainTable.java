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

import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.HyperLap2DUtils;
import games.rednblack.editor.view.menu.HyperLap2DMenuBar;
import games.rednblack.editor.view.menu.HyperLap2DMenuBarMediator;
import games.rednblack.editor.view.ui.box.*;
import games.rednblack.editor.view.ui.widget.H2DLogo;
import org.apache.commons.lang3.SystemUtils;

public class UIMainTable extends VisTable {
    private final VisTable topTable, middleTable;
    private final HyperLap2DFacade facade;

	public UIMainTable() {
        facade = HyperLap2DFacade.getInstance();

        setFillParent(true);
        top();
        topTable = new VisTable();
        middleTable = new VisTable();
        add(topTable).fillX().expandX();
        row();
        add(middleTable).fillX().growY().padTop(1);

        initMenuBar();
        topTable.row();
		initSupportMenus();
        initToolsPanel();
        initLeftBoxesPanel();
        initRightBoxesPanel();
    }

	private void initMenuBar() {
		topTable.add(new H2DLogo()).left().fillY();

        HyperLap2DMenuBarMediator hyperlap2DMenuBarMediator = facade.retrieveMediator(HyperLap2DMenuBarMediator.NAME);
        HyperLap2DMenuBar menuBar = hyperlap2DMenuBarMediator.getViewComponent();

		if (SystemUtils.IS_OS_WINDOWS) {
            topTable.add(menuBar.getTable()).height(32);

            UIWindowTitleMediator uiWindowTitleMediator = facade.retrieveMediator(UIWindowTitleMediator.NAME);
            UIWindowTitle uiWindowTitle = uiWindowTitleMediator.getViewComponent();
            topTable.add(uiWindowTitle).growX().fillY();

            UIWindowActionMediator uiWindowActionMediator = facade.retrieveMediator(UIWindowActionMediator.NAME);
            UIWindowAction uiWindowAction = uiWindowActionMediator.getViewComponent();
            topTable.add(uiWindowAction).padTop(-1).fillY();
        } else if (SystemUtils.IS_OS_MAC) {
            topTable.add(menuBar.getTable()).height(32);

            UIWindowTitleMediator uiWindowTitleMediator = facade.retrieveMediator(UIWindowTitleMediator.NAME);
            UIWindowTitle uiWindowTitle = uiWindowTitleMediator.getViewComponent();
            HyperLap2DUtils.setWindowDragListener(uiWindowTitle);
            topTable.add(uiWindowTitle).growX().fillY();
        } else {
            topTable.add(menuBar.getTable()).growX().height(32);
        }
	}

    private void initSupportMenus() {
		UISubmenuBar compositePanel = new UISubmenuBar();
        topTable.add(compositePanel).fillX().colspan(topTable.getChildren().size).expandX().height(32);
    }

	private void initLeftBoxesPanel() {
		//Align
		VisTable leftBoxesPanel = new VisTable();
		UIAlignBoxMediator uiAlignBoxMediator = facade.retrieveMediator(UIAlignBoxMediator.NAME);
		UIAlignBox uiAlignBox = uiAlignBoxMediator.getViewComponent();
		leftBoxesPanel.add(uiAlignBox).expandX().fillX();
		leftBoxesPanel.row();

		//TreeView
		UIItemsTreeBoxMediator uiItemsTreeBoxMediator = facade.retrieveMediator(UIItemsTreeBoxMediator.NAME);
		UIItemsTreeBox itemsBox = uiItemsTreeBoxMediator.getViewComponent();
		leftBoxesPanel.add(itemsBox).fillX().maxHeight(620).top();
		middleTable.add(leftBoxesPanel).top().left().expand().padTop(15).padLeft(16);
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

        middleTable.add(rightPanel).top().right().expand().padTop(15);
    }

    private void initToolsPanel() {
        VisTable toolsPanel = new VisTable();
        toolsPanel.background("toolbar-bg");
        //
        UIToolBoxMediator uiToolBoxMediator = facade.retrieveMediator(UIToolBoxMediator.NAME);
        UIToolBox uiToolBox = uiToolBoxMediator.getViewComponent();
        toolsPanel.add(uiToolBox).top().expandY().padTop(4);
        //
        middleTable.add(toolsPanel).top().left().width(40).fillY().expandY();
    }
}
