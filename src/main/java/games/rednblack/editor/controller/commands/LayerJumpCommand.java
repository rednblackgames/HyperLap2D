package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.components.LayerMapComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;

public class LayerJumpCommand extends EntityModifyRevertibleCommand {
    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.LayerJumpCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    private Integer entityId;

    private String sourceName;
    private String targetName;

    private void backup() {
        if(entityId == null) {
            Object[] payload = getNotification().getBody();
            sourceName = (String) payload[0];
            targetName = (String) payload[1];
            entityId = EntityUtils.getEntityId(Sandbox.getInstance().getCurrentViewingEntity());
        }
    }

    @Override
    public void doAction() {
        backup();

        int viewingEntity = EntityUtils.getByUniqueId(entityId);
        LayerMapComponent layerMapComponent = SandboxComponentRetriever.get(viewingEntity, LayerMapComponent.class);
        targetName = layerMapComponent.jump(sourceName, targetName);

        facade.sendNotification(DONE);

    }

    @Override
    public void undoAction() {
        int viewingEntity = EntityUtils.getByUniqueId(entityId);
        LayerMapComponent layerMapComponent = SandboxComponentRetriever.get(viewingEntity, LayerMapComponent.class);
        layerMapComponent.jump(sourceName, targetName);

        facade.sendNotification(DONE);
    }
}
