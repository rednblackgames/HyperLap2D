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
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ZIndexComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.systems.LayerSystem;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.proxy.LayerSelectionProxy;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/28/2015.
 */
public class PasteItemsCommand extends EntityModifyRevertibleCommand {

    private final Array<String> pastedEntityIds = new Array<>();

    @Override
    public void doAction() {
        Object[] payload = (Object[]) Sandbox.retrieveFromClipboard();

        if(LayerSelectionProxy.get(facade).getCurrentLayerName() == null || payload == null) {
            cancel();
            return;
        }

        Vector2 cameraPrevPosition = (Vector2) payload[0];
        Vector2 cameraCurrPosition = new Vector2(PluginUIBridge.get().getSandbox().getCamera().position.x,PluginUIBridge.get().getSandbox().getCamera().position.y);

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
            zIndexComponent.setLayerName(LayerSelectionProxy.get(facade).getCurrentLayerName());
            PluginUIBridge.get().getSandbox().getEngine().getSystem(LayerSystem.class).process();
            Facade.getInstance().sendNotification(MsgAPI.NEW_ITEM_ADDED, entity);
            pastedEntityIds.add(EntityUtils.getEntityId(entity));
        }

        facade.sendNotification(MsgAPI.ACTION_SET_SELECTION, SelectionPayload.multiple(newEntitiesList));
    }

    @Override
    public void undoAction() {
        for (String entityId : pastedEntityIds) {
            int entity = EntityUtils.getByUniqueId(entityId);
            facade.sendNotification(MsgAPI.FOLLOWER_REMOVED, entity);
            sandbox.getEngine().delete(entity);
            sandbox.getEngine().process();
            facade.sendNotification(MsgAPI.DELETE_ITEMS_COMMAND_DONE);
        }
        pastedEntityIds.clear();
    }

    public static Set<Integer> createEntitiesFromVO(CompositeItemVO compositeVO) {
        Set<Integer> entities = new HashSet<>();

        EntityFactory factory = PluginUIBridge.get().getSandbox().getSceneControl().sceneLoader.getEntityFactory();
        int parentEntity = PluginUIBridge.get().getSandbox().getCurrentViewingEntity();


        for (String key : compositeVO.content.keys()) {
            if (key.equals(HyperJson.getJson().getTag(CompositeItemVO.class))) continue;

            Array<MainItemVO> vos = compositeVO.content.get(key);
            for (MainItemVO mainItemVO : vos) {
                int entity = factory.createEntity(parentEntity, mainItemVO);
                entities.add(entity);
            }
        }

        Array<MainItemVO> compositeVOs = compositeVO.content.get(HyperJson.getJson().getTag(CompositeItemVO.class));
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
