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

package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddComponentToItemCommand;
import games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand;
import games.rednblack.editor.renderer.components.shape.CircleShapeComponent;
import games.rednblack.editor.renderer.components.shape.PolygonShapeComponent;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.components.physics.SensorComponent;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.editor.view.stage.tools.TextTool;
import games.rednblack.editor.view.ui.properties.UIAbstractProperties;
import games.rednblack.editor.view.ui.properties.UIAbstractPropertiesMediator;
import games.rednblack.editor.view.ui.properties.panels.*;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.typinglabel.TypingLabelComponent;
import org.puremvc.java.interfaces.IMediator;
import org.puremvc.java.interfaces.INotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by azakhary on 4/15/2015.
 */
public class UIMultiPropertyBoxMediator extends PanelMediator<UIMultiPropertyBox> {

    private static final String TAG = UIMultiPropertyBoxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private HashMap<String, ArrayList<String>> classToMediatorMap;

    private ArrayList<UIAbstractPropertiesMediator> currentRegisteredPropertyBoxes = new ArrayList<>();

    public UIMultiPropertyBoxMediator() {
        super(NAME, new UIMultiPropertyBox());
        facade = HyperLap2DFacade.getInstance();

        initMap();
    }

    private void initMap() {
        classToMediatorMap = new HashMap<>();

        classToMediatorMap.put(Integer.class.getName(), new ArrayList<>());
        classToMediatorMap.get(Integer.class.getName()).add(UIBasicItemPropertiesMediator.NAME);

        classToMediatorMap.put(SceneVO.class.getName(), new ArrayList<>());
        classToMediatorMap.get(SceneVO.class.getName()).add(UIScenePropertiesMediator.NAME);

        classToMediatorMap.put(TextTool.class.getName(), new ArrayList<>());
        classToMediatorMap.get(TextTool.class.getName()).add(UITextToolPropertiesMediator.NAME);
    }

