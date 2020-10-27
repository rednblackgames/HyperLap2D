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

package games.rednblack.h2d.common.view.ui.widget;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sargis on 4/3/15.
 */
public class InputFileWidget extends VisTable {
    private static final String TAG = "InputFileWidget";

    private VisTextField textField;
    private VisTextButton browsBtn;
    private Cell<VisTextField> textFieldCell;
    private FileChooser.Mode chooserMode;
    private FileChooser.SelectionMode chooserSelectionMode;
    private boolean chooserMultiSelect;
    private FileHandle value;
    private Array<FileHandle> values;

    public InputFileWidget(FileChooser.Mode mode, FileChooser.SelectionMode selectionMode, boolean multiselectionEnabled, boolean setVisDefaults) {
        super(setVisDefaults);
        initWidget();
        initFileChooser(mode, selectionMode, multiselectionEnabled);
    }

    public InputFileWidget(FileChooser.Mode mode, FileChooser.SelectionMode selectionMode, boolean multiselectionEnabled) {
        this(mode, selectionMode, multiselectionEnabled, true);
    }

    private void initFileChooser(FileChooser.Mode mode, FileChooser.SelectionMode selectionMode, boolean multiselectionEnabled) {
        chooserMode = mode;
        chooserSelectionMode = selectionMode;
        chooserMultiSelect = multiselectionEnabled;
    }

    private void initWidget() {
        textField = StandardWidgetsFactory.createTextField("light");
        textFieldCell = add(textField).growX().fillX().padRight(8).height(21);
        browsBtn = new VisTextButton("Browse");
        add(browsBtn);
        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(() -> {
                    String selected;
                    if (chooserMode == FileChooser.Mode.OPEN) {
                        //Open
                        if (chooserSelectionMode == FileChooser.SelectionMode.DIRECTORIES) {
                            //Select dir
                            selected = TinyFileDialogs.tinyfd_selectFolderDialog("Choose a folder...", "");
                        } else {
                            //Select file
                            selected = TinyFileDialogs.tinyfd_openFileDialog("Choose a file...", null, null, null, chooserMultiSelect);
                        }
                    } else {
                        //Save
                        selected = TinyFileDialogs.tinyfd_saveFileDialog("Choose a file...", null, null, null);
                    }

                    if (selected != null) {
                        String[] files = selected.split("\\|");
                        if (files.length == 1) {
                            setValue(new FileHandle(files[0]));
                        } else {
                            //setValues(files);
                        }
                    }
                });
                executor.shutdown();
            }
        });
    }

    public FileHandle getValue() {
        if (chooserMultiSelect) {
            throw new IllegalStateException("Multiselection is enabled, use 'getValues' instead of 'getValue'");
        }
        return value;
    }

    public void setValue(FileHandle value) {
        this.value = value;
        textField.setText(value.path());
    }

    public Array<FileHandle> getValues() {
        if (!chooserMultiSelect) {
            throw new IllegalStateException("Multiselection is not enabled, use 'getValue' instead of 'getValues'");
        }
        return values;
    }

    private void setValues(Array<FileHandle> values) {
        this.values = values;
        StringBuilder path = new StringBuilder();
        for (FileHandle handle : values) {
            path.append(handle.path()).append(",");
        }

        // Remove comma in the last character.
        path.deleteCharAt(path.length() - 1);
        textField.setText(path.toString());
    }


    public void setTextFieldWidth(int textFieldWidth) {
        textFieldCell.width(textFieldWidth);
    }

    public void resetData() {
        textField.setText("");
        value = null;
        values = null;
    }
}
