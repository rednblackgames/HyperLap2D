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
import com.badlogic.gdx.Gdx;
import games.rednblack.editor.controller.commands.component.UpdateCompositeDataCommand;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.CompositeTransformComponent;
import games.rednblack.editor.renderer.systems.CompositeSystem;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by azakhary on 4/16/2015.
 */
public class UICompositeItemPropertiesMediator extends UIItemPropertiesMediator<Entity, UICompositeItemProperties> {

    private static final String TAG = UICompositeItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UICompositeItemPropertiesMediator() {
        super(NAME, new UICompositeItemProperties());
    }

    @Override
    protected void translateObservableDataToView(Entity item) {
        viewComponent.setAutomaticResize(item.getComponent(CompositeTransformComponent.class).automaticResize);
        viewComponent.setScissorsEnabled(item.getComponent(CompositeTransformComponent.class).scissorsEnabled);
        viewComponent.setRenderToFBOEnabled(item.getComponent(CompositeTransformComponent.class).renderToFBO);
    }

    @Override
    protected void translateViewToItemData() {
        CompositeItemVO payloadVo = new CompositeItemVO();
        payloadVo.loadFromEntity(observableReference);

        payloadVo.automaticResize = viewComponent.isAutomaticResizeIsEnabled();
        payloadVo.scissorsEnabled = viewComponent.isScissorsEnabled();
        payloadVo.renderToFBO = viewComponent.isRenderToFBOEnabled();

        Object payload = UpdateCompositeDataCommand.payload(observableReference, payloadVo);
        facade.sendNotification(MsgAPI.ACTION_UPDATE_COMPOSITE_DATA, payload);

        CompositeSystem compositeSystem = Sandbox.getInstance().getEngine().getSystem(CompositeSystem.class);
        if (compositeSystem != null) {
            compositeSystem.processEntity(observableReference, Gdx.graphics.getDeltaTime());
        }

        Set<Entity> entityHashSet = new HashSet<>();
        entityHashSet.add(observableReference);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_SELECTION_CHANGED, entityHashSet);
    }
}