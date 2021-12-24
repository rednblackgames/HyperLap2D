package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.components.LayerMapComponent;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;

/**
 * Created by CyberJoe on 7/25/2015.
 * This command marked as "atom" meaning it cannot be called outside the transaction
 * TODO: make this an annotation
 */
public class DeleteLayerAtomCommand extends EntityModifyRevertibleCommand {

    private String layerName;

    private LayerItemVO layerItemVO;
    private int layerIndex;

    public DeleteLayerAtomCommand(String layerName) {
        this.layerName = layerName;
    }

    @Override
    public void doAction() {
        int viewingEntity = Sandbox.getInstance().getCurrentViewingEntity();
        LayerMapComponent layerMapComponent = SandboxComponentRetriever.get(viewingEntity, LayerMapComponent.class);

        if(layerMapComponent.getLayers().size > 1) {
            layerMapComponent.deleteLayer(layerName);
        } else {
            cancel();
        }
    }

    @Override
    public void undoAction() {
        int viewingEntity = Sandbox.getInstance().getCurrentViewingEntity();
        LayerMapComponent layerMapComponent = SandboxComponentRetriever.get(viewingEntity, LayerMapComponent.class);

        layerMapComponent.addLayer(layerIndex, layerItemVO);
    }

    public int getLayerIndex() {
        return layerIndex;
    }
}
