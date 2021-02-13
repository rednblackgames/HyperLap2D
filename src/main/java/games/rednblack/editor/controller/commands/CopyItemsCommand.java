package games.rednblack.editor.controller.commands;

import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;

public class CopyItemsCommand extends NonRevertibleCommand {

    public CopyItemsCommand() {
        setShowConfirmDialog(false);
    }

    @Override
    public void doAction() {
        String data = EntityUtils.getJsonStringFromEntities(sandbox.getSelector().getSelectedItems());
        cancel();

        Sandbox.copyToClipboard(data);
    }
}
