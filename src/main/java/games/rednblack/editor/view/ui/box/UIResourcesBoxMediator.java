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

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.ui.box.resourcespanel.*;
import games.rednblack.editor.view.ui.box.resourcespanel.filter.NormalMapFilter;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;
import org.puremvc.java.interfaces.INotification;

import java.util.stream.Stream;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIResourcesBoxMediator extends PanelMediator<UIResourcesBox> {

    private static final String TAG = UIResourcesBoxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private static final String PREFIX = "games.rednblack.editor.view.ui.box.UIResourcesBoxMediator";

    public static final String RESOURCE_BOX_LEFT_CLICK = PREFIX + ".RESOURCE_BOX_LEFT_CLICK";
    public static final String RESOURCE_BOX_RIGHT_CLICK = PREFIX + ".RESOURCE_BOX_RIGHT_CLICK";

    public static final String RESOURCE_BOX_DRAG_START = PREFIX + ".RESOURCE_BOX_DRAG_START";

    public static final String SHIFT_CTRL_EVENT_TYPE = PREFIX + ".SHIFT_CTRL_EVENT_TYPE";
    public static final String SHIFT_EVENT_TYPE = PREFIX + ".SHIFT_EVENT_TYPE";
    public static final String CTRL_EVENT_TYPE = PREFIX + ".CTRL_EVENT_TYPE";
    public static final String NORMAL_CLICK_EVENT_TYPE = PREFIX + ".NORMAL_CLICK_EVENT_TYPE";

	public static final String ADD_RESOURCES_BOX_TABLE_SELECTION_MANAGEMENT = PREFIX + ".ADD_RESOURCES_BOX_TABLE_SELECTION_MANAGEMENT";

    public static final String IMAGE_RIGHT_CLICK = PREFIX + ".IMAGE_RIGHT_CLICK";
    public static final String TINY_VG_RIGHT_CLICK = PREFIX + ".TINY_VG_RIGHT_CLICK";
    public static final String SPINE_ANIMATION_RIGHT_CLICK = PREFIX + ".SPINE_ANIMATION_RIGHT_CLICK";
    public static final String SPRITE_ANIMATION_RIGHT_CLICK = PREFIX + ".SPRITE_ANIMATION_RIGHT_CLICK";
    public static final String LIBRARY_ITEM_RIGHT_CLICK = PREFIX + ".LIBRARY_ITEM_RIGHT_CLICK";
    public static final String PARTICLE_EFFECT_RIGHT_CLICK = PREFIX + ".PARTICLE_EFFECT_RIGHT_CLICK";
    public static final String TALOS_VFX_RIGHT_CLICK = PREFIX + ".TALOS_VFX_RIGHT_CLICK";
    public static final String LIBRARY_ACTION_RIGHT_CLICK = PREFIX + ".LIBRARY_ACTION_RIGHT_CLICK";

    public static final String SANDBOX_DRAG_IMAGE_ENTER = PREFIX + ".SANDBOX_DRAG_IMAGE_ENTER";
    public static final String SANDBOX_DRAG_IMAGE_EXIT = PREFIX + ".SANDBOX_DRAG_IMAGE_EXIT";

    public Array<DragAndDrop.Target> customTargets = new Array<>();

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        registerTabMediators();
        initTabs();
    }

    public UIResourcesBoxMediator() {
        super(NAME, new UIResourcesBox());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] parentNotifications = super.listNotificationInterests();
        return Stream.of(parentNotifications, new String[]{
                ProjectManager.PROJECT_OPENED,
                ProjectManager.PROJECT_DATA_UPDATED,
                MsgAPI.ADD_TARGET,
                MsgAPI.REMOVE_TARGET
            }).flatMap(Stream::of).toArray(String[]::new);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case ProjectManager.PROJECT_OPENED:

                break;
            case ProjectManager.PROJECT_DATA_UPDATED:
                break;
            case MsgAPI.REMOVE_TARGET:
                customTargets.removeValue(notification.getBody(), true);
                break;
            case MsgAPI.ADD_TARGET:
                customTargets.add(notification.getBody());
                break;
            default:
                break;
        }
    }

    private void registerTabMediators() {
        facade.registerMediator(new UIImagesTabMediator());
        facade.registerMediator(new UIAnimationsTabMediator());
        facade.registerMediator(new UILibraryItemsTabMediator());
        facade.registerMediator(new UIParticleEffectsTabMediator());
        facade.registerMediator(new UIActionsTabMediator());
        facade.registerMediator(new UIFilterMenuMediator());
    }

    private void initTabs() {
        facade.sendNotification(MsgAPI.ADD_RESOURCES_BOX_FILTER, new NormalMapFilter());

        UIImagesTabMediator imagesTabMediator = facade.retrieveMediator(UIImagesTabMediator.NAME);
        ImageTab imagesTab = imagesTabMediator.getViewComponent();
        viewComponent.addTab(0, imagesTab);

        UIAnimationsTabMediator animationsTabMediator = facade.retrieveMediator(UIAnimationsTabMediator.NAME);
        ImageTab animationsTab = animationsTabMediator.getViewComponent();
        viewComponent.addTab(1, animationsTab);

        UIParticleEffectsTabMediator particlesTabMediator = facade.retrieveMediator(UIParticleEffectsTabMediator.NAME);
        ImageTab particlesTab = particlesTabMediator.getViewComponent();
        viewComponent.addTab(2, particlesTab);

        UILibraryItemsTabMediator libraryTabMediator = facade.retrieveMediator(UILibraryItemsTabMediator.NAME);
        ImageTab libraryItemsTab = libraryTabMediator.getViewComponent();
        viewComponent.addTab(3, libraryItemsTab);

        UIActionsTabMediator actionsTabMediator = facade.retrieveMediator(UIActionsTabMediator.NAME);
        ImageTab actionsTab = actionsTabMediator.getViewComponent();
        viewComponent.addTab(4, actionsTab);

        viewComponent.setActiveTabContent(imagesTab);
    }
}
