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

package games.rednblack.editor.view.ui.properties;

import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by azakhary on 4/15/2015.
 */
public abstract class UIItemPropertiesMediator<V extends UIAbstractProperties> extends UIAbstractEntityPropertiesMediator<V> {

    public UIItemPropertiesMediator(String mediatorName, V viewComponent) {
        super(mediatorName, viewComponent);
    }

    @Override
    public void handleNotification(INotification notification) {
        if (!validReference())
            return;

        if(notification.getName().equals(viewComponent.getUpdateEventName())) {
            if(!lockUpdates) {
                translateViewToItemData();
                afterItemDataModified();
            }
        }

        switch (notification.getName()) {
            case MsgAPI.ITEM_DATA_UPDATED:
                if(observableReference == -1) return;
                onItemDataUpdate();
                break;
            default:
                break;
        }
    }

    protected void afterItemDataModified() {

    }

    private boolean validReference() {
        return observableReference != -1
                && sandbox.getEngine().getEntityManager().isActive(observableReference)
                && SandboxComponentRetriever.get(observableReference, MainItemComponent.class) != null;
    }
}
