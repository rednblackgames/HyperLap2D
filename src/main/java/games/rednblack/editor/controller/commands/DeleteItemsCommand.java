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

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.h2d.common.MsgAPI;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/28/2015.
 */
public class DeleteItemsCommand extends EntityModifyRevertibleCommand {

    private String backup;
    private Array<Integer> entityIdsToDelete;

    private void backup() {
        Set<Integer> entitySet = new HashSet<>();
        if(entityIdsToDelete == null) {
            entityIdsToDelete = new Array<>();
            entitySet = sandbox.getSelector().getSelectedItems();
            for(int entity: entitySet) {
                entityIdsToDelete.add(EntityUtils.getEntityId(entity));
            }
        } else {
            for(Integer entityId: entityIdsToDelete) {
                entitySet.add(EntityUtils.getByUniqueId(entityId));
            }
        }

        backup = EntityUtils.getJsonStringFromEntities(entitySet);
    }

    @Override
    public void doAction() {
        backup();

        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        for (Integer entityId : entityIdsToDelete) {
            int item = EntityUtils.getByUniqueId(entityId);
            followersUIMediator.removeFollower(item);
            sandbox.getEngine().delete(item);
        }
        sandbox.getEngine().process();

        sandbox.getSelector().getCurrentSelection().clear();

        facade.sendNotification(MsgAPI.DELETE_ITEMS_COMMAND_DONE);
    }

    @Override
    public void undoAction() {
        Json json = HyperJson.getJson();
        CompositeItemVO compositeVO = json.fromJson(CompositeItemVO.class, backup);
        compositeVO.cleanIds();
        Set<Integer> newEntitiesList = PasteItemsCommand.createEntitiesFromVO(compositeVO);

        sandbox.getEngine().process();
        for (int entity : newEntitiesList) {
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.NEW_ITEM_ADDED, entity);
        }

        sandbox.getSelector().setSelections(newEntitiesList, true);
    }

    public void setItemsToDelete(Set<Integer> entities) {
        entityIdsToDelete = new Array<>();
        for(int entity: entities) {
            entityIdsToDelete.add(EntityUtils.getEntityId(entity));
        }
    }
}
