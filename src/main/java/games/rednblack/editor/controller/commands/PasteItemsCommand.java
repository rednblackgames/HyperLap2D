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

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.data.CompositeVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.FollowersUIMediator;
import games.rednblack.editor.view.ui.box.UILayerBoxMediator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/28/2015.
 */
public class PasteItemsCommand extends EntityModifyRevertibleCommand {

    private Array<Integer> pastedEntityIds = new Array<>();

    @Override
    public void doAction() {
        Object[] payload = (Object[]) Sandbox.getInstance().retrieveFromClipboard();

        UILayerBoxMediator layerBoxMediator = facade.retrieveMediator(UILayerBoxMediator.NAME);
        if(layerBoxMediator.getCurrentSelectedLayerName() == null || payload == null) {
            cancel();
            return;
        }

        Vector2 cameraPrevPosition = (Vector2) payload[0];
        Vector2 cameraCurrPosition = new Vector2(Sandbox.getInstance().getCamera().position.x,Sandbox.getInstance().getCamera().position.y);

        Vector2 diff = cameraCurrPosition.sub(cameraPrevPosition);

        Json json =  new Json();
        CompositeVO compositeVO = json.fromJson(CompositeVO.class, (String) payload[1]);
        forceIdChange(compositeVO);
        Set<Entity> newEntitiesList = createEntitiesFromVO(compositeVO);
        for (Entity entity : newEntitiesList) {
            TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
            transformComponent.x += diff.x;
            transformComponent.y += diff.y;
            ZIndexComponent zIndexComponent = ComponentRetriever.get(entity, ZIndexComponent.class);
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
            Entity entity = EntityUtils.getByUniqueId(entityId);
            followersUIMediator.removeFollower(entity);
            sandbox.getEngine().removeEntity(entity);
            facade.sendNotification(MsgAPI.DELETE_ITEMS_COMMAND_DONE);
        }
    }

    public static void forceIdChange(CompositeVO compositeVO) {
        ArrayList<MainItemVO> items = compositeVO.getAllItems();
        for(MainItemVO item: items) {
            item.uniqueId = -1;
        }
    }


    public static Set<Entity> createEntitiesFromVO(CompositeVO compositeVO) {
        Set<Entity> entities = new HashSet<>();

        EntityFactory factory = Sandbox.getInstance().sceneControl.sceneLoader.getEntityFactory();
        Entity parentEntity = Sandbox.getInstance().getCurrentViewingEntity();

        for (int i = 0; i < compositeVO.sImages.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sImages.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
        }
        for (int i = 0; i < compositeVO.sImage9patchs.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sImage9patchs.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
        }
        for (int i = 0; i < compositeVO.sLabels.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sLabels.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
        }
        for (int i = 0; i < compositeVO.sParticleEffects.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sParticleEffects.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
        }
        for (int i = 0; i < compositeVO.sLights.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sLights.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
        }
        for (int i = 0; i < compositeVO.sSpineAnimations.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sSpineAnimations.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
        }
        for (int i = 0; i < compositeVO.sSpriteAnimations.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sSpriteAnimations.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
        }
        for (int i = 0; i < compositeVO.sColorPrimitives.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sColorPrimitives.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
        }
        for (int i = 0; i < compositeVO.sComposites.size(); i++) {
            Entity child = factory.createEntity(parentEntity, compositeVO.sComposites.get(i));
            Sandbox.getInstance().getEngine().addEntity(child);
            entities.add(child);
            factory.initAllChildren(Sandbox.getInstance().getEngine(), child, compositeVO.sComposites.get(i).composite);
        }

        return entities;
    }
}
