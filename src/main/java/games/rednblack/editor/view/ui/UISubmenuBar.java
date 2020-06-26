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

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.box.UICompositeHierarchy;
import games.rednblack.editor.view.ui.box.UICompositeHierarchyMediator;
import games.rednblack.editor.view.ui.box.UIGridBox;
import games.rednblack.editor.view.ui.box.UIGridBoxMediator;
import games.rednblack.editor.view.ui.box.UIResolutionBox;
import games.rednblack.editor.view.ui.box.UIResolutionBoxMediator;
import games.rednblack.editor.view.ui.box.UIZoomBox;
import games.rednblack.editor.view.ui.box.UIZoomBoxMediator;

/**
 * Created by sargis on 4/8/15.
 */
public class UISubmenuBar extends VisTable {
    private final HyperLap2DFacade facade;

    public UISubmenuBar() {
        Skin skin = VisUI.getSkin();
        facade = HyperLap2DFacade.getInstance();
        //debug();
        setBackground(skin.getDrawable("sub-menu-bg"));

        //hierarchy
        UICompositeHierarchyMediator uiCompositeHierarchyMediator = facade.retrieveMediator(UICompositeHierarchyMediator.NAME);
        UICompositeHierarchy uiCompositeHierarchy = uiCompositeHierarchyMediator.getViewComponent();
        add(uiCompositeHierarchy).left().expand().fill().padRight(6);

        //grid
        UIGridBoxMediator uiGridBoxMediator = facade.retrieveMediator(UIGridBoxMediator.NAME);
        UIGridBox uiGridBox = uiGridBoxMediator.getViewComponent();
        add(uiGridBox).padRight(8);
        //

        //grid
        UIZoomBoxMediator uiZoomBoxMediator = facade.retrieveMediator(UIZoomBoxMediator.NAME);
        UIZoomBox uiZoomBox = uiZoomBoxMediator.getViewComponent();
        add(uiZoomBox).padRight(8);

        //resolution box
        UIResolutionBoxMediator uiResolutionBoxMediator = facade.retrieveMediator(UIResolutionBoxMediator.NAME);
        UIResolutionBox uiResolutionBox = uiResolutionBoxMediator.getViewComponent();
        add(uiResolutionBox);
    }
}
