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

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.event.NumberSelectorOverlapListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by azakhary on 4/16/2015.
 */
public class UISpriteAnimationItemProperties extends UIItemCollapsibleProperties {

    public static final String EDIT_ANIMATIONS_CLICKED = "games.rednblack.editor.view.ui.properties.panels.UISpriteAnimationItemProperties" + ".EDIT_ANIMATIONS_CLICKED";

    private Spinner fpsSelector;
    private VisSelectBox<String> animationsSelectBox;
    private VisSelectBox<String> playModesSelectBox;
    private VisTextButton editAnimationsButton;
    private Array<String> playModeNames = new Array<>();

    public HashMap<Animation.PlayMode, String> playModes = new HashMap<>();

    public UISpriteAnimationItemProperties() {
        super("Sprite Animation");

        fpsSelector = StandardWidgetsFactory.createNumberSelector(0, 120);
        animationsSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        playModesSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        editAnimationsButton = new VisTextButton("Edit animations");

        mainTable.add(StandardWidgetsFactory.createLabel("FPS:", Align.right)).padRight(5).fillX();
        mainTable.add(fpsSelector).width(50).left();
        mainTable.row().padTop(5);

        mainTable.add(StandardWidgetsFactory.createLabel("Play mode:", Align.right)).padRight(5).fillX();
        mainTable.add(playModesSelectBox).width(120);
        mainTable.row().padTop(5);

        mainTable.add(StandardWidgetsFactory.createLabel("Animation:", Align.right)).padRight(5).fillX();
        mainTable.add(animationsSelectBox).width(120);
        mainTable.row().padTop(5);
        mainTable.add(editAnimationsButton).right().colspan(2);

        setPlayModes();

        setListeners();
    }

    public void setFPS(int fps) {
        ((IntSpinnerModel)fpsSelector.getModel()).setValue(fps);
    }

    public int getFPS() {
        return ((IntSpinnerModel)fpsSelector.getModel()).getValue();
    }

    public void setPlayModes() {
        playModes.clear();
        playModeNames.clear();

        playModes.put(Animation.PlayMode.LOOP, "Loop");
        playModeNames.add("Loop");
        playModes.put(Animation.PlayMode.NORMAL, "Normal");
        playModeNames.add("Normal");
        playModes.put(Animation.PlayMode.REVERSED, "Reversed");
        playModeNames.add("Reversed");
        playModes.put(Animation.PlayMode.LOOP_REVERSED, "Loop reversed");
        playModeNames.add("Loop reversed");
        playModes.put(Animation.PlayMode.LOOP_PINGPONG, "Loop ping-pong");
        playModeNames.add("Loop ping-pong");
        playModes.put(Animation.PlayMode.LOOP_RANDOM, "Loop random");
        playModeNames.add("Loop random");

        playModesSelectBox.setItems(playModeNames);
    }

    public Animation.PlayMode getPlayMode() {
        for (Map.Entry<Animation.PlayMode, String> entry : playModes.entrySet()) {
            Animation.PlayMode key = entry.getKey();
            String value = entry.getValue();
            if(playModesSelectBox.getSelected().equals(value)) {
                return key;
            }
        }
        return Animation.PlayMode.LOOP;
    }

    public void setPlayMode(Animation.PlayMode playMode) {
        playModesSelectBox.setSelected(playModes.get(playMode));
    }

    public Array<String> getAnimations() {
        return animationsSelectBox.getItems();
    }

    public void setAnimations(Array<String> animations) {
        animationsSelectBox.setItems(animations);
    }

    public String getSelectedAnimation() {
        return animationsSelectBox.getSelected();
    }

    public void setSelectedAnimation(String currentAnimationName) {
        animationsSelectBox.setSelected(currentAnimationName);
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }

    private void setListeners() {
        fpsSelector.addListener(new NumberSelectorOverlapListener(getUpdateEventName()));

        animationsSelectBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        playModesSelectBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));

        editAnimationsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(EDIT_ANIMATIONS_CLICKED);
            }
        });
    }
}
