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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;

/**
 * Created by sargis on 4/29/15.
 */
public class UICollapsibleBox extends VisWindow {
    protected static final int BOX_DEFAULT_WIDTH = 250;

    private final VisImageButton collapsibleButton;
    private final VisTable mainTable;
    protected CollapsibleWidget collapsibleWidget;

    public UICollapsibleBox(String title) {
        this(title, BOX_DEFAULT_WIDTH);
    }

    public UICollapsibleBox(String title, int width) {
        super(title);
        mainTable = new VisTable();
        mainTable.top();
        setStyle(VisUI.getSkin().get("box", WindowStyle.class));
        collapsibleButton = new VisImageButton("close-box");
        collapsibleButton.setOrigin(Align.center);
        collapsibleButton.setTransform(true);
        collapsibleButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggle();
            }
        });
        getTitleLabel().setAlignment(Align.left);
        getTitleTable().add(collapsibleButton).right().padRight(5);
        getTitleTable().padLeft(4).padTop(3);
        add(mainTable).width(width).padBottom(4).padLeft(5);

        // by default all collapsible panels are not visible
        setVisible(false);
        padTop(32);

        setKeepWithinParent(false);
        setKeepWithinStage(false);

        getTitleTable().addListener(new ActorGestureListener() {
            @Override
            public void tap(InputEvent event, float x, float y, int count, int button) {
                if (count == 2) toggle();
            }
        });
    }

    protected void toggle() {
        collapsibleButton.clearActions();
        collapsibleButton.addAction(Actions.rotateTo(collapsibleWidget.isCollapsed() ? 0 : -90, .1f));
        collapsibleWidget.setCollapsed(!collapsibleWidget.isCollapsed());
    }

    protected void createCollapsibleWidget(Table table) {
        collapsibleWidget = new CollapsibleWidget(table);
        collapsibleWidget.setCollapseInterpolation(Interpolation.pow5Out);
        mainTable.add(collapsibleWidget).growX().top();
    }
}
