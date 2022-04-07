package games.rednblack.editor.view.ui.dialog;

import com.kotcrab.vis.ui.util.highlight.Highlighter;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class CodeEditorDialogMediator extends Mediator<CodeEditorDialog> {

    private static final String TAG = CodeEditorDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private File observedFile = null;

    public CodeEditorDialogMediator() {
        super(NAME, new CodeEditorDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.OPEN_CODE_EDITOR,
                MsgAPI.PROJECT_FILE_MODIFIED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case MsgAPI.OPEN_CODE_EDITOR:
                Object[] payload = notification.getBody();
                viewComponent.setSyntax((Highlighter) payload[0]);
                viewComponent.setText((String) payload[1]);
                viewComponent.setNotificationCallback((String) payload[2]);
                viewComponent.setNotificationCallbackType((String) payload[4]);
                viewComponent.show(uiStage);

                observedFile = (File) payload[3];
                if (observedFile != null) {
                    readObservedFile();
                }
                break;
            case MsgAPI.PROJECT_FILE_MODIFIED:
                if (!viewComponent.hasParent())
                    break;

                File modifiedFile = notification.getBody();
                if (modifiedFile != null && modifiedFile.equals(observedFile)) {
                    readObservedFile();
                }
                break;
        }
    }

    private void readObservedFile() {
        try {
            String fileContent = Files.readString(observedFile.toPath());
            viewComponent.setText(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object[] openCodeEditorPayload(Highlighter syntax, String text, String notificationCallback, File file, String type) {
        Object[] payload = new Object[5];
        payload[0] = syntax;
        payload[1] = text;
        payload[2] = notificationCallback;
        payload[3] = file;
        payload[4] = type;
        return payload;
    }
}
