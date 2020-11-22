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

package games.rednblack.editor.view.ui.box;

import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPane;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTabbedPaneListener;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIResourcesBox extends UICollapsibleBox {
    private HyperLap2DFacade facade;

    private VisTable contentTable;
    private VisTable tabContent;

    private ImageTabbedPane tabbedPane;

    public UIResourcesBox() {
        super("Resources");
        facade = HyperLap2DFacade.getInstance();

        setMovable(false);
        contentTable = new VisTable();
        tabContent = new VisTable();
        tabbedPane = new ImageTabbedPane();
        tabbedPane.addListener(new ImageTabbedPaneListener() {
            @Override
            public void switchedTab(ImageTab tab) {
                if (tab == null) return;
                setActiveTabContent(tab);
            }

            @Override
            public void removedTab(ImageTab tab) {

            }

            @Override
            public void removedAllTabs() {

            }
        });
        contentTable.add(tabbedPane.getTable()).expandX().fillX().growX().padTop(8);
        contentTable.row();
        contentTable.add(tabContent).expandX().width(BOX_DEFAULT_WIDTH);
        contentTable.row();
        createCollapsibleWidget(contentTable);
    }

    public void setActiveTabContent(ImageTab tab) {
        tabContent.clear();
        tabContent.add(tab.getContentTable()).expandX().fillX();
    }

    public void addTab(int index, ImageTab tab) {
        tabbedPane.insert(index, tab);
    }
}
