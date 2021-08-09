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

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.h2d.common.MsgAPI;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 6/9/2015.
 */
public class CreateItemCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private int entityType;
    private String serializedEntity;

    @Override
    public void doAction() {
        int entity = getNotification().getBody();

        if (serializedEntity != null) {
            EntityFactory factory = Sandbox.getInstance().sceneControl.sceneLoader.getEntityFactory();
            int parentEntity = Sandbox.getInstance().getCurrentViewingEntity();
            entity =  EntityUtils.getEntityFromJson(serializedEntity, entityType, factory, parentEntity);
            serializedEntity = null;
        }

        entityId = EntityUtils.getEntityId(entity);
        entityType = EntityUtils.getType(entity);

        // z-index
        NodeComponent nodeComponent = SandboxComponentRetriever.get(Sandbox.getInstance().getCurrentViewingEntity(), NodeComponent.class);
        ZIndexComponent zindexComponent = SandboxComponentRetriever.get(entity, ZIndexComponent.class);
        zindexComponent.setZIndex(nodeComponent.children.size);

        sandbox.getEngine().process();
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.NEW_ITEM_ADDED, entity);

        Set<Integer> items = new HashSet<>();
        items.add(entity);
        facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, items);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        serializedEntity = EntityUtils.getJsonStringFromEntity(entity);

        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        followersUIMediator.removeFollower(entity);

        sandbox.getEngine().delete(entity);
        sandbox.getEngine().process();
        facade.sendNotification(MsgAPI.DELETE_ITEMS_COMMAND_DONE);
    }
}
