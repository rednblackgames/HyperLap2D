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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.renderer.data.FrameRange;
import games.rednblack.editor.view.ui.validator.EmptyOrDefaultValidator;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

import java.util.Map;

/**
 * Created by azakhary on 5/12/2015.
 */
public class EditSpriteAnimationPanel extends UIDraggablePanel {
    public static final String PREFIX = "games.rednblack.editor.view.ui.panel.EditSpriteAnimationDialog";
    public static final String ADD_BUTTON_PRESSED = PREFIX + ".ADD_BUTTON_PRESSED";
    public static final String DELETE_BUTTON_PRESSED = PREFIX + ".DELETE_BUTTON_PRESSED";

    private final Facade facade;

    private VisTextField nameField;
    private Spinner fromFrameField;
    private Spinner toFrameField;
    private VisTextButton addButton;

    private final VisTable animationsList;

    public EditSpriteAnimationPanel() {
        super("Edit Sprite Animation Ranges");
        addCloseButton();

        facade = Facade.getInstance();

        VisTable mainTable = new VisTable();

        animationsList = new VisTable();

        createNewAnimationTable(100);

        mainTable.row();
        mainTable.add(animationsList).fillX();
        mainTable.row();

        getContentTable().add(mainTable).pad(10);
    }

    private void createNewAnimationTable(int maxFrame) {
        nameField = StandardWidgetsFactory.createValidableTextField(new EmptyOrDefaultValidator());
        fromFrameField = StandardWidgetsFactory.createNumberSelector(0, maxFrame);
        toFrameField = StandardWidgetsFactory.createNumberSelector(0, maxFrame);
        addButton = new VisTextButton("Add");

        animationsList.add(nameField).width(120);
        animationsList.add(fromFrameField).padLeft(5);
        animationsList.add(toFrameField).padLeft(5);
        animationsList.add(addButton).padLeft(7).padRight(3);
        animationsList.row().padTop(5);
        initListeners();
    }

    public void setEmpty(String text) {
        animationsList.clear();
        animationsList.add(text).row();
        invalidateHeight();
    }

    public void updateView(Map<String, FrameRange> frameRangeMap) {
        animationsList.clear();
        createNewAnimationTable(frameRangeMap.get("Default").endFrame);

        animationsList.add("Animation Name").expandX();
        animationsList.add("Start").expandX();
        animationsList.add("End").expandX();
        animationsList.add("").expandX();
        animationsList.row();
        animationsList.addSeparator().colspan(4).expandX().fillX().row();

        for (Map.Entry<String, FrameRange> entry : frameRangeMap.entrySet()) {
            String animationName = entry.getKey();
            FrameRange range = entry.getValue();

            VisTable nameTbl = new VisTable();
            nameTbl.setBackground(VisUI.getSkin().getDrawable("layer-bg"));
            nameTbl.add(StandardWidgetsFactory.createLabel(animationName)).left();
            VisTable startTbl = new VisTable();
            startTbl.setBackground(VisUI.getSkin().getDrawable("layer-bg"));
            startTbl.add(StandardWidgetsFactory.createLabel(range.startFrame + "")).left();
            VisTable endTbl = new VisTable();
            endTbl.setBackground(VisUI.getSkin().getDrawable("layer-bg"));
            endTbl.add(StandardWidgetsFactory.createLabel(range.endFrame + "")).left();

            VisImageButton trashBtn = new VisImageButton("trash-button");

            animationsList.add(nameTbl).height(20).expandX().fillX();
            animationsList.add(startTbl).height(20).expandX().fillX();
            animationsList.add(endTbl).height(20).expandX().fillX();

            if (!animationName.equals("Default"))
                animationsList.add(trashBtn).padLeft(10);
            else
                animationsList.add();
            animationsList.row().padBottom(2);

            trashBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    facade.sendNotification(DELETE_BUTTON_PRESSED, animationName);
                }
            });
        }

        invalidateHeight();
    }

    private void initListeners() {
        addButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getFrameFrom() <= getFrameTo() && nameField.isInputValid())
                    facade.sendNotification(ADD_BUTTON_PRESSED);
            }
        });
    }

    public String getName() {
        return nameField.getText();
    }

    public int getFrameFrom() {
        return ((IntSpinnerModel)fromFrameField.getModel()).getValue();
    }

    public int getFrameTo() {
        return ((IntSpinnerModel)toFrameField.getModel()).getValue();
    }


    public void setName(String name) {
        nameField.setText(name);
    }

    public void setFrameFrom(int from) {
        ((IntSpinnerModel)fromFrameField.getModel()).setValue(from);
    }

    public void setFrameTo(int to) {
        ((IntSpinnerModel)toFrameField.getModel()).setValue(to);
    }

}