    @Override
    public String[] listNotificationInterests() {
        String[] parentNotifications = super.listNotificationInterests();
        return Stream.of(parentNotifications, new String[]{
                MsgAPI.SCENE_LOADED,
                MsgAPI.EMPTY_SPACE_CLICKED,
                MsgAPI.ITEM_DATA_UPDATED,
                MsgAPI.ITEM_SELECTION_CHANGED,
                MsgAPI.DELETE_ITEMS_COMMAND_DONE,
                SandboxMediator.SANDBOX_TOOL_CHANGED,
                AddComponentToItemCommand.DONE,
                RemoveComponentFromItemCommand.DONE
        }).flatMap(Stream::of).toArray(String[]::new);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
            case MsgAPI.EMPTY_SPACE_CLICKED:
            case MsgAPI.DELETE_ITEMS_COMMAND_DONE:
                initAllPropertyBoxes(null);
                break;
            case MsgAPI.ITEM_SELECTION_CHANGED:
                Set<Integer> selection = notification.getBody();
                if (selection.size() == 1) {
                    initAllPropertyBoxes(selection.iterator().next());
                } else if (selection.size() > 1) {
                    initMultipleSelectionPropertyBox(selection);
                }
                break;
            case RemoveComponentFromItemCommand.DONE:
            case SandboxMediator.SANDBOX_TOOL_CHANGED:
            case AddComponentToItemCommand.DONE:
                initAllPropertyBoxes(notification.getBody());
                break;
            default:
                break;
        }
    }

    private void initAllPropertyBoxes(Object observable) {
        if (observable == null) {
            // if there is nothing to observe, always observe current scene
            observable = Sandbox.getInstance().sceneControl.getCurrentSceneVO();
        }

        String mapName = observable.getClass().getName();

        if (classToMediatorMap.get(mapName) == null) return;

        // retrieve a list of property panels to show
        ArrayList<String> mediatorNames = new ArrayList<>(classToMediatorMap.get(mapName));

        // TODO: this is not uber cool, gotta think a new way to make this class know nothing about entities
        if (observable instanceof Integer) {
            initEntityProperties(mediatorNames, (int) observable);
        }

        clearPropertyBoxes();

        for (String mediatorName : mediatorNames) {
            try {
                facade.registerMediator((IMediator) ClassReflection.newInstance(ClassReflection.forName(mediatorName)));

                UIAbstractPropertiesMediator<Object, UIAbstractProperties> propertyBoxMediator = facade.retrieveMediator(mediatorName);
                currentRegisteredPropertyBoxes.add(propertyBoxMediator);
                propertyBoxMediator.setItem(observable);
                viewComponent.addPropertyBox(propertyBoxMediator.getViewComponent());
            } catch (ReflectionException e) {
                e.printStackTrace();
            }
        }
    }

    private void initEntityProperties(ArrayList<String> mediatorNames, int entity) {
        int entityType = EntityUtils.getType(entity);

        if (entityType == EntityFactory.IMAGE_TYPE) {
            mediatorNames.add(UIImageItemPropertiesMediator.NAME);
        }

        if (entityType == EntityFactory.COMPOSITE_TYPE) {
            mediatorNames.add(UICompositeItemPropertiesMediator.NAME);
        }
        if (entityType == EntityFactory.LABEL_TYPE) {
            mediatorNames.add(UILabelItemPropertiesMediator.NAME);
        }
        if (entityType == EntityFactory.SPRITE_TYPE) {
            mediatorNames.add(UISpriteAnimationItemPropertiesMediator.NAME);
        }
        if (entityType == SpineItemType.SPINE_TYPE) {
            mediatorNames.add(UISpineAnimationItemPropertiesMediator.NAME);
        }
        if (entityType == EntityFactory.LIGHT_TYPE) {
            mediatorNames.add(UILightItemPropertiesMediator.NAME);
        }
        if (entityType == EntityFactory.PARTICLE_TYPE) {
            mediatorNames.add(UIParticlePropertiesMediator.NAME);
        }
        if (entityType == TalosItemType.TALOS_TYPE) {
            mediatorNames.add(UITalosPropertiesMediator.NAME);
        }

        // optional panels based on components
        PolygonShapeComponent polygonShapeComponent = SandboxComponentRetriever.get(entity, PolygonShapeComponent.class);
        PhysicsBodyComponent physicsComponent = SandboxComponentRetriever.get(entity, PhysicsBodyComponent.class);
        SensorComponent sensorComponent = SandboxComponentRetriever.get(entity, SensorComponent.class);
        ShaderComponent shaderComponent = SandboxComponentRetriever.get(entity, ShaderComponent.class);
        LightBodyComponent lightComponent = SandboxComponentRetriever.get(entity, LightBodyComponent.class);
        TypingLabelComponent typingLabelComponent = SandboxComponentRetriever.get(entity, TypingLabelComponent.class);
        CircleShapeComponent circleShapeComponent = SandboxComponentRetriever.get(entity, CircleShapeComponent.class);

        if (polygonShapeComponent != null) {
            mediatorNames.add(UIPolygonComponentPropertiesMediator.NAME);
        }
        if (physicsComponent != null) {
            mediatorNames.add(UIPhysicsPropertiesMediator.NAME);
        }
        if (sensorComponent != null) {
            mediatorNames.add(UISensorPropertiesMediator.NAME);
        }
        if (shaderComponent != null) {
            mediatorNames.add(UIShaderPropertiesMediator.NAME);
        }
        if (lightComponent != null) {
            mediatorNames.add(UILightBodyPropertiesMediator.NAME);
        }
        if (typingLabelComponent != null) {
            mediatorNames.add(UITypingLabelPropertiesMediator.NAME);
        }
        if (circleShapeComponent != null) {
            mediatorNames.add(UICircleShapePropertiesMediator.NAME);
        }
    }

    private void clearPropertyBoxes() {
        //clear all current enabled panels
        viewComponent.clearAll();

        //unregister all current mediators
        for (UIAbstractPropertiesMediator mediator : currentRegisteredPropertyBoxes) {
            facade.removeMediator(mediator.getMediatorName());
        }
        currentRegisteredPropertyBoxes.clear();
    }

    private void initMultipleSelectionPropertyBox(Set<Integer> selection) {
        clearPropertyBoxes();
        UIMultipleSelectPropertiesMediator mediator = new UIMultipleSelectPropertiesMediator();
        facade.registerMediator(mediator);
        currentRegisteredPropertyBoxes.add(mediator);
        mediator.setItem(selection);
        viewComponent.addPropertyBox(mediator.getViewComponent());
    }
}
