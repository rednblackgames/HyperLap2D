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
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.spinner.IntSpinnerModel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.renderer.data.FrameRange;
import games.rednblack.editor.view.ui.validator.EmptyOrDefaultValidator;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.FormRow;
import games.rednblack.h2d.common.view.ui.ListTable;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
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

    /** The range every sprite animation has, covering the whole sheet: it cannot be deleted. */
    private static final String DEFAULT_RANGE = "Default";
    private static final int MIN_WIDTH = 480;

    private final Facade facade;

    private final VisTextField nameField;
    private final VisTextButton addButton;
    private final VisTable mainTable;

    private Spinner fromFrameField;
    private Spinner toFrameField;

    public EditSpriteAnimationPanel() {
        super("Sprite Animation Ranges");
        addCloseButton();

        facade = Facade.getInstance();

        nameField = StandardWidgetsFactory.createValidableTextField(new EmptyOrDefaultValidator());
        addButton = StandardWidgetsFactory.createTextButton("Add");
        addButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (getFrameFrom() <= getFrameTo() && nameField.isInputValid())
                    facade.sendNotification(ADD_BUTTON_PRESSED);
            }
        });

        createFrameSpinners(100);

        mainTable = new VisTable();
        getContentTable().add(mainTable).growX();
    }

    /** The spinners are bound to the sheet length, so they are rebuilt whenever it is known. */
    private void createFrameSpinners(int maxFrame) {
        fromFrameField = StandardWidgetsFactory.createNumberSelector(0, maxFrame);
        toFrameField = StandardWidgetsFactory.createNumberSelector(0, maxFrame);
    }

    public void setEmpty(String text) {
        mainTable.clear();
        PropertyGrid.on(mainTable).dialogScale().padPanel().wideCentered(PropertyGrid.value(text));
        invalidateHeight();
    }

    public void updateView(Map<String, FrameRange> frameRangeMap) {
        mainTable.clear();
        createFrameSpinners(frameRangeMap.get(DEFAULT_RANGE).endFrame);

        PropertyGrid grid = PropertyGrid.on(mainTable).dialogScale().padPanel();
        grid.section("New range");
        grid.wideContent(new FormRow()
                .label("Name").field(nameField)
                .label("From").compact(fromFrameField)
                .label("To").compact(toFrameField)
                .action(addButton));

        grid.section("Ranges");
        grid.wideContent(createRangesList(frameRangeMap));

        invalidateHeight();
    }

    /** Clicking a range loads it back into the form above, ready to be edited and re-added. */
    private ListTable createRangesList(Map<String, FrameRange> frameRangeMap) {
        ListTable list = new ListTable("Animation", "From", "To");
        if (frameRangeMap.isEmpty()) {
            return list.message("This sprite animation has no ranges");
        }
        for (Map.Entry<String, FrameRange> entry : frameRangeMap.entrySet()) {
            String animationName = entry.getKey();
            FrameRange range = entry.getValue();

            ListTable.ItemRow row = list.item(animationName,
                    String.valueOf(range.startFrame), String.valueOf(range.endFrame));
            row.onClick(() -> {
                setName(animationName);
                setFrameFrom(range.startFrame);
                setFrameTo(range.endFrame);
            });

            if (!animationName.equals(DEFAULT_RANGE)) {
                VisImageButton deleteButton = StandardWidgetsFactory.createImageButton("trash-button");
                deleteButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        facade.sendNotification(DELETE_BUTTON_PRESSED, animationName);
                    }
                });
                row.action(deleteButton);
            }
        }
        return list;
    }

    public String getName() {
        return nameField.getText();
    }

    public int getFrameFrom() {
        return ((IntSpinnerModel) fromFrameField.getModel()).getValue();
    }

    public int getFrameTo() {
        return ((IntSpinnerModel) toFrameField.getModel()).getValue();
    }

    public void setName(String name) {
        nameField.setText(name);
    }

    public void setFrameFrom(int from) {
        ((IntSpinnerModel) fromFrameField.getModel()).setValue(from);
    }

    public void setFrameTo(int to) {
        ((IntSpinnerModel) toFrameField.getModel()).setValue(to);
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
    }
}
