package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;

/**
 * Created by Sasun Poghosyan on 5/10/2016.
 */
public class DeleteSpineAnimation extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteSpineAnimation";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete Spine Animation";
    }

    @Override
    public void doAction() {
        String spineItemName = notification.getBody();
        if (AssetIOManager.getInstance().deleteAsset(AssetsUtils.TYPE_SPINE_ANIMATION, sandbox.getRootEntity(), spineItemName)) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions(true);

            sendNotification(DONE, spineItemName);

            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }
}


