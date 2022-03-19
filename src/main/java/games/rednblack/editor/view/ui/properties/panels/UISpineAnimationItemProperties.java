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

import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 4/16/2015.
 */
public class UISpineAnimationItemProperties extends UIItemCollapsibleProperties {

    private VisSelectBox<String> animationsSelectBox, skinSelectBox;

    public UISpineAnimationItemProperties() {
        super("Spine Animations");
        animationsSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        skinSelectBox = StandardWidgetsFactory.createSelectBox(String.class);

        mainTable.add(StandardWidgetsFactory.createLabel("Animation:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(animationsSelectBox).width(120).colspan(2).row();

        mainTable.add().padTop(7).colspan(4).row();

        mainTable.add(StandardWidgetsFactory.createLabel("Skin:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(skinSelectBox).width(120).colspan(2).row();
        setListeners();
    }

    public Array<String> getAnimations() {
        return animationsSelectBox.getItems();
    }

    public Array<String> getSkins() {
        return skinSelectBox.getItems();
    }

    public void setAnimations(Array<String> animations) {
        animationsSelectBox.setItems(animations);
    }

    public void setSkins(Array<String> animations) {
        skinSelectBox.setItems(animations);
    }

    public String getSelectedAnimation() {
        return animationsSelectBox.getSelected();
    }

    public String getSelectedSkin() {
        return skinSelectBox.getSelected();
    }

    public void setSelectedAnimation(String currentAnimationName) {
        animationsSelectBox.setSelected(currentAnimationName);
    }

    public void setSelectedSkin(String currentSkinName) {
        skinSelectBox.setSelected(currentSkinName);
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }

    private void setListeners() {
        animationsSelectBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        skinSelectBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
    }
}
