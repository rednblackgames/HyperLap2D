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
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.controller.commands.component.UpdateLightDataCommand;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.renderer.data.LightVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Created by azakhary on 4/28/2015.
 */
public class UILightItemPropertiesMediator extends UIItemPropertiesMediator<Entity, UILightItemProperties> {

    private static final String TAG = UILightItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UILightItemPropertiesMediator() {
        super(NAME, new UILightItemProperties());
    }

    @Override
    protected void translateObservableDataToView(Entity entity) {
        LightObjectComponent lightObjectComponent = ComponentRetriever.get(entity, LightObjectComponent.class);

        viewComponent.setType(lightObjectComponent.getType());
        viewComponent.setRayCount(lightObjectComponent.rays);
        viewComponent.setStatic(lightObjectComponent.isStatic);
        viewComponent.setXRay(lightObjectComponent.isXRay);
        viewComponent.setRadius(lightObjectComponent.distance + "");
        viewComponent.setAngle(lightObjectComponent.coneDegree + "");
        viewComponent.setDistance(lightObjectComponent.distance + "");
        viewComponent.setDirection(lightObjectComponent.directionDegree + "");
        viewComponent.setLightHeight(lightObjectComponent.height + "");
        viewComponent.setSoftnessLength(lightObjectComponent.softnessLength + "");
        viewComponent.setActive(lightObjectComponent.isActive);
        viewComponent.setSoft(lightObjectComponent.isSoft);
    }

    @Override
    protected void translateViewToItemData() {
        LightVO oldPayloadVo = new LightVO();
        oldPayloadVo.loadFromEntity(observableReference);

        LightVO payloadVo = new LightVO();
        payloadVo.loadFromEntity(observableReference);

        payloadVo.rays = viewComponent.getRayCount();
        payloadVo.isStatic = viewComponent.isStatic();
        payloadVo.isXRay = viewComponent.isXRay();
        payloadVo.coneDegree = NumberUtils.toFloat(viewComponent.getAngle());
        payloadVo.softnessLength = NumberUtils.toFloat(viewComponent.getSoftnessLength());
        payloadVo.height = NumberUtils.toFloat(viewComponent.getLightHeight());
        payloadVo.isActive = viewComponent.isActive();
        payloadVo.isSoft = viewComponent.isSoft();
        
        if(payloadVo.type == LightVO.LightType.POINT) {
            payloadVo.distance = NumberUtils.toFloat(viewComponent.getRadius());
        } else {
            payloadVo.distance = NumberUtils.toFloat(viewComponent.getDistance());
            payloadVo.directionDegree = NumberUtils.toFloat(viewComponent.getDirection());
        }

        if (!oldPayloadVo.equals(payloadVo)) {
            Object payload = UpdateLightDataCommand.payload(observableReference, payloadVo);
            facade.sendNotification(MsgAPI.ACTION_UPDATE_LIGHT_DATA, payload);
        }
    }
}
