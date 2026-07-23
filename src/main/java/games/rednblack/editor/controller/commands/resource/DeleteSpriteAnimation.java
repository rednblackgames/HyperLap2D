package games.rednblack.editor.controller.commands.resource;

import com.badlogic.gdx.utils.ObjectSet;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Sasun Poghosyan on 5/12/2016.
 */
public class DeleteSpriteAnimation extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteSpriteAnimation";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete Sprite Animation";
    }

    @Override
    public void doAction() {
        String spriteAnimationName = notification.getBody();
        // find the pack holding this animation before deletion scopes the repack to just that pack
        String packName = projectManager.findPackNameForRegion(spriteAnimationName);
        if (AssetIOManager.getInstance().deleteAsset(AssetsUtils.TYPE_SPRITE_ANIMATION_ATLAS, sandbox.getRootEntity(), spriteAnimationName)) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            ObjectSet<String> forcePacks = new ObjectSet<>();
            forcePacks.add(packName != null ? packName : "main");
            resolutionManager.rePackProjectImagesForAllResolutions(true, forcePacks, null);

            facade.sendNotification(DONE, spriteAnimationName);

            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }
}


