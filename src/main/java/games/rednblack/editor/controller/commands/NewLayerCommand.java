package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.components.LayerMapComponent;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;

/**
 * Created by CyberJoe on 7/25/2015.
 */
public class NewLayerCommand extends EntityModifyRevertibleCommand {
    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.NewLayerCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    private String layerName;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int index = (int) payload[0];
        layerName = (String) payload[1];

        int viewingEntity = Sandbox.getInstance().getCurrentViewingEntity();
        LayerMapComponent layerMapComponent = SandboxComponentRetriever.get(viewingEntity, LayerMapComponent.class);

        LayerItemVO vo = new LayerItemVO(layerName);
        vo.isVisible = true;
        layerMapComponent.addLayer(index, vo);

        facade.sendNotification(DONE, layerName);
    }

    @Override
    public void undoAction() {
        int viewingEntity = Sandbox.getInstance().getCurrentViewingEntity();
        LayerMapComponent layerMapComponent = SandboxComponentRetriever.get(viewingEntity, LayerMapComponent.class);

        layerMapComponent.deleteLayer(layerName);

        facade.sendNotification(DONE, layerName);
    }

    public static Object[] payload(int index, String name) {
        Object[] payload = new Object[2];
        payload[0] = index;
        payload[1] = name;

        return payload;
    }
}
