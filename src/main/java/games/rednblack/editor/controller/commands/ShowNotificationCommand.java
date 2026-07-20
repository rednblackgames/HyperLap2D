package games.rednblack.editor.controller.commands;

import games.rednblack.h2d.common.HyperLog;
import games.rednblack.puremvc.commands.SimpleCommand;
import games.rednblack.puremvc.interfaces.INotification;

/**
 * Logs a user-facing notification text. The toast UI is shown by {@link games.rednblack.editor.view.stage.UIStageMediator},
 * which subscribes to {@link MsgAPI#SHOW_NOTIFICATION} and owns the {@code UIStage} toast manager —
 * this keeps the command off the view layer.
 */
public class ShowNotificationCommand extends SimpleCommand {
    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.ShowNotificationCommand";
    public static final String TYPE_CLEAR_STACK = CLASS_NAME + ".CLEAR_STACK";

    @Override
    public void execute(INotification notification) {
        HyperLog.cInfo(notification.getBody());
    }
}