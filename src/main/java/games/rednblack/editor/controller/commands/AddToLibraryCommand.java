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
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

import java.util.HashMap;

/**
 * Created by azakhary on 4/28/2015.
 */
public class AddToLibraryCommand extends HistoricRevertibleCommand {

    private String createdLibraryItemName;
    private CompositeItemVO overwritten;
    private String prevName;
    private Integer entityId;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();

        int item = ((int) payload[0]);
        entityId = EntityUtils.getEntityId(item);
        createdLibraryItemName = (String) payload[1];

        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(item, MainItemComponent.class);

        if(createdLibraryItemName.length() > 0) {
            ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
            HashMap<String, CompositeItemVO> libraryItems = projectManager.currentProjectInfoVO.libraryItems;

            if (libraryItems.containsKey(createdLibraryItemName)) {
                overwritten = libraryItems.get(createdLibraryItemName);
            }

            CompositeItemVO newVO = new CompositeItemVO();
            newVO.loadFromEntity(item, sandbox.getEngine(), sandbox.sceneControl.sceneLoader.getEntityFactory());
            newVO.cleanIds();
            libraryItems.put(createdLibraryItemName, newVO);

            //mark this entity as belonging to library
            mainItemComponent.libraryLink = createdLibraryItemName;
            facade.sendNotification(MsgAPI.LIBRARY_LIST_UPDATED);
        } else {
            prevName = mainItemComponent.libraryLink;
            // unlink it
            mainItemComponent.libraryLink = "";
        }
        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED);
    }

    @Override
    public void undoAction() {
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, CompositeItemVO> libraryItems = projectManager.currentProjectInfoVO.libraryItems;

        if(createdLibraryItemName.length() > 0) {
            libraryItems.remove(createdLibraryItemName);

            if (overwritten != null) {
                libraryItems.put(createdLibraryItemName, overwritten);
            }
            facade.sendNotification(MsgAPI.LIBRARY_LIST_UPDATED);
        } else {
            int entity = EntityUtils.getByUniqueId(entityId);
            MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
            mainItemComponent.libraryLink = prevName;
            facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED);
        }
    }

    public static Object payloadUnLink(int entity) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = "";

        return payload;
    }

    public static Object payloadLink(int entity, String link) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = link;

        return payload;
    }
}
