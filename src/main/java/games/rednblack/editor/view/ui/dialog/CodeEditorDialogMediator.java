package games.rednblack.editor.view.ui.dialog;

import com.kotcrab.vis.ui.util.highlight.Highlighter;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;

public class CodeEditorDialogMediator extends SimpleMediator<CodeEditorDialog> {

    private static final String TAG = CodeEditorDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

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
                MsgAPI.OPEN_CODE_EDITOR
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case MsgAPI.OPEN_CODE_EDITOR:
                Object[] payload = notification.getBody();
                viewComponent.setSyntax((Highlighter) payload[0]);
                viewComponent.setText((String) payload[1]);
                viewComponent.setNotificationCallback((String) payload[2]);
                viewComponent.show(uiStage);
                break;
        }
    }

    public static Object[] openCodeEditorPayload(Highlighter syntax, String text, String notificationCallback) {
        Object[] payload = new Object[3];
        payload[0] = syntax;
        payload[1] = text;
        payload[2] = notificationCallback;
        return payload;
    }
}
