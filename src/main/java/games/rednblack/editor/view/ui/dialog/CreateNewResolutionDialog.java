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
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

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

    private final Facade facade;

    private static final int MIN_WIDTH = 420;
    /** Which side the asset scale for this resolution is derived from, see ResolutionEntryVO.base. */
    private static final String SCALE_TOOLTIP =
            "The side compared with the original resolution to work out how much to scale the "
                    + "project's images for this one.";

    public CreateNewResolutionDialog(Facade facade) {
        super("Create New Resolution");
        this.facade = facade;
        addCloseButton();
        closeOnEscape();

        VisTextField.TextFieldFilter.DigitsOnlyFilter digitsOnly = new VisTextField.TextFieldFilter.DigitsOnlyFilter();
        nameVisTextField = StandardWidgetsFactory.createValidableTextField("light", new StringNameValidator());
        widthVisTextField = StandardWidgetsFactory.createTextField("light", digitsOnly);
        heightVisTextField = StandardWidgetsFactory.createTextField("light", digitsOnly);

        // the order matters: the checked index becomes ResolutionEntryVO.base, 0 for width, 1 for height
        basedOnWidthRadioButton = new VisRadioButton("Width");
        basedOnHeightRadioButton = new VisRadioButton("Height");
        buttonGroup = new ButtonGroup<>();
        buttonGroup.add(basedOnWidthRadioButton);
        buttonGroup.add(basedOnHeightRadioButton);
        // without a default nothing is checked and the base would come out as -1
        basedOnWidthRadioButton.setChecked(true);

        VisTable scaleBy = new VisTable();
        scaleBy.add(basedOnWidthRadioButton).left();
        scaleBy.add(basedOnHeightRadioButton).left().padLeft(PropertyGrid.PAIR_GAP * 2);

        VisTable body = new VisTable();
        getContentTable().add(body).growX();
        PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();
        grid.section("Resolution");
        grid.row("Name", nameVisTextField);
        grid.rowUnit("Width", widthVisTextField, "px");
        grid.rowUnit("Height", heightVisTextField, "px");
        grid.rowCompact("Scale by", scaleBy);
        grid.tooltipLastRow(SCALE_TOOLTIP);

        VisTextButton createBtn = StandardWidgetsFactory.createTextButton("Create", "accent");
        createBtn.addListener(new CrateButtonClickListener());
        getButtonsTable().add(createBtn).pad(2);
        getCell(getButtonsTable()).right();
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), MIN_WIDTH);
    }

	@Override
	public void close() {
    	super.close();
		facade.sendNotification(CLOSE_DIALOG);
	}

	private class CrateButtonClickListener extends ClickListener {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            super.clicked(event, x, y);
            if (nameVisTextField.getText().equals("") || widthVisTextField.getText().equals("") || heightVisTextField.getText().equals("")) {
                return;
            }
            ResolutionEntryVO resolutionEntryVO = new ResolutionEntryVO();
            resolutionEntryVO.name = nameVisTextField.getText();
            resolutionEntryVO.width = Integer.parseInt(widthVisTextField.getText());
            resolutionEntryVO.height = Integer.parseInt(heightVisTextField.getText());
            resolutionEntryVO.base = buttonGroup.getCheckedIndex();
            facade.sendNotification(CREATE_BTN_CLICKED, resolutionEntryVO);
        }
    }
}
