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

package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.controller.commands.component.UpdateShaderDataCommand;
import games.rednblack.h2d.common.MsgAPI;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Created by azakhary on 8/12/2015.
 */
public class UIShaderPropertiesMediator extends UIItemPropertiesMediator<Entity, UIShaderProperties> {
    private static final String TAG = UIShaderPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIShaderPropertiesMediator() {
        super(NAME, new UIShaderProperties());

        ResourceManager resourceManager = HyperLap2DFacade.getInstance().retrieveProxy(ResourceManager.NAME);
        viewComponent.initView(resourceManager.getShaders());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UIShaderProperties.CLOSE_CLICKED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UIShaderProperties.CLOSE_CLICKED:
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_REMOVE_COMPONENT, RemoveComponentFromItemCommand.payload(observableReference, ShaderComponent.class));
                break;
        }
    }

    @Override
    protected void translateObservableDataToView(Entity item) {
        ShaderComponent shaderComponent = ComponentRetriever.get(item, ShaderComponent.class);
        String currShaderName = shaderComponent.shaderName;
        viewComponent.setSelected(currShaderName);
    }

    @Override
    protected void translateViewToItemData() {
        ShaderComponent shaderComponent = ComponentRetriever.get(observableReference, ShaderComponent.class);

        if (!shaderComponent.shaderName.equals(viewComponent.getShader())) {
            Object payload = UpdateShaderDataCommand.payload(observableReference, viewComponent.getShader());
            facade.sendNotification(MsgAPI.ACTION_UPDATE_SHADER_DATA, payload);
        }
    }
}
