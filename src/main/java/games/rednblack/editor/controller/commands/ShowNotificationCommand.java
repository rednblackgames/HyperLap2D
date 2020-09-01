package games.rednblack.editor.controller.commands;

import com.kotcrab.vis.ui.widget.toast.MessageToast;
import games.rednblack.editor.controller.SandboxCommand;
import org.puremvc.java.interfaces.INotification;

public class ShowNotificationCommand extends SandboxCommand {

    @Override
    public void execute(INotification notification) {
        String text = notification.getBody();
        final MessageToast messageToast = new MessageToast(text);
        messageToast.pad(10);
        sandbox.getToastManager().show(messageToast, 5);
    }
}
