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
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIParticleEffectsTab extends UIResourcesTab {

    private VisTable list;

    @Override
    protected VisScrollPane crateScrollPane() {
        list = new VisTable();
        return StandardWidgetsFactory.createScrollPane(list);
    }

    @Override
    public String getTabTitle() {
        return "Particles";
    }

    @Override
    public String getTabIconStyle() {
        return "particle-button";
    }

    public void setItems(Array<DraggableResource> items) {
        list.clearChildren();
        for (DraggableResource box : items) {
            box.initDragDrop();
            list.add((Actor) box.getViewComponent()).expandX().fillX();
            list.row();
        }
    }
}
