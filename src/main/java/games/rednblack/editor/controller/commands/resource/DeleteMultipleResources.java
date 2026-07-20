package games.rednblack.editor.controller.commands.resource;
import java.util.SortedSet;

import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.proxy.ResourceSelectionProxy;

public class DeleteMultipleResources extends DeleteResourceCommand {
    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteMultipleResources";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogMessage() {
        SortedSet<String> selectedResources = ResourceSelectionProxy.get(facade).getSelectedResources();
        return "Are you sure you want to delete " + selectedResources.size() + " resources?";
    }

    @Override
    public void doAction() {
        SortedSet<String> selectedResources = ResourceSelectionProxy.get(facade).getSelectedResources();
        for (String resource : selectedResources) {
            if (!AssetIOManager.getInstance().deleteAsset(sandbox.getRootEntity(), resource))
                cancel();
        }

        if (!isCancelled) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions(true);

            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);

            sendNotification(DONE);
        }
    }
}
