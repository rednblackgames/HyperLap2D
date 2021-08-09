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
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by azakhary on 5/14/2015.
 */
public class SetSelectionCommand extends RevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.SetSelectionCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    private Array<Integer> previousSelectionIds;

    @Override
    public void doAction() {
        cancel();
        HashSet<Integer> previousSelection = new HashSet<>(Sandbox.getInstance().getSelector().getSelectedItems());
        previousSelectionIds = EntityUtils.getEntityId(previousSelection);

        Set<Integer> items = getNotification().getBody();

        if(items == null) {
            // deselect all
            sandbox.getSelector().setSelections(items, true);
            facade.sendNotification(DONE);
            return;
        }

        // check if items are in viewable element, if no - cancel
        NodeComponent nodeComponent = SandboxComponentRetriever.get(sandbox.getCurrentViewingEntity(), NodeComponent.class);
        for (Iterator<Integer> iterator = items.iterator(); iterator.hasNext();) {
            int item = iterator.next();
            if(!nodeComponent.children.contains(item, false)) {
                iterator.remove();
            }
        }

        if(items.size() == 0) {
            cancel();
        } else {
            sandbox.getSelector().setSelections(items, true);
        }
        facade.sendNotification(DONE);
    }

    @Override
    public void undoAction() {
        Sandbox.getInstance().getSelector().setSelections(EntityUtils.getByUniqueId(previousSelectionIds), true);
        facade.sendNotification(DONE);
    }
}
