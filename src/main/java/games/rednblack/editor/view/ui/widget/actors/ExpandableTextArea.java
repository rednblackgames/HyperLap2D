package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.highlight.Highlighter;
import com.kotcrab.vis.ui.widget.VisImageButton;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextArea;
import games.rednblack.editor.view.ui.dialog.CodeEditorDialogMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.puremvc.java.interfaces.IFacade;

public class ExpandableTextArea extends VisTable {
    public interface OnExpandListener {
        void onExpand(VisTextArea textArea);
    }

    private IFacade facade;

    private OnExpandListener listener;
    private VisTextArea textArea;
    private VisImageButton expandButton;

    private Highlighter syntax;

    public ExpandableTextArea(IFacade facade, String notificationCallback) {
        this.facade = facade;

        textArea = StandardWidgetsFactory.createTextArea();
        add(textArea).growX().fillX().height(65);
        row();
        expandButton = StandardWidgetsFactory.createImageButton("expand-button");
        expandButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(MsgAPI.OPEN_CODE_EDITOR, CodeEditorDialogMediator.openCodeEditorPayload(syntax, textArea.getText(), notificationCallback, null, ""));
                if (listener != null)
                    listener.onExpand(textArea);
            }
        });
        add(expandButton).right().pad(5);
    }

    public void setExpandListener(OnExpandListener listener) {
        this.listener = listener;
    }

    public void setSyntax(Highlighter syntax) {
        this.syntax = syntax;
    }

    public VisTextArea getTextArea() {
        return textArea;
    }
}
