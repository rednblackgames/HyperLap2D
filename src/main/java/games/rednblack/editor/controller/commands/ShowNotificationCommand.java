package games.rednblack.editor.controller.commands;

import com.kotcrab.vis.ui.widget.toast.MessageToast;
import games.rednblack.editor.controller.SandboxCommand;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;

public class ShowNotificationCommand extends SandboxCommand {

    @Override
    public void execute(INotification notification) {
        String text = notification.getBody();
        final MessageToast messageToast = new MessageToast(text);
        messageToast.pad(10);
        sandbox.getToastManager().show(messageToast, 5);

        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "[246BCE]");
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, text);
        facade.sendNotification(MsgAPI.WRITE_TO_CONSOLE, "[ffffff]\n");
    }
}
