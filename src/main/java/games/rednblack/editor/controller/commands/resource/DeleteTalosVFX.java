package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.view.stage.Sandbox;

public class DeleteTalosVFX extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteTalosVFX";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete Talos VFX";
    }

    @Override
    public void doAction() {
        String particleName = notification.getBody();
        if (AssetIOManager.getInstance().deleteAsset(AssetsUtils.TYPE_TALOS_VFX, sandbox.getRootEntity(), particleName)) {
            projectManager.loadProjectData(projectManager.getCurrentProjectPath());
            sendNotification(DONE, particleName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
            Sandbox.getInstance().loadCurrentProject();
        } else {
            cancel();
        }
    }
}
