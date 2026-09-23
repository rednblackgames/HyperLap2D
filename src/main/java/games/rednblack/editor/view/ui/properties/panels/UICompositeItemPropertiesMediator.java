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
import games.rednblack.editor.proxy.PluginUIBridge;

import games.rednblack.editor.controller.commands.component.UpdateCompositeDataCommand;
import games.rednblack.editor.renderer.components.CompositeTransformComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.systems.CompositeSystem;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.renderer.widget.WidgetTypes;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/16/2015.
 */
public class UICompositeItemPropertiesMediator extends UIItemPropertiesMediator<UICompositeItemProperties> {

    private static final String TAG = UICompositeItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UICompositeItemPropertiesMediator() {
        super(NAME, new UICompositeItemProperties());
    }

    @Override
    protected void translateObservableDataToView(int item) {
        CompositeTransformComponent transformComponent = entityData.get(item, CompositeTransformComponent.class);
        viewComponent.setAutomaticResize(transformComponent.automaticResize);
        viewComponent.setScissorsEnabled(transformComponent.scissorsEnabled);
        viewComponent.setRenderToFBOEnabled(transformComponent.renderToFBO);

        WidgetComponent widget = entityData.get(item, WidgetComponent.class);
        WidgetType widgetType = widget == null ? null : WidgetTypes.get(widget.widgetType);
        viewComponent.setOwnedByWidget(widgetType != null && widgetType.clipsContent);
    }

    @Override
    protected void translateViewToItemData() {
        CompositeItemVO payloadVo = new CompositeItemVO();
        payloadVo.loadFromEntity(observableReference, sandbox.getEngine(), sandbox.getSceneControl().sceneLoader.getEntityFactory());

        payloadVo.automaticResize = viewComponent.isAutomaticResizeIsEnabled();
        payloadVo.scissorsEnabled = viewComponent.isScissorsEnabled();
        payloadVo.renderToFBO = viewComponent.isRenderToFBOEnabled();

        Object payload = UpdateCompositeDataCommand.payload(observableReference, payloadVo);
        facade.sendNotification(MsgAPI.ACTION_UPDATE_COMPOSITE_DATA, payload);

        CompositeSystem compositeSystem = PluginUIBridge.get().getSandbox().getEngine().getSystem(CompositeSystem.class);
        if (compositeSystem != null) {
            compositeSystem.process(observableReference);
        }

        Set<Integer> entityHashSet = new HashSet<>();
        entityHashSet.add(observableReference);
        facade.sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, entityHashSet);
    }
}