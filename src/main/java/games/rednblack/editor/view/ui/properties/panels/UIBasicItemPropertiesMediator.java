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

import com.artemis.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddComponentToItemCommand;
import games.rednblack.editor.controller.commands.AddToLibraryCommand;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.components.physics.SensorComponent;
import games.rednblack.editor.renderer.components.shape.CircleShapeComponent;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.ComponentCloner;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.widget.HyperLapColorPicker;
import games.rednblack.h2d.extension.typinglabel.TypingLabelComponent;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.puremvc.java.interfaces.INotification;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by azakhary on 4/15/2015.
 */
public class UIBasicItemPropertiesMediator extends UIItemPropertiesMediator<UIBasicItemProperties> {
    private static final String TAG = UIBasicItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private TransformComponent transformComponent;
    private MainItemComponent mainItemComponent;
    private DimensionsComponent dimensionComponent;
    private TintComponent tintComponent;

    public static final String POLYGON_COMPONENT_KEY = "Polygon Shape";
    public static final String CIRCLE_SHAPE_COMPONENT_KEY = "Circle Shape";
    public static final String PHYSICS_COMPONENT_KEY = "Physics";
    public static final String SENSOR_COMPONENT_KEY = "Physics Sensors";
    public static final String SHADER_COMPONENT_KEY = "Shader";
    public static final String LIGHT_COMPONENT_KEY = "Light";
    public static final String TYPING_LABEL_COMPONENT_KEY = "Typing Label";

    final private HashMap<String, Class<? extends Component>> componentClassMap = new HashMap<>();

    public UIBasicItemPropertiesMediator() {
        super(NAME, new UIBasicItemProperties());
    }

    @Override
    public void onRegister() {
        componentClassMap.put(POLYGON_COMPONENT_KEY, PolygonShapeComponent.class);
        componentClassMap.put(CIRCLE_SHAPE_COMPONENT_KEY, CircleShapeComponent.class);
        componentClassMap.put(PHYSICS_COMPONENT_KEY, PhysicsBodyComponent.class);
        componentClassMap.put(SENSOR_COMPONENT_KEY, SensorComponent.class);
        componentClassMap.put(SHADER_COMPONENT_KEY, ShaderComponent.class);
        componentClassMap.put(LIGHT_COMPONENT_KEY, LightBodyComponent.class);
        componentClassMap.put(TYPING_LABEL_COMPONENT_KEY, TypingLabelComponent.class);
    }

    @Override
    public String[] listNotificationInterests() {
        String[] defaultNotifications = super.listNotificationInterests();
        String[] notificationInterests = new String[]{
                UIBasicItemProperties.TINT_COLOR_BUTTON_CLICKED,
                UIBasicItemProperties.LINKING_CHANGED,
                UIBasicItemProperties.ADD_COMPONENT_BUTTON_CLICKED
        };

        return ArrayUtils.addAll(defaultNotifications, notificationInterests);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        switch (notification.getName()) {
            case UIBasicItemProperties.TINT_COLOR_BUTTON_CLICKED:
                Color prevColor = viewComponent.getTintColor().cpy();
                ColorPicker picker = new HyperLapColorPicker(new ColorPickerAdapter() {
                    @Override
                    public void finished(Color newColor) {
                        TintComponent tintComponent = SandboxComponentRetriever.get(observableReference, TintComponent.class);
                        tintComponent.color.set(prevColor);

                        viewComponent.setTintColor(newColor);
                        facade.sendNotification(viewComponent.getUpdateEventName());
                    }

                    @Override
                    public void changed(Color newColor) {
                        TintComponent tintComponent = SandboxComponentRetriever.get(observableReference, TintComponent.class);
                        tintComponent.color.set(newColor);
                    }
                });

                if (notification.getBody() != null) {
                    viewComponent.setTintColor(notification.getBody());
                }

                picker.setColor(viewComponent.getTintColor());
                Sandbox.getInstance().getUIStage().addActor(picker.fadeIn());
                break;
            case UIBasicItemProperties.LINKING_CHANGED:
                boolean isLinked = notification.getBody();
                if(!isLinked) {
                    facade.sendNotification(MsgAPI.ACTION_ADD_TO_LIBRARY, AddToLibraryCommand.payloadUnLink(observableReference));
                } else {
                    facade.sendNotification(MsgAPI.SHOW_ADD_LIBRARY_DIALOG, observableReference);
                }
                break;
            case UIBasicItemProperties.ADD_COMPONENT_BUTTON_CLICKED:
                Class<? extends Component> componentClass = componentClassMap.get(viewComponent.getSelectedComponent());
                if(componentClass == null) break;
                facade.sendNotification(MsgAPI.ACTION_ADD_COMPONENT, AddComponentToItemCommand.payload(observableReference, componentClass));
                break;
            default:
                break;
        }
    }

    public boolean isXYScaleLinked() {
        return viewComponent.isXYScaleLinked();
    }

    @Override
    public void setItem(int item) {
        super.setItem(item);
        lockUpdates = true;
        int entityType = EntityUtils.getType(observableReference);
        if (entityType == EntityFactory.LABEL_TYPE
                || entityType == EntityFactory.COMPOSITE_TYPE
                || entityType == EntityFactory.NINE_PATCH) {
            viewComponent.setWidthHeightDisabled(false);
        } else {
            viewComponent.setWidthHeightDisabled(true);
        }
        lockUpdates = false;
    }

