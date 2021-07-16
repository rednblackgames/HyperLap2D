package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetImporter;
import games.rednblack.editor.utils.ImportUtils;

/**
 * Created by azakhary on 11/29/2015.
 */
public class DeleteImageResource extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteImageResource";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete Image Resource";
    }

    @Override
    public void doAction() {
        String imageName = notification.getBody();
        if (AssetImporter.getInstance().deleteAsset(ImportUtils.TYPE_IMAGE, sandbox.getRootEntity(), imageName)) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions(true);
            sendNotification(DONE, imageName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }
}
