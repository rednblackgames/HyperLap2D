package games.rednblack.editor.controller.commands;

import com.kotcrab.vis.ui.widget.toast.MessageToast;
import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.h2d.common.HyperLog;
import org.puremvc.java.interfaces.INotification;

public class ShowNotificationCommand extends SandboxCommand {
    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.ShowNotificationCommand";
    public static final String TYPE_CLEAR_STACK = CLASS_NAME + ".CLEAR_STACK";

    @Override
    public void execute(INotification notification) {
        if (TYPE_CLEAR_STACK.equals(notification.getType())) {
            sandbox.getToastManager().clear();
        }

        String text = notification.getBody();
        final MessageToast messageToast = new MessageToast(text);
        messageToast.pad(10);
        sandbox.getToastManager().show(messageToast, 5);

        HyperLog.cInfo(text);
    }
}
