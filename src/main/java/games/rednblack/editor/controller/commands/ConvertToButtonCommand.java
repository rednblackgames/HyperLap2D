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

package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.components.LayerMapComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.additional.ButtonComponent;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;

import java.util.HashSet;

/**
 * Created by azakhary on 4/28/2015.
 */
public class ConvertToButtonCommand extends ConvertToCompositeCommand {

    @Override
    public void doAction() {
        int entity;

        HashSet<Integer> entities = (HashSet<Integer>) sandbox.getSelector().getSelectedItems();
        if (entities.size() == 0) {
            cancel();
            return;
        }

        int item = entities.iterator().next();

        if(entities.size() == 1 && EntityUtils.getType(item) == EntityFactory.COMPOSITE_TYPE) {
            entity = item;
        } else {
            super.doAction();
            entity = EntityUtils.getByUniqueId(entityId);
        }

        //create layers
        LayerMapComponent layerMapComponent = SandboxComponentRetriever.get(entity, LayerMapComponent.class);
        layerMapComponent.addLayer(new LayerItemVO("normal"));
        layerMapComponent.addLayer(new LayerItemVO("pressed"));

        // adding button logic
        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
        mainItemComponent.tags.add("button");
        sandbox.getEngine().edit(entity).create(ButtonComponent.class);
    }

    @Override
    public void undoAction() {
        super.undoAction();
    }
}
