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

package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.InputFileWidget;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;

public class NewProjectDialog extends H2DDialog {
    private static final String prefix = "games.rednblack.editor.view.ui.dialog.NewProjectDialog";
    public static final String CREATE_BTN_CLICKED = prefix + ".CREATE_BTN_CLICKED";
    private static final String DEFAULT_ORIGIN_WIDTH = "1920";
    private static final String DEFAULT_ORIGIN_HEIGHT = "1080";
    private static final String DEFAULT_PPWU = "1";

    private final InputFileWidget workspacePathField;
    private final VisValidatableTextField projectName;
    private VisValidatableTextField originWidthTextField;
    private VisValidatableTextField originHeightTextField;
    private String defaultWorkspacePath;
    private VisValidatableTextField pixelsPerWorldUnitField;
    private VisLabel worldSizeLabel;

    NewProjectDialog() {
        super("Create New Project");

        setModal(true);
        addCloseButton();
        VisTable mainTable = new VisTable();
        mainTable.pad(6);
        //
        VisLabel projectNameLavel = new VisLabel("Project Name:");
        mainTable.add(projectNameLavel).right().padRight(5);
        projectName = StandardWidgetsFactory.createValidableTextField(new StringNameValidator());
        mainTable.add(projectName).height(21).expandX().fillX();
        //
        mainTable.row().padTop(10);
        //
        mainTable.add(new VisLabel("Project Folder:")).right().padRight(5);
        workspacePathField = new InputFileWidget(FileChooser.Mode.OPEN, FileChooser.SelectionMode.DIRECTORIES, false);
        workspacePathField.setTextFieldWidth(156);
        mainTable.add(workspacePathField);
        //
        mainTable.row().colspan(2);
        mainTable.addSeparator().padTop(10).padBottom(10);
        mainTable.row();
        //
        mainTable.add(new VisLabel("Original Size")).top().left().padRight(5);
        mainTable.add(getDimensionsTable()).left();
        mainTable.row().padTop(10);

        mainTable.add(new VisLabel("World Size: ")).top().left().padRight(5);
        worldSizeLabel = StandardWidgetsFactory.createLabel("0 x 0", "default", Align.left);
        mainTable.add(worldSizeLabel).top().left().padRight(5);
        getContentTable().add(mainTable);

        VisTextButton createBtn = StandardWidgetsFactory.createTextButton("Create", "red");
        createBtn.addListener(new BtnClickListener(CREATE_BTN_CLICKED));
        getButtonsTable().add(createBtn).width(93).height(25).colspan(2);

        updateWorldSize();
    }

    private Table getDimensionsTable() {
        VisTextField.TextFieldFilter.DigitsOnlyFilter digitsOnlyFilter = new VisTextField.TextFieldFilter.DigitsOnlyFilter();
        VisTable dimensionsTable = new VisTable();
        originWidthTextField = StandardWidgetsFactory.createValidableTextField(DEFAULT_ORIGIN_WIDTH, "light", new Validators.IntegerValidator(), digitsOnlyFilter);
        originWidthTextField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateWorldSize();
            }
        });
        dimensionsTable.add(new VisLabel("Width : ")).left().padRight(3);
        dimensionsTable.add(originWidthTextField).width(45).height(21).padRight(3);
        dimensionsTable.add("px").left();
        dimensionsTable.row().padTop(10);
        originHeightTextField = StandardWidgetsFactory.createValidableTextField(DEFAULT_ORIGIN_HEIGHT, "light", new Validators.IntegerValidator(), digitsOnlyFilter);
        originHeightTextField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateWorldSize();
            }
        });
        dimensionsTable.add(new VisLabel("Height : ")).left().padRight(3);
        dimensionsTable.add(originHeightTextField).width(45).height(21).left();
        dimensionsTable.add("px").left();
        dimensionsTable.row().padTop(10);
        pixelsPerWorldUnitField = StandardWidgetsFactory.createValidableTextField(DEFAULT_PPWU, "light", new Validators.IntegerValidator(), digitsOnlyFilter);
        pixelsPerWorldUnitField.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateWorldSize();
            }
        });
        dimensionsTable.add(new VisLabel("World Unit : ")).left().padRight(3);
        dimensionsTable.add(pixelsPerWorldUnitField).width(45).height(21).left();
        dimensionsTable.add("px").left();
        return dimensionsTable;
    }

    @Override
    public VisDialog show(Stage stage, Action action) {
        originWidthTextField.setText(DEFAULT_ORIGIN_WIDTH);
        originHeightTextField.setText(DEFAULT_ORIGIN_HEIGHT);
        workspacePathField.resetData();
        workspacePathField.setValue(new FileHandle(defaultWorkspacePath));
        return super.show(stage, action);
    }

    public String getOriginWidth() {
        return originWidthTextField.getText();
    }

    public String getPixelPerWorldUnit() {
        return pixelsPerWorldUnitField.getText();
    }

    public String getOriginHeight() {
        return originHeightTextField.getText();
    }

    public String getDefaultWorkspacePath() {
        return defaultWorkspacePath;
    }

    public void setDefaultWorkspacePath(String defaultWorkspacePath) {
        this.defaultWorkspacePath = defaultWorkspacePath;
    }

    private void updateWorldSize() {
        int originW = NumberUtils.toInt(getOriginWidth());
        int originH = NumberUtils.toInt(getOriginHeight());

        int ppwu = NumberUtils.toInt(getPixelPerWorldUnit(), 1);

        worldSizeLabel.setText(originW / ppwu + " x " + originH / ppwu);
    }

    private class BtnClickListener extends ClickListener {
        private final String command;

        public BtnClickListener(String command) {
            this.command = command;
        }

        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
            if (projectName.isInputValid() && pixelsPerWorldUnitField.isInputValid() && originHeightTextField.isInputValid() && originWidthTextField.isInputValid()) {
                facade.sendNotification(command, workspacePathField.getValue().path() + File.separator + projectName.getText());
            }
        }
    }
}
