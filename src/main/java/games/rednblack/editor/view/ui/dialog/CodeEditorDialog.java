package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.highlight.Highlighter;
import com.kotcrab.vis.ui.widget.H2DHighlightTextArea;
import com.kotcrab.vis.ui.widget.HighlightTextArea;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.code.syntax.ProgrammingSyntax;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;

public class CodeEditorDialog extends H2DDialog {

    private final HighlightTextArea textArea;
    private String notificationCallback, notificationCallbackType;

    private boolean smartIndent = false;

    public CodeEditorDialog() {
        super("Code Editor");

        addCloseButton();
        setResizable(true);

        textArea = new H2DHighlightTextArea("", "code-editor") {
            @Override
            protected boolean onKeyTyped(InputEvent event, char character) {
                if (character == '\t') {
                    insertText("    ");
                    return true;
                }

                if (smartIndent && character == '\n' && getLinesBreak().size > (getCursorLine() + 1) * 2 - 1) {
                    int start = getLinesBreak().get((getCursorLine()) * 2);
                    int end = getLinesBreak().get((getCursorLine() + 1) * 2 - 1);
                    if (start >= end)
                        return false;

                    String prevLines = text.substring(start, end);

                    insertText("\n");

                    for(int i = 0; i < prevLines.length(); i++) {
                        if(Character.isWhitespace(prevLines.charAt(i)))
                            insertText(" ");
                        else
                            break;
                    }

                    return true;
                }

                return false;
            }
        };
        ScrollPane scrollPane = textArea.createCompatibleScrollPane();
        scrollPane.addListener(new ScrollFocusListener());
        getContentTable().add(scrollPane).grow().row();

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
                HyperLap2DFacade.getInstance().sendNotification(notificationCallback, textArea.getText(), notificationCallbackType);
                close();
            }
        });

        getButtonsTable().add(cancelButton).width(65).pad(2).right();
        getButtonsTable().add(saveButton).width(65).pad(2).right();
        getCell(getButtonsTable()).right();

        pack();
    }

    public void setSyntax(Highlighter syntax) {
        if (syntax != null){
            textArea.setHighlighter(syntax);
            smartIndent = syntax instanceof ProgrammingSyntax;
        }
    }

    public void setText(String text) {
        if (text != null) {
            text = text.replace("\t", "    ");
            textArea.setText(text);
        }
    }

    public void setNotificationCallback(String notificationCallback) {
        this.notificationCallback = notificationCallback;
    }

    public void setNotificationCallbackType(String notificationCallbackType) {
        this.notificationCallbackType = notificationCallbackType;
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
