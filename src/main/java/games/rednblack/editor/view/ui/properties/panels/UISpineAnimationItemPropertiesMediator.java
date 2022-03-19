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

import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.Animation;
import com.esotericsoftware.spine.Skin;
import games.rednblack.editor.controller.commands.component.UpdateSpineDataCommand;
import games.rednblack.h2d.extension.spine.SpineVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.extension.spine.SpineComponent;

public class UISpineAnimationItemPropertiesMediator extends UIItemPropertiesMediator<UISpineAnimationItemProperties> {
    private static final String TAG = UISpineAnimationItemPropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private SpineComponent spineComponent;

    Array<String> animations = new Array<>();
    Array<String> skins = new Array<>();

    public UISpineAnimationItemPropertiesMediator() {
        super(NAME, new UISpineAnimationItemProperties());
    }

    @Override
    protected void translateObservableDataToView(int entity) {
        spineComponent = SandboxComponentRetriever.get(entity, SpineComponent.class);

        animations.clear();
        for (Animation animation : spineComponent.getAnimations()) {
            animations.add(animation.getName());
        }

        viewComponent.setAnimations(animations);
        viewComponent.setSelectedAnimation(spineComponent.currentAnimationName);

        skins.clear();
        for (Skin skin : spineComponent.getSkins()) {
            skins.add(skin.getName());
        }
        viewComponent.setSkins(skins);
        viewComponent.setSelectedSkin(spineComponent.currentSkinName);
    }

    @Override
    protected void translateViewToItemData() {
        SpineVO payloadVO = new SpineVO();
        payloadVO.currentAnimationName = viewComponent.getSelectedAnimation();
        payloadVO.currentSkinName = viewComponent.getSelectedSkin();

        Object payload = UpdateSpineDataCommand.payload(observableReference, payloadVO);
        facade.sendNotification(MsgAPI.ACTION_UPDATE_SPINE_ANIMATION_DATA, payload);
    }
}
