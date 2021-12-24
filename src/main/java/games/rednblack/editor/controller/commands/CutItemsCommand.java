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

import com.badlogic.gdx.utils.Json;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.utils.HyperJson;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;

import java.util.Set;

/**
 * Created by azakhary on 4/28/2015.
 */
public class CutItemsCommand extends EntityModifyRevertibleCommand {

    private String backup;

    @Override
    public void doAction() {
        backup = EntityUtils.getJsonStringFromEntities(sandbox.getSelector().getSelectedItems());
        String data = EntityUtils.getJsonStringFromEntities(sandbox.getSelector().getSelectedItems());

        Sandbox.copyToClipboard(data);
        sandbox.getSelector().removeCurrentSelectedItems();

        facade.sendNotification(MsgAPI.DELETE_ITEMS_COMMAND_DONE);
    }

    @Override
    public void undoAction() {
        Json json = HyperJson.getJson();
        CompositeItemVO compositeVO = json.fromJson(CompositeItemVO.class, backup);
        Set<Integer> newEntitiesList = PasteItemsCommand.createEntitiesFromVO(compositeVO);

        sandbox.getEngine().process();
        for (int entity : newEntitiesList) {
            HyperLap2DFacade.getInstance().sendNotification(MsgAPI.NEW_ITEM_ADDED, entity);
        }

        sandbox.getSelector().setSelections(newEntitiesList, true);
    }
}
