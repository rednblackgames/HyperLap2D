package games.rednblack.editor.controller.commands.resource;

import com.badlogic.gdx.utils.ObjectSet;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;

import java.util.Collections;
import java.util.Set;

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
        // find the pack holding this image before deletion scopes the repack to just that pack
        String packName = projectManager.findPackNameForRegion(imageName);
        if (AssetIOManager.getInstance().deleteAsset(AssetsUtils.TYPE_IMAGE, sandbox.getRootEntity(), imageName)) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);

            ObjectSet<String> forcePacks = new ObjectSet<>();
            forcePacks.add(packName != null ? packName : "main");
            resolutionManager.rePackProjectImagesForAllResolutions(true, forcePacks, null);
            sendNotification(DONE, imageName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }
}
