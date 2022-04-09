package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;

public class DeleteTinyVGResource extends DeleteResourceCommand {
    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteTinyVGResource";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete TinyVG Resource";
    }

    @Override
    public void doAction() {
        String imageName = notification.getBody();
        if (AssetIOManager.getInstance().deleteAsset(AssetsUtils.TYPE_TINY_VG, sandbox.getRootEntity(), imageName)) {
            projectManager.loadProjectData(projectManager.getCurrentProjectPath());
            sendNotification(DONE, imageName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }
}
