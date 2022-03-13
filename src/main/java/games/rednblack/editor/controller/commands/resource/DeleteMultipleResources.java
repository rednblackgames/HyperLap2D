package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.view.ui.BoxItemResourceSelectionUIMediator;

public class DeleteMultipleResources extends DeleteResourceCommand {
    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteMultipleResources";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogMessage() {
        BoxItemResourceSelectionUIMediator boxSelection = facade.retrieveMediator(BoxItemResourceSelectionUIMediator.NAME);
        return "Are you sure you want to delete " + boxSelection.boxResourceSelectedSet.size() + " resources?";
    }

    @Override
    public void doAction() {
        BoxItemResourceSelectionUIMediator boxSelection = facade.retrieveMediator(BoxItemResourceSelectionUIMediator.NAME);
        for (String resource : boxSelection.boxResourceSelectedSet) {
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
