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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.utils.StandardWidgetsFactory;

/**
 * Created by azakhary on 4/15/2015.
 */
public class UIGridBox extends UIBaseBox {

    private static final String TAG = "UIGridBox";
    private static final String GRID_BOX_PREFIX = "games.rednblack.editor.view.ui.box.UIGridBox";
    public static final String GRID_SIZE_TEXT_FIELD_UPDATED = GRID_BOX_PREFIX + ".GRID_SIZE_TEXT_FIELD_UPDATED";
    public static final String LOCK_LINES_CHECKBOX_FIELD_UPDATED = GRID_BOX_PREFIX + ".LOCK_LINES_CHECKBOX_FIELD_UPDATED";
    private VisValidatableTextField gridSizeTextField;

	public UIGridBox() {
        init();
        setVisible(false);
    }

    @Override
    public void update() {
        setVisible(true);
    }

    private void init() {
		VisCheckBox lockLinesCheckBox = StandardWidgetsFactory.createCheckBox("Lock lines");
        lockLinesCheckBox.addListener(new CheckBoxChangeListener(LOCK_LINES_CHECKBOX_FIELD_UPDATED));
        add(lockLinesCheckBox);
		addSeparator(true).padRight(13).padLeft(13);

        VisLabel lbl = new VisLabel("Grid Size:");
        add(lbl).padRight(4);
        gridSizeTextField = StandardWidgetsFactory.createValidableTextField("light", new Validators.IntegerValidator());
        gridSizeTextField.addListener(new KeyboardListener(GRID_SIZE_TEXT_FIELD_UPDATED));
		gridSizeTextField.setAlignment(Align.center);
        add(gridSizeTextField).width(30);
    }

    public void setGridSize(int gridSize) {
        gridSizeTextField.setText(gridSize + "");
    }

    public void setLockLines(boolean lockLines) {
        if (lockLines) {
            Gdx.app.log(TAG, "Lines locked");
        } else {
            Gdx.app.log(TAG, "Lines unlocked");
        }
    }
}
