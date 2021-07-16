package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.utils.AssetImporter;
import games.rednblack.editor.utils.ImportUtils;

public class DeleteLibraryAction extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteLibraryAction";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete Library Action";
    }

    @Override
    public void doAction() {
        String libraryActionName = notification.getBody();

        AssetImporter.getInstance().deleteAsset(ImportUtils.TYPE_HYPERLAP2D_ACTION, sandbox.getRootEntity(), libraryActionName);

        facade.sendNotification(DONE, libraryActionName);
    }
}
