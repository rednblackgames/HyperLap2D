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

package games.rednblack.editor.view.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.PluginManager;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.stage.tools.PolygonTool;
import games.rednblack.editor.view.stage.tools.TransformTool;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.HashMap;

/**
 * Created by azakhary on 4/20/2015.
 */
public class UIDropDownMenuMediator extends Mediator<UIDropDownMenu> {
    private static final String TAG = UIDropDownMenuMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public static final Integer SCENE_ACTIONS_SET = 0;
    public static final Integer ITEMS_ACTIONS_SET = 1;
    public static final Integer LIBRARY_ITEM_ACTION_SET = 2;
    public static final Integer IMAGE_RESOURCE_ACTION_SET = 3;
    public static final Integer RULER_RESOURCE_ACTION_SET = 4;
    public static final Integer SPINE_ANIMATION_ACTION_SET = 5;
    public static final Integer SPRITE_ANIMATION_ACTION_SET = 7;
    public static final Integer PARTICLE_ACTION_SET = 9;
    public static final Integer POLYGON_VERTEX_ACTION_SET = 10;
    public static final Integer ORIGIN_POINT_ACTION_SET = 11;
    public static final Integer LIBRARY_ACTION_ACTION_SET = 12;
    public static final Integer TALOS_ACTION_SET = 13;
    public static final Integer MIX_RESOURCE_BOX_ACTION_SET = 14;
    public static final Integer TINY_VG_RESOURCE_ACTION_SET = 15;

    private Sandbox sandbox;

    private Object currentObservable;

    public HashMap<Integer, Array<String>> actionSets = new HashMap<>();
    private final Array<String> tmpActionSet = new Array<>();

    public UIDropDownMenuMediator() {
        super(NAME, new UIDropDownMenu());
    }

