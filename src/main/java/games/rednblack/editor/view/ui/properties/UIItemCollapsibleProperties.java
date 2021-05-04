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

package games.rednblack.editor.view.ui.properties;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.CollapsibleWidget;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 4/15/2015.
 */
public abstract class UIItemCollapsibleProperties extends UIItemProperties {
    protected final String title;
    protected VisTable mainTable;
    protected VisTable header;
    protected CollapsibleWidget collapsibleWidget;

    public UIItemCollapsibleProperties(String title) {
        this.title = title;
        row().padTop(9).padBottom(6);
        add(crateHeaderTable()).expandX().fillX().padBottom(7);
        createCollapsibleWidget();
    }

    public Table crateHeaderTable() {
        header = new VisTable();
        header.setTouchable(Touchable.enabled);
        header.setBackground(VisUI.getSkin().getDrawable("expandable-properties-active-bg"));
        header.add(StandardWidgetsFactory.createLabel(title)).left().expandX().padRight(6).padLeft(8);
        VisImageButton button = StandardWidgetsFactory.createImageButton("expandable-properties-button");
        header.add(button).padRight(8);
        header.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                collapse(header);
            }
        });
        return header;
    }

    public void collapse(VisTable header) {
        collapsibleWidget.setCollapsed(!collapsibleWidget.isCollapsed());
        header.setBackground(VisUI.getSkin().getDrawable("expandable-properties-" + (collapsibleWidget.isCollapsed() ? "inactive" : "active") + "-bg"));
    }

    private void createCollapsibleWidget() {
        mainTable = new VisTable();
        collapsibleWidget = new CollapsibleWidget(mainTable);
        row();
        add(collapsibleWidget).expand();
    }
}
