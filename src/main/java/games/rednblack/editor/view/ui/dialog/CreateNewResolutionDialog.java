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

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class CreateNewResolutionDialog extends H2DDialog {
	private static final String prefix = "games.rednblack.editor.view.ui.dialog.CreateNewResolutionDialog";
	public static final String CREATE_BTN_CLICKED = prefix + ".CREATE_BTN_CLICKED";
	public static final String CLOSE_DIALOG = prefix + ".CLOSE_DIALOG";

    private final VisTextField nameVisTextField;
    private VisTextField widthVisTextField;
    private VisTextField heightVisTextField;
    private ButtonGroup<VisRadioButton> buttonGroup;
    private VisRadioButton basedOnWidthRadioButton;
    private VisRadioButton basedOnHeightRadioButton;

    public CreateNewResolutionDialog() {
        super("Create New Resolution");
        addCloseButton();
        VisTable mainTable = new VisTable();

        mainTable.padTop(6).padRight(6).padBottom(22);
        mainTable.add("Name:").padRight(5).right();
        nameVisTextField = StandardWidgetsFactory.createValidableTextField("light", new StringNameValidator());
        mainTable.add(nameVisTextField).colspan(3).width(177).height(21);
        mainTable.row().padTop(10);
        mainTable.add("Resolution:").padRight(5).right().top();
        mainTable.add(getDimensionsTable()).left();
        mainTable.row().padTop(20);
        VisTextButton createBtn = StandardWidgetsFactory.createTextButton("Create", "red");
        createBtn.addListener(new CrateButtonClickListener());
        getButtonsTable().add(createBtn).width(93).height(24).colspan(2);
        getContentTable().add(mainTable);
    }

    private Table getDimensionsTable() {
        buttonGroup = new ButtonGroup<>();
        VisTextField.TextFieldFilter.DigitsOnlyFilter digitsOnlyFilter = new VisTextField.TextFieldFilter.DigitsOnlyFilter();
        VisTable dimensionsTable = new VisTable();
        widthVisTextField = StandardWidgetsFactory.createTextField("light", digitsOnlyFilter);
        dimensionsTable.add(new VisLabel("Width:")).left().padRight(3);
        dimensionsTable.add(widthVisTextField).width(45).height(21).padRight(7);
        basedOnWidthRadioButton = new VisRadioButton(null);
        dimensionsTable.add(basedOnWidthRadioButton);
        dimensionsTable.add("Based on");
        dimensionsTable.row().padTop(10);
        heightVisTextField = StandardWidgetsFactory.createTextField("light", digitsOnlyFilter);
        dimensionsTable.add(new VisLabel("Height:")).left().padRight(7);
        dimensionsTable.add(heightVisTextField).width(45).height(21).left();
        basedOnHeightRadioButton = new VisRadioButton(null);
        dimensionsTable.add(basedOnHeightRadioButton);
        dimensionsTable.add("Based on");
        buttonGroup.add(basedOnWidthRadioButton);
        buttonGroup.add(basedOnHeightRadioButton);
        return dimensionsTable;
    }

	@Override
	public void close() {
    	super.close();
		HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
		facade.sendNotification(CLOSE_DIALOG);
	}

	private class CrateButtonClickListener extends ClickListener {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            if (nameVisTextField.getText().equals("") || widthVisTextField.getText().equals("") || heightVisTextField.getText().equals("")) {
                return;
            }
            HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
            ResolutionEntryVO resolutionEntryVO = new ResolutionEntryVO();
            resolutionEntryVO.name = nameVisTextField.getText();
            resolutionEntryVO.width = Integer.parseInt(widthVisTextField.getText());
            resolutionEntryVO.height = Integer.parseInt(heightVisTextField.getText());
            resolutionEntryVO.base = buttonGroup.getCheckedIndex();
            facade.sendNotification(CREATE_BTN_CLICKED, resolutionEntryVO);
        }
    }
}
