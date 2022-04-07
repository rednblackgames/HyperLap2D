package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.controller.commands.NonRevertibleCommand;

public abstract class DeleteResourceCommand extends NonRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteResourceCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmAction() {
        return "Delete";
    }

    @Override
    protected String confirmDialogMessage() {
        String resourceName = notification.getBody();
        return "Are you sure you want to delete '" + resourceName +"'?\nAction cannot be undone.";
    }

    @Override
    protected void callDoAction() {
        super.callDoAction();

        if (!isCancelled) {
            facade.sendNotification(DONE, notification.getBody());
        }
    }
}
