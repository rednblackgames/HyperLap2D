package games.rednblack.h2d.common.view.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.TableUtils;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.h2d.common.view.ui.listener.MultipleInputDialogListener;

public class MultipleInputDialog extends VisWindow {
    private MultipleInputDialogListener listener;
    private Array<VisTextField> fields;
    private VisTextButton okButton;
    private VisTextButton cancelButton;

    public MultipleInputDialog(String title, String[] fieldTitles, boolean cancelable, InputValidator validator, MultipleInputDialogListener listener) {
        super(title);
        this.listener = listener;
        fields = new Array<>();

        TableUtils.setSpacingDefaults(this);
        setModal(true);

        if (cancelable) {
            addCloseButton();
            closeOnEscape();
        }

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.setIgnoreSpacing(true);
        buttonBar.setButton(ButtonBar.ButtonType.CANCEL, cancelButton = new VisTextButton(ButtonBar.ButtonType.CANCEL.getText()));
        buttonBar.setButton(ButtonBar.ButtonType.OK, okButton = new VisTextButton(ButtonBar.ButtonType.OK.getText()));

        VisTable fieldTable = new VisTable(true);

        if (validator == null) {
            for (int i = 0; i < fieldTitles.length;  i++) {
                fields.add(new VisTextField());
            }
        } else {
            for (int i = 0; i < fieldTitles.length;  i++) {
                fields.add(new VisValidatableTextField(validator));
            }
        }

        if (fieldTitles.length > 0) {
            for (int i = 0; i < fieldTitles.length;  i++) {
                fieldTable.add(new VisLabel(fieldTitles[i]));
                fieldTable.add(fields.get(i)).expand().fill().row();
            }
        }

        add(fieldTable).padTop(3).spaceBottom(4);
        row();
        add(buttonBar.createTable()).padBottom(3);

        addListeners();

        if (validator != null) {
            addValidatableFieldListener(fields);
            boolean valid = true;
            for (VisTextField field : fields) {
                if (!field.isInputValid()) {
                    valid = false;
                    break;
                }
            }
            okButton.setDisabled(!valid);
        }

        pack();
        centerWindow();
    }

    @Override
    protected void close() {
        super.close();
        listener.canceled();
    }

    @Override
    protected void setStage(Stage stage) {
        super.setStage(stage);
        if (stage != null && fields.size > 0)
            fields.get(0).focusField();
    }

    public MultipleInputDialog setText(String[] text) {
        return setText(text, false);
    }

    /**
     * @param selectText if true text will be selected (this can be useful if you want to allow user quickly erase all text).
     */
    public MultipleInputDialog setText(String[] texts, boolean selectText) {
        for (int i = 0; i < texts.length; i++) {
            fields.get(i).setText(texts[i]);
            fields.get(i).setCursorPosition(texts[i].length());
            if (selectText) {
                fields.get(i).selectAll();
            }
        }
        return this;
    }

    private MultipleInputDialog addValidatableFieldListener(final Array<VisTextField> fields) {
        for (VisTextField field : fields) {
            field.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    boolean valid = true;
                    for (VisTextField field : fields) {
                        if (!field.isInputValid()) {
                            valid = false;
                            break;
                        }
                    }
                    okButton.setDisabled(!valid);
                }
            });
        }
        return this;
    }

    private void addListeners() {
        okButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String[] results = new String[fields.size];
                for (int i = 0; i < fields.size; i++)
                    results[i] = fields.get(i).getText();
                listener.finished(results);
                fadeOut();
            }
        });

        cancelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                close();
            }
        });

        for (VisTextField field : fields) {
            field.addListener(new InputListener() {
                @Override
                public boolean keyDown(InputEvent event, int keycode) {
                    if (keycode == Input.Keys.ENTER && !okButton.isDisabled()) {
                        String[] results = new String[fields.size];
                        for (int i = 0; i < fields.size; i++)
                            results[i] = fields.get(i).getText();
                        listener.finished(results);
                        fadeOut();
                    }
                    return super.keyDown(event, keycode);
                }
            });
        }
    }
}
