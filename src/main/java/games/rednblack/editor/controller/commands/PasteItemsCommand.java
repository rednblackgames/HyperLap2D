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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.box.UILayerBoxMediator;
import games.rednblack.h2d.common.MsgAPI;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/28/2015.
 */
public class PasteItemsCommand extends EntityModifyRevertibleCommand {

    private Array<Integer> pastedEntityIds = new Array<>();

    @Override
    public void doAction() {
        Object[] payload = (Object[]) Sandbox.retrieveFromClipboard();

        UILayerBoxMediator layerBoxMediator = facade.retrieveMediator(UILayerBoxMediator.NAME);
        if(layerBoxMediator.getCurrentSelectedLayerName() == null || payload == null) {
            cancel();
            return;
        }

        Vector2 cameraPrevPosition = (Vector2) payload[0];
        Vector2 cameraCurrPosition = new Vector2(Sandbox.getInstance().getCamera().position.x,Sandbox.getInstance().getCamera().position.y);

        Vector2 diff = cameraCurrPosition.sub(cameraPrevPosition);

        Json json = HyperJson.getJson();
        CompositeItemVO compositeVO = json.fromJson(CompositeItemVO.class, (String) payload[1]);
        compositeVO.cleanIds();

        Set<Integer> newEntitiesList = createEntitiesFromVO(compositeVO);
        sandbox.getEngine().process();
        for (int entity : newEntitiesList) {
            TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
            transformComponent.x += diff.x;
            transformComponent.y += diff.y;
            ZIndexComponent zIndexComponent = SandboxComponentRetriever.get(entity, ZIndexComponent.class);
//            UILayerBoxMediator layerBoxMediator = facade.retrieveMediator(UILayerBoxMediator.NAME);
            zIndexComponent.layerName = layerBoxMediator.getCurrentSelectedLayerName();
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.NEW_ITEM_ADDED, entity);
            pastedEntityIds.add(EntityUtils.getEntityId(entity));
        }
        sandbox.getSelector().setSelections(newEntitiesList, true);
    }

    @Override
    public void undoAction() {
        FollowersUIMediator followersUIMediator = HyperLap2DFacade.getInstance().retrieveMediator(FollowersUIMediator.NAME);
        for (Integer entityId : pastedEntityIds) {
            int entity = EntityUtils.getByUniqueId(entityId);
            followersUIMediator.removeFollower(entity);
            sandbox.getEngine().delete(entity);
            sandbox.getEngine().process();
            facade.sendNotification(MsgAPI.DELETE_ITEMS_COMMAND_DONE);
        }
        pastedEntityIds.clear();
    }

    public static Set<Integer> createEntitiesFromVO(CompositeItemVO compositeVO) {
        Set<Integer> entities = new HashSet<>();

        EntityFactory factory = Sandbox.getInstance().sceneControl.sceneLoader.getEntityFactory();
        int parentEntity = Sandbox.getInstance().getCurrentViewingEntity();


        for (String key : compositeVO.content.keys()) {
            if (key.equals(CompositeItemVO.class.getName())) continue;

            Array<MainItemVO> vos = compositeVO.content.get(key);
            for (MainItemVO mainItemVO : vos) {
                int entity = factory.createEntity(parentEntity, mainItemVO);
                entities.add(entity);
            }
        }

        Array<MainItemVO> compositeVOs = compositeVO.content.get(CompositeItemVO.class.getName());
        if (compositeVOs != null) {
            for (MainItemVO mainItemVO : compositeVOs) {
                CompositeItemVO compositeItemVO = (CompositeItemVO) mainItemVO;
                int composite = factory.createEntity(parentEntity, compositeItemVO);
                entities.add(composite);
                factory.initAllChildren(composite, compositeItemVO);
            }
        }

        return entities;
    }
}
