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

package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Pools;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;

/**
 * Created by sargis on 5/4/15.
 */
public abstract class UIResourcesTab extends ImageTab {

    protected final VisTable contentTable;

    public String searchString = "";

    public UIResourcesTab() {
        super(false, false);
        contentTable = new VisTable();
        contentTable.padTop(8).padLeft(7);
        contentTable.add(StandardWidgetsFactory.createLabel("Search:")).padLeft(1).padBottom(6);
        contentTable.add(createTextField()).padLeft(0).padRight(7).fillX().padBottom(4);
        contentTable.add(createFilterButton()).padRight(7).padTop(-4);
        contentTable.row();

        VisScrollPane scrollPane = crateScrollPane();
        contentTable.add(scrollPane).padTop(4).colspan(3).maxHeight(Gdx.graphics.getHeight() * 0.22f).expandX().fillX();
    }

    protected VisTextField createTextField() {
        VisTextField visTextField = StandardWidgetsFactory.createTextField();
        visTextField.setMessageText(getTabTitle());
        visTextField.setTextFieldListener(new VisTextField.TextFieldListener() {
            @Override
            public void keyTyped(VisTextField textField, char c) {
                searchString    =   textField.getText();
                HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
                facade.sendNotification(MsgAPI.UPDATE_RESOURCES_LIST);
            }
        });
        return visTextField;
    }

    protected VisImageButton createFilterButton () {
        VisImageButton button = StandardWidgetsFactory.createImageButton("filter-button");
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
                Vector2 pos = Pools.obtain(Vector2.class);
                pos.set(0, 0);
                button.localToStageCoordinates(pos);
                Object[] payload = new Object[]{pos.x, pos.y, getTabTitle()};
                facade.sendNotification(UIFilterMenu.SHOW_FILTER_MENU, payload);
                Pools.free(pos);
            }
        });
        return button;
    }

    @Override
    public Table getContentTable() {
        return contentTable;
    }

    protected abstract VisScrollPane crateScrollPane();

}
