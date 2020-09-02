package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.highlight.Highlighter;
import com.kotcrab.vis.ui.widget.HighlightTextArea;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class CodeEditorDialog extends H2DDialog {

    private HighlightTextArea textArea;
    private String notificationCallback;

    public CodeEditorDialog() {
        super("Code Editor");

        addCloseButton();
        setResizable(true);

        textArea = new HighlightTextArea("", "code-editor");
        getContentTable().add(textArea.createCompatibleScrollPane()).grow().row();

        VisTextButton cancelButton = StandardWidgetsFactory.createTextButton("Cancel");
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                close();
            }
        });
        VisTextButton saveButton = StandardWidgetsFactory.createTextButton("Save");
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                HyperLap2DFacade.getInstance().sendNotification(notificationCallback, textArea.getText());
                close();
            }
        });

        getButtonsTable().add(cancelButton).width(65).pad(2).right();
        getButtonsTable().add(saveButton).width(65).pad(2).right();
        getCell(getButtonsTable()).right();

        pack();
    }

    public void setSyntax(Highlighter syntax) {
        if (syntax != null)
            textArea.setHighlighter(syntax);
    }

    public void setText(String text) {
        if (text != null) {
            textArea.setText(text);
        }
    }

    public void setNotificationCallback(String notificationCallback) {
        this.notificationCallback = notificationCallback;
    }

    @Override
    public float getPrefWidth() {
        return Sandbox.getInstance().getUIStage().getWidth() * 0.7f;
    }

    @Override
    public float getPrefHeight() {
        return Sandbox.getInstance().getUIStage().getHeight() * 0.8f;
    }
}
