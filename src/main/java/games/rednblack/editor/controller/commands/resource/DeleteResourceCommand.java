package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.controller.commands.NonRevertibleCommand;

public abstract class DeleteResourceCommand extends NonRevertibleCommand {

    @Override
    protected String confirmAction() {
        return "Delete";
    }

    @Override
    protected String confirmDialogMessage() {
        String resourceName = notification.getBody();
        return "Are you sure you want to delete '" + resourceName +"'?";
    }
}
