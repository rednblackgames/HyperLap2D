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

package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIAnimationsTab extends UIResourcesTab {
    private VisTable animationsTable;

    @Override
    public String getTabTitle() {
        return "Animations";
    }

    @Override
    public String getTabIconStyle() {
        return "animation-button";
    }

    public void setThumbnailBoxes(Array<DraggableResource> draggableResources) {
        animationsTable.clearChildren();
        for (int i = 0; i < draggableResources.size; i++) {
            DraggableResource draggableResource = draggableResources.get(i);
            animationsTable.add((Actor) draggableResource.getViewComponent()).pad(4);
            if ((i - 7) % 4 == 0) {
                animationsTable.row();
            }
        }
    }

    @Override
    protected VisScrollPane crateScrollPane() {
        animationsTable = new VisTable();
        HyperLap2DFacade.getInstance().sendNotification(UIResourcesBoxMediator.ADD_RESOURCES_BOX_TABLE_SELECTION_MANAGEMENT, animationsTable);
        VisScrollPane scrollPane = StandardWidgetsFactory.createScrollPane(animationsTable);
        scrollPane.setScrollingDisabled(true, false);
        return scrollPane;
    }
}