    @Override
    protected void translateObservableDataToView(int entity) {
    	transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
    	mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
    	dimensionComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
    	tintComponent = SandboxComponentRetriever.get(entity, TintComponent.class);

    	int entityType = EntityUtils.getType(observableReference);
        if(entityType == EntityFactory.COMPOSITE_TYPE) {
            if(mainItemComponent.libraryLink!= null && mainItemComponent.libraryLink.length() > 0) {
                viewComponent.setLinkage(true, mainItemComponent.libraryLink);
            } else {
                viewComponent.setLinkage(false, "not in library");
            }
        }

        if (entityType == EntityFactory.LIGHT_TYPE) {
            componentClassMap.remove(LIGHT_COMPONENT_KEY);
            componentClassMap.remove(SHADER_COMPONENT_KEY);
        }
        if (entityType != EntityFactory.LABEL_TYPE) {
            componentClassMap.remove(TYPING_LABEL_COMPONENT_KEY);
        }

        viewComponent.setItemType(EntityUtils.getType(entity), mainItemComponent.uniqueId);
        viewComponent.setIdBoxValue(mainItemComponent.itemIdentifier);
        viewComponent.setXValue(String.format(Locale.ENGLISH, "%.2f", transformComponent.x));
        viewComponent.setYValue(String.format(Locale.ENGLISH, "%.2f", transformComponent.y));

        if (dimensionComponent.polygon != null) {
            Rectangle rectangle = dimensionComponent.polygon.getBoundingRectangle();
            viewComponent.setWidthValue(String.format(Locale.ENGLISH, "%.2f", rectangle.width));
            viewComponent.setHeightValue(String.format(Locale.ENGLISH, "%.2f", rectangle.height));
        } else {
            viewComponent.setWidthValue(String.format(Locale.ENGLISH, "%.2f", dimensionComponent.width));
            viewComponent.setHeightValue(String.format(Locale.ENGLISH, "%.2f", dimensionComponent.height));
        }

        viewComponent.setRotationValue(transformComponent.rotation + "");
        viewComponent.setScaleXValue(transformComponent.scaleX + "");
        viewComponent.setScaleYValue(transformComponent.scaleY + "");
        viewComponent.setTintColor(tintComponent.color);
        viewComponent.setFlipX(transformComponent.flipX);
        viewComponent.setFlipY(transformComponent.flipY);

        // non existent components
        Array<String> componentsToAddList = new Array<>();
        for (Map.Entry<String, Class<? extends Component>> entry : componentClassMap.entrySet()) {
            String componentName = entry.getKey();
            Class<? extends Component> componentClass = entry.getValue();
            Component component = SandboxComponentRetriever.get(entity, componentClass);
            if(component == null) {
                componentsToAddList.add(componentName);
            }
        }
        componentsToAddList.sort();
        viewComponent.setNonExistentComponents(componentsToAddList);
    }

    @Override
    protected void translateViewToItemData() {
    	int entity  = observableReference;

        transformComponent = ComponentCloner.get(SandboxComponentRetriever.get(entity, TransformComponent.class));
        mainItemComponent = ComponentCloner.get(SandboxComponentRetriever.get(entity, MainItemComponent.class));
        dimensionComponent = ComponentCloner.get(SandboxComponentRetriever.get(entity, DimensionsComponent.class));
        tintComponent = ComponentCloner.get(SandboxComponentRetriever.get(entity, TintComponent.class));

    	mainItemComponent.itemIdentifier = viewComponent.getIdBoxValue();
    	transformComponent.x = NumberUtils.toFloat(viewComponent.getXValue(), transformComponent.x);
    	transformComponent.y = NumberUtils.toFloat(viewComponent.getYValue(), transformComponent.y);

        dimensionComponent.width = NumberUtils.toFloat(viewComponent.getWidthValue());
        dimensionComponent.height = NumberUtils.toFloat(viewComponent.getHeightValue());

        if (dimensionComponent.boundBox != null) {
            dimensionComponent.boundBox.width = dimensionComponent.width;
            dimensionComponent.boundBox.height = dimensionComponent.height;
        }

        transformComponent.rotation = NumberUtils.toFloat(viewComponent.getRotationValue(), transformComponent.rotation);
    	transformComponent.scaleX = NumberUtils.toFloat(viewComponent.getScaleXValue(), transformComponent.scaleX);
    	transformComponent.scaleY = NumberUtils.toFloat(viewComponent.getScaleYValue(), transformComponent.scaleY);
    	transformComponent.flipY = viewComponent.getFlipY();
    	transformComponent.flipX = viewComponent.getFlipX();
        Color color = viewComponent.getTintColor();
        tintComponent.color.set(color);

        Array<Component> componentsToUpdate = new Array<>();
        componentsToUpdate.add(transformComponent);
        componentsToUpdate.add(mainItemComponent);
        componentsToUpdate.add(dimensionComponent);
        componentsToUpdate.add(tintComponent);
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = componentsToUpdate;
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_ITEM_DATA, payload);
    }
}
