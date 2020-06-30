package games.rednblack.editor.controller.commands;

import com.kotcrab.vis.ui.widget.toast.MessageToast;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.controller.SandboxCommand;

public class ShowNotificationCommand extends SandboxCommand {

    @Override
    public void execute(Notification notification) {
        String text = notification.getBody();
        final MessageToast messageToast = new MessageToast(text);
        messageToast.pad(10);
        sandbox.getToastManager().show(messageToast, 5);
    }
}
