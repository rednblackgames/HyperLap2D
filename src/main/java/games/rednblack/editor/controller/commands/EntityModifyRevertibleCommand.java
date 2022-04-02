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
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;

import java.util.HashMap;

/**
 * Created by azakhary on 6/15/2015.
 */
public abstract class EntityModifyRevertibleCommand extends HistoricRevertibleCommand {

    @Override
    public void callDoAction() {
        super.callDoAction();
        postChange();
    }
    @Override
    public void callUndoAction() {
        super.callUndoAction();
        postChange();
    }

    protected void postChange() {
        Integer parentId = EntityUtils.getEntityId(sandbox.getCurrentViewingEntity());
        int entity = EntityUtils.getByUniqueId(parentId);

        // Update item library data if it was in library
        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
        String link = mainItemComponent.libraryLink;

        if(link != null && link.length() > 0) {
            ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
            HashMap<String, CompositeItemVO> libraryItems = projectManager.currentProjectInfoVO.libraryItems;
            if (libraryItems.containsKey(mainItemComponent.libraryLink)) {
                CompositeItemVO itemVO = new CompositeItemVO();
                itemVO.loadFromEntity(entity, sandbox.getEngine(), sandbox.sceneControl.sceneLoader.getEntityFactory());
                itemVO.cleanIds();
                libraryItems.put(mainItemComponent.libraryLink, itemVO);
            }

            Array<Integer> linkedEntities = EntityUtils.getByLibraryLink(link);
            for (int dependable : linkedEntities) {
                if(dependable == entity) continue;
                NodeComponent nodeComponent = SandboxComponentRetriever.get(dependable, NodeComponent.class);
                for(int child: nodeComponent.children) {
                    sandbox.getEngine().delete(child);
                }
                nodeComponent.children.clear();
                sandbox.getEngine().process();

                EntityFactory factory = sandbox.getSceneControl().sceneLoader.getEntityFactory();
                factory.initAllChildren(dependable, libraryItems.get(link));
            }
        }
    }
}
