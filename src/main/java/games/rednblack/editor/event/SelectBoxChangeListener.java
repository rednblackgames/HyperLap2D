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

package games.rednblack.editor.event;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import games.rednblack.editor.HyperLap2DFacade;

/**
 * Created by azakhary on 4/16/2015.
 */
public class SelectBoxChangeListener extends ChangeListener {

    private final String eventName;

    private String lastSelected = "";

    public SelectBoxChangeListener(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public void changed(ChangeEvent changeEvent, Actor actor) {
        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
        String selected = (String) ((VisSelectBox) actor).getSelected();
        if(!lastSelected.equals(selected)) {
            lastSelected = selected;
            facade.sendNotification(eventName, selected);
        }
    }
}
