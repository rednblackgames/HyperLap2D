package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;

/**
 * Created by Sasun Poghosyan on 5/10/2016.
 */
public class DeleteParticleEffect extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteParticleEffect";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete Particle Effect";
    }

    @Override
    public void doAction() {
        String particleName = notification.getBody();
        if (AssetIOManager.getInstance().deleteAsset(AssetsUtils.TYPE_PARTICLE_EFFECT, sandbox.getRootEntity(), particleName)) {
            projectManager.loadProjectData(projectManager.getCurrentProjectPath());
            sendNotification(DONE, particleName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }
}
