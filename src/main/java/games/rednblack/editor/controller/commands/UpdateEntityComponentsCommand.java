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

import com.artemis.Component;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.runtime.ComponentCloner;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Created by azakhary on 6/3/2015.
 */
public class UpdateEntityComponentsCommand extends EntityModifyRevertibleCommand {

    private Array<Component> backupComponents = new Array<>();
    private Integer entityId;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();

        int entity = (int) payload[0];
        entityId = EntityUtils.getEntityId(entity);
        Array<Component> components = (Array<Component>) payload[1];

        for(int i = 0; i < components.size; i++) {
            //backup the original component
            Component originalComponent = SandboxComponentRetriever.get(entity, components.get(i).getClass());
            backupComponents.add(ComponentCloner.get(originalComponent));

            //now modify the entity component from provided data
            ComponentCloner.set(originalComponent, components.get(i));
        }

        EntityUtils.refreshComponents(entity);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        for(int i = 0; i < backupComponents.size; i++) {
            Component entityComponent = SandboxComponentRetriever.get(entity, backupComponents.get(i).getClass());
            ComponentCloner.set(entityComponent, backupComponents.get(i));
        }

        EntityUtils.refreshComponents(entity);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
