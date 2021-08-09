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

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by azakhary on 4/15/2015.
 */
public abstract class UIAbstractEntityPropertiesMediator<V extends UIAbstractProperties> extends UIAbstractPropertiesMediator<Integer, V> {
    protected Sandbox sandbox;

    protected int observableReference;

    protected boolean lockUpdates = true;

    public UIAbstractEntityPropertiesMediator(String mediatorName, V viewComponent) {
        super(mediatorName, viewComponent);

        sandbox = Sandbox.getInstance();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public void onRegister() {
        facade = HyperLap2DFacade.getInstance();
    }


    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.ITEM_DATA_UPDATED,
                viewComponent.getUpdateEventName()
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);


        if(notification.getName().equals(viewComponent.getUpdateEventName())) {
            if(!lockUpdates) {
                translateViewToItemData();
            }
        }

        switch (notification.getName()) {
            case MsgAPI.ITEM_DATA_UPDATED:
                onItemDataUpdate();
                break;
            default:
                break;
        }
    }

    @Override
    public void setItem(Integer item) {
        setItem((int) item);
    }

    public void setItem(int item) {
        observableReference = item;
        lockUpdates = true;
        translateObservableDataToView(observableReference);
        lockUpdates = false;
    }

    public void onItemDataUpdate() {
        lockUpdates = true;
        translateObservableDataToView(observableReference);
        lockUpdates = false;
    }

    @Override
    protected void translateObservableDataToView(Integer item) {
        translateObservableDataToView((int) item);
    }

    protected abstract void translateObservableDataToView(int item);

    protected abstract void translateViewToItemData();
}
