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

package games.rednblack.editor.view.ui.panel;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.data.FrameRange;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.menu.WindowMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.editor.view.ui.properties.panels.UISpriteAnimationItemProperties;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.Set;

/**
 * Created by azakhary on 5/12/2015.
 */
public class EditSpriteAnimationPanelMediator extends Mediator<EditSpriteAnimationPanel> {
    private static final String TAG = EditSpriteAnimationPanelMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private int observable = -1;

    public EditSpriteAnimationPanelMediator() {
        super(NAME, new EditSpriteAnimationPanel());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        viewComponent.setEmpty("No item selected");
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.ITEM_SELECTION_CHANGED,
                MsgAPI.EMPTY_SPACE_CLICKED,
                UISpriteAnimationItemProperties.EDIT_ANIMATIONS_CLICKED,
                EditSpriteAnimationPanel.ADD_BUTTON_PRESSED,
                EditSpriteAnimationPanel.DELETE_BUTTON_PRESSED,
                WindowMenu.SPRITE_ANIMATIONS_EDITOR_OPEN
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);

        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();

        switch (notification.getName()) {
            case WindowMenu.SPRITE_ANIMATIONS_EDITOR_OPEN:
                viewComponent.show(uiStage);
                break;
            case UISpriteAnimationItemProperties.EDIT_ANIMATIONS_CLICKED:
                viewComponent.show(uiStage);
                break;
            case MsgAPI.ITEM_SELECTION_CHANGED:
                Set<Integer> selection = notification.getBody();
                if(selection.size() == 1) {
                    int entity = selection.iterator().next();
                    if(EntityUtils.getType(entity) == EntityFactory.SPRITE_TYPE) {
                        setObservable(entity);
                    } else {
                        observable = -1;
                        viewComponent.setEmpty("Selected item is not a sprite animation");
                    }
                }

            break;
            case MsgAPI.EMPTY_SPACE_CLICKED:
                setObservable(-1);
                break;
            case EditSpriteAnimationPanel.ADD_BUTTON_PRESSED:
                addAnimation();
                updateView();
            break;
            case EditSpriteAnimationPanel.DELETE_BUTTON_PRESSED:
                removeAnimation(notification.getBody());
                updateView();
            break;
        }
    }

    private void setObservable(int animation) {
        observable = animation;
        updateView();
        viewComponent.setName("");
        viewComponent.setFrameFrom(0);
        viewComponent.setFrameTo(0);
    }

    private void updateView() {
        if(observable == -1) {
            viewComponent.setEmpty("No item selected");
        } else {
            SpriteAnimationComponent spriteAnimationComponent = SandboxComponentRetriever.get(observable, SpriteAnimationComponent.class);
            viewComponent.updateView(spriteAnimationComponent.frameRangeMap);
        }
    }

    private void addAnimation() {
        String name = viewComponent.getName();
        int frameFrom = viewComponent.getFrameFrom();
        int frameTo = viewComponent.getFrameTo();

        SpriteAnimationComponent spriteAnimationComponent = SandboxComponentRetriever.get(observable, SpriteAnimationComponent.class);
        spriteAnimationComponent.frameRangeMap.put(name, new FrameRange(name, frameFrom, frameTo));

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, observable);
    }

    private void removeAnimation(String name) {
        SpriteAnimationComponent spriteAnimationComponent = SandboxComponentRetriever.get(observable, SpriteAnimationComponent.class);
        spriteAnimationComponent.frameRangeMap.remove(name);

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, observable);
    }
}
