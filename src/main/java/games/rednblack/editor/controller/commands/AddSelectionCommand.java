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
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;

import java.util.Set;

/**
 * Created by azakhary on 5/14/2015.
 */
public class AddSelectionCommand extends RevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.AddSelectionCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    private Array<Integer> entityIds;

    @Override
    public void doAction() {
        cancel();
        if(entityIds == null) {
            Set<Integer> items = getNotification().getBody();
            entityIds = EntityUtils.getEntityId(items);
        }

        Set<Integer> items = EntityUtils.getByUniqueId(entityIds);
        Sandbox.getInstance().getSelector().addSelections(items);
        facade.sendNotification(DONE);
    }

    @Override
    public void undoAction() {
        Set<Integer> items = EntityUtils.getByUniqueId(entityIds);
        Sandbox.getInstance().getSelector().releaseSelections(items);
        facade.sendNotification(DONE);
    }

}
