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

import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.LightsPropertiesVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.badlogic.gdx.graphics.Color;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.properties.UIAbstractPropertiesMediator;
import games.rednblack.editor.renderer.data.PhysicsPropertiesVO;
import games.rednblack.editor.renderer.data.SceneVO;

/**
 * Created by azakhary on 4/16/2015.
 */
public class UIScenePropertiesMediator extends UIAbstractPropertiesMediator<SceneVO, UISceneProperties> {
    private static final String TAG = UIScenePropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIScenePropertiesMediator() {
        super(NAME, new UISceneProperties());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UISceneProperties.AMBIENT_COLOR_BUTTON_CLICKED,
                UISceneProperties.DIRECTIONAL_COLOR_BUTTON_CLICKED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UISceneProperties.AMBIENT_COLOR_BUTTON_CLICKED:
                ColorPicker picker = new ColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void finished(Color newColor) {
                        viewComponent.setAmbientColor(newColor);
                        facade.sendNotification(viewComponent.getUpdateEventName());
                    }
                    @Override
                    public void changed(Color newColor) {
                        viewComponent.setAmbientColor(newColor);
                        facade.sendNotification(viewComponent.getUpdateEventName());
                    }
                });

                if (notification.getBody() != null) {
                    viewComponent.setAmbientColor(notification.getBody());
                }
                picker.setColor(viewComponent.getAmbientColor());
                Sandbox.getInstance().getUIStage().addActor(picker.fadeIn());

                break;
            case UISceneProperties.DIRECTIONAL_COLOR_BUTTON_CLICKED:
                picker = new ColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void finished(Color newColor) {
                        viewComponent.setDirectionalColor(newColor);
                        facade.sendNotification(viewComponent.getUpdateEventName());
                    }
                    @Override
                    public void changed(Color newColor) {
                        viewComponent.setDirectionalColor(newColor);
                        facade.sendNotification(viewComponent.getUpdateEventName());
                    }
                });

                if (notification.getBody() != null) {
                    viewComponent.setDirectionalColor(notification.getBody());
                }
                picker.setColor(viewComponent.getDirectionalColor());
                Sandbox.getInstance().getUIStage().addActor(picker.fadeIn());

                break;
            default:
                break;
        }
    }

    protected void translateObservableDataToView(SceneVO item) {
        PhysicsPropertiesVO physicsVO = item.physicsPropertiesVO;
        LightsPropertiesVO lightsVO = item.lightsPropertiesVO;
        ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        ResolutionEntryVO res = resolutionManager.getCurrentResolution();

        viewComponent.setPixelsPerWorldUnit(Sandbox.getInstance().getPixelPerWU(), res.width, res.height);
        viewComponent.setGravityXValue(physicsVO.gravityX + "");
        viewComponent.setGravityYValue(physicsVO.gravityY + "");
        viewComponent.setPhysicsEnable(physicsVO.enabled);
        viewComponent.setSleepVelocityValue(physicsVO.sleepVelocity + "");
        viewComponent.setAmbientColor(new Color(lightsVO.ambientColor[0], lightsVO.ambientColor[1], lightsVO.ambientColor[2], lightsVO.ambientColor[3]));
        viewComponent.setBlurNum(lightsVO.blurNum + "");
        viewComponent.setDirectionalDegree(lightsVO.directionalDegree + "");
        viewComponent.setDirectionalRays(lightsVO.directionalRays + "");
        viewComponent.setDirectionalColor(new Color(lightsVO.directionalColor[0], lightsVO.directionalColor[1], lightsVO.directionalColor[2], lightsVO.directionalColor[3]));

        viewComponent.setLightsEnabled(lightsVO.enabled);
        viewComponent.setLightType(lightsVO.lightType);
    }

    @Override
    protected void translateViewToItemData() {
        PhysicsPropertiesVO physicsVO = observableReference.physicsPropertiesVO;
        physicsVO.gravityX = NumberUtils.toFloat(viewComponent.getGravityXValue(), physicsVO.gravityX);
        physicsVO.gravityY = NumberUtils.toFloat(viewComponent.getGravityYValue(), physicsVO.gravityY);
        physicsVO.sleepVelocity = NumberUtils.toFloat(viewComponent.getSleepVelocityValue(), physicsVO.sleepVelocity);
        physicsVO.enabled = viewComponent.isPhysicsEnabled();

        LightsPropertiesVO lightsVO = observableReference.lightsPropertiesVO;
        Color color = viewComponent.getAmbientColor();
        lightsVO.ambientColor[0] = color.r;
        lightsVO.ambientColor[1] = color.g;
        lightsVO.ambientColor[2] = color.b;
        lightsVO.ambientColor[3] = color.a;
        lightsVO.blurNum = NumberUtils.toInt(viewComponent.getBlurNumValue(), lightsVO.blurNum);
        lightsVO.lightType = viewComponent.getLightType();
        lightsVO.directionalDegree = NumberUtils.toFloat(viewComponent.getDirectionalDegree(), lightsVO.directionalDegree);
        lightsVO.directionalRays = NumberUtils.toInt(viewComponent.getDirectionalRays(), lightsVO.directionalRays);
        color = viewComponent.getDirectionalColor();
        lightsVO.directionalColor[0] = color.r;
        lightsVO.directionalColor[1] = color.g;
        lightsVO.directionalColor[2] = color.b;
        lightsVO.directionalColor[3] = color.a;

        lightsVO.enabled = viewComponent.isLightsEnabled();

        Sandbox.getInstance().sceneControl.updateAmbientLights();
    }
}
