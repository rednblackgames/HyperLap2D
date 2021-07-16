package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetImporter;
import games.rednblack.editor.utils.ImportUtils;

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
        if (AssetImporter.getInstance().deleteAsset(ImportUtils.TYPE_PARTICLE_EFFECT, sandbox.getRootEntity(), particleName)) {
            projectManager.loadProjectData(projectManager.getCurrentProjectPath());
            sendNotification(DONE, particleName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }
}