    @Override
    public void onRegister() {
        super.onRegister();

        sandbox = Sandbox.getInstance();

        actionSets.put(SCENE_ACTIONS_SET, new Array<>());
        actionSets.get(SCENE_ACTIONS_SET).add(MsgAPI.ACTION_PASTE);
        actionSets.get(SCENE_ACTIONS_SET).add(MsgAPI.ACTION_CREATE_PRIMITIVE);
        actionSets.get(SCENE_ACTIONS_SET).add(MsgAPI.ACTION_CREATE_STICKY_NOTE);

        actionSets.put(MIX_RESOURCE_BOX_ACTION_SET, new Array<>());
        actionSets.get(MIX_RESOURCE_BOX_ACTION_SET).add(MsgAPI.ACTION_DELETE_MULTIPLE_RESOURCE);

        actionSets.put(IMAGE_RESOURCE_ACTION_SET, new Array<>());
        actionSets.get(IMAGE_RESOURCE_ACTION_SET).add(MsgAPI.ACTION_DELETE_IMAGE_RESOURCE);

        actionSets.put(TINY_VG_RESOURCE_ACTION_SET, new Array<>());
        actionSets.get(TINY_VG_RESOURCE_ACTION_SET).add(MsgAPI.ACTION_DELETE_TINY_VG_RESOURCE);

        actionSets.put(SPINE_ANIMATION_ACTION_SET, new Array<>());
        actionSets.get(SPINE_ANIMATION_ACTION_SET).add(MsgAPI.ACTION_DELETE_SPINE_ANIMATION_RESOURCE);

        actionSets.put(SPRITE_ANIMATION_ACTION_SET, new Array<>());
        actionSets.get(SPRITE_ANIMATION_ACTION_SET).add(MsgAPI.ACTION_DELETE_SPRITE_ANIMATION_RESOURCE);

        actionSets.put(LIBRARY_ITEM_ACTION_SET, new Array<>());
        actionSets.get(LIBRARY_ITEM_ACTION_SET).add(MsgAPI.ACTION_DELETE_LIBRARY_ITEM);
        actionSets.get(LIBRARY_ITEM_ACTION_SET).add(MsgAPI.ACTION_EXPORT_LIBRARY_ITEM);

        actionSets.put(LIBRARY_ACTION_ACTION_SET, new Array<>());
        actionSets.get(LIBRARY_ACTION_ACTION_SET).add(MsgAPI.ACTION_DUPLICATE_LIBRARY_ACTION);
        actionSets.get(LIBRARY_ACTION_ACTION_SET).add(MsgAPI.ACTION_DELETE_LIBRARY_ACTION);
        actionSets.get(LIBRARY_ACTION_ACTION_SET).add(MsgAPI.ACTION_EXPORT_ACTION_ITEM);
        actionSets.get(LIBRARY_ACTION_ACTION_SET).add(MsgAPI.ACTION_RENAME_ACTION_ITEM);

        actionSets.put(PARTICLE_ACTION_SET, new Array<>());
        actionSets.get(PARTICLE_ACTION_SET).add(MsgAPI.ACTION_DELETE_PARTICLE_EFFECT);

        actionSets.put(TALOS_ACTION_SET, new Array<>());
        actionSets.get(TALOS_ACTION_SET).add(MsgAPI.ACTION_DELETE_TALOS_VFX);

        actionSets.put(ITEMS_ACTIONS_SET, new Array<>());
        actionSets.get(ITEMS_ACTIONS_SET).add(MsgAPI.ACTION_CUT);
        actionSets.get(ITEMS_ACTIONS_SET).add(MsgAPI.ACTION_COPY);
        actionSets.get(ITEMS_ACTIONS_SET).add(MsgAPI.ACTION_PASTE);
        actionSets.get(ITEMS_ACTIONS_SET).add(MsgAPI.ACTION_DELETE);
        actionSets.get(ITEMS_ACTIONS_SET).add(MsgAPI.ACTION_GROUP_ITEMS);
        actionSets.get(ITEMS_ACTIONS_SET).add(MsgAPI.ACTION_CONVERT_TO_BUTTON);
        
        actionSets.put(RULER_RESOURCE_ACTION_SET, new Array<>());
        actionSets.get(RULER_RESOURCE_ACTION_SET).add(MsgAPI.ACTION_UPDATE_RULER_POSITION);

        actionSets.put(POLYGON_VERTEX_ACTION_SET, new Array<>());
        actionSets.get(POLYGON_VERTEX_ACTION_SET).add(MsgAPI.ACTION_CHANGE_POLYGON_VERTEX_POSITION);
        actionSets.get(POLYGON_VERTEX_ACTION_SET).add(MsgAPI.ACTION_DELETE_POLYGON_VERTEX);

        actionSets.put(ORIGIN_POINT_ACTION_SET, new Array<>());
        actionSets.get(ORIGIN_POINT_ACTION_SET).add(MsgAPI.ACTION_CHANGE_ORIGIN_POSITION);
        actionSets.get(ORIGIN_POINT_ACTION_SET).add(MsgAPI.ACTION_CENTER_ORIGIN_POSITION);

        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.SCENE_RIGHT_CLICK,
                MsgAPI.ITEM_RIGHT_CLICK,
                UIDropDownMenu.ITEM_CLICKED,
                UIResourcesBoxMediator.IMAGE_RIGHT_CLICK,
                UIResourcesBoxMediator.TINY_VG_RIGHT_CLICK,
                UIResourcesBoxMediator.SPINE_ANIMATION_RIGHT_CLICK,
                UIResourcesBoxMediator.SPRITE_ANIMATION_RIGHT_CLICK,
                UIResourcesBoxMediator.LIBRARY_ITEM_RIGHT_CLICK,
                UIResourcesBoxMediator.PARTICLE_EFFECT_RIGHT_CLICK,
                UIResourcesBoxMediator.TALOS_VFX_RIGHT_CLICK,
                UIResourcesBoxMediator.LIBRARY_ACTION_RIGHT_CLICK,
                RulersUI.RIGHT_CLICK_RULER,
                PolygonTool.MANUAL_VERTEX_POSITION,
                TransformTool.MANUAL_ORIGIN_POSITION
        };
    }

    private void applyItemTypeMutators(Array<String> actionsSet) {
        // generic mutators
        if (sandbox.getSelector().getCurrentSelection().size() == 1) {
            if(sandbox.getSelector().selectionIsComposite()) {
                actionsSet.add(MsgAPI.SHOW_ADD_LIBRARY_DIALOG);
                actionsSet.add(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE);
            }
        }

        // external plugin mutators
        PluginManager pluginManager = facade.retrieveProxy(PluginManager.NAME);
        if (pluginManager != null)
            pluginManager.dropDownActionSets(sandbox.getSelector().getCurrentSelection(), actionsSet);
    }

    private void applyResourceBoxMutators(Array<String> actionsSet) {
        BoxItemResourceSelectionUIMediator boxSelection = facade.retrieveMediator(BoxItemResourceSelectionUIMediator.NAME);
        if (boxSelection.boxResourceSelectedSet.size() > 1) {
            actionsSet.addAll(actionSets.get(MIX_RESOURCE_BOX_ACTION_SET));
        }
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        tmpActionSet.clear();

        switch (notification.getName()) {
            case MsgAPI.SCENE_RIGHT_CLICK:
                Vector2 stageCoords = notification.getBody();
                tmpActionSet.addAll(actionSets.get(SCENE_ACTIONS_SET));
                applyItemTypeMutators(tmpActionSet);
                showPopup(tmpActionSet, stageCoords);
                break;
            case MsgAPI.ITEM_RIGHT_CLICK:
                tmpActionSet.addAll(actionSets.get(ITEMS_ACTIONS_SET));
                applyItemTypeMutators(tmpActionSet);
                showPopup(tmpActionSet, sandbox.getSelector().getSelectedItem());
                break;
            case UIResourcesBoxMediator.IMAGE_RIGHT_CLICK:
                tmpActionSet.addAll(actionSets.get(IMAGE_RESOURCE_ACTION_SET));
                applyResourceBoxMutators(tmpActionSet);
                showPopup(tmpActionSet, notification.getBody());
                break;
            case UIResourcesBoxMediator.TINY_VG_RIGHT_CLICK:
                tmpActionSet.addAll(actionSets.get(TINY_VG_RESOURCE_ACTION_SET));
                applyResourceBoxMutators(tmpActionSet);
                showPopup(tmpActionSet, notification.getBody());
                break;
            case UIResourcesBoxMediator.SPINE_ANIMATION_RIGHT_CLICK:
                tmpActionSet.addAll(actionSets.get(SPINE_ANIMATION_ACTION_SET));
                applyResourceBoxMutators(tmpActionSet);
                showPopup(tmpActionSet, notification.getBody());
                break;
            case UIResourcesBoxMediator.SPRITE_ANIMATION_RIGHT_CLICK:
                tmpActionSet.addAll(actionSets.get(SPRITE_ANIMATION_ACTION_SET));
                applyResourceBoxMutators(tmpActionSet);
                showPopup(tmpActionSet, notification.getBody());
                break;
            case UIResourcesBoxMediator.LIBRARY_ITEM_RIGHT_CLICK:
                showPopup(LIBRARY_ITEM_ACTION_SET, notification.getBody());
                break;
            case UIResourcesBoxMediator.LIBRARY_ACTION_RIGHT_CLICK:
                showPopup(LIBRARY_ACTION_ACTION_SET, notification.getBody());
                break;
            case UIResourcesBoxMediator.PARTICLE_EFFECT_RIGHT_CLICK:
                showPopup(PARTICLE_ACTION_SET, notification.getBody());
                break;
            case UIResourcesBoxMediator.TALOS_VFX_RIGHT_CLICK:
                showPopup(TALOS_ACTION_SET, notification.getBody());
                break;
            case RulersUI.RIGHT_CLICK_RULER:
            	showPopup(RULER_RESOURCE_ACTION_SET, notification.getBody());
            	break;
            case PolygonTool.MANUAL_VERTEX_POSITION:
                showPopup(POLYGON_VERTEX_ACTION_SET, notification.getBody());
                break;
            case TransformTool.MANUAL_ORIGIN_POSITION:
                showPopup(ORIGIN_POINT_ACTION_SET, notification.getBody());
                break;
            case UIDropDownMenu.ITEM_CLICKED:
                facade.sendNotification(notification.getBody(), currentObservable);
                break;
            default:
                break;
        }
    }

    private void showPopup(Integer actionsSet, Object observable) {
        showPopup(actionSets.get(actionsSet), observable);
    }

    private void showPopup(Array<String> actionsSet, Object observable) {
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        viewComponent.setActionList(actionsSet);
        viewComponent.showMenu(sandbox.getUIStage(), sandbox.getInputX(), uiStage.getHeight() - sandbox.getInputY());

        currentObservable = observable;
    }

    public void setCurrentObservable(Object currentObservable) {
        this.currentObservable = currentObservable;
    }
}
