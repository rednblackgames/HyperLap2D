package games.rednblack.editor.view.ui.box.resourcespanel.draggable.list;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.box.UIResourcesBoxMediator;
import games.rednblack.h2d.common.ResourcePayloadObject;

public class TalosResource extends ListItemResource {
    private final HyperLap2DFacade facade;
    private final Image payloadImg;
    private final ResourcePayloadObject payload;


    public TalosResource(String particleName) {
        super(particleName, "particle");
        facade = HyperLap2DFacade.getInstance();
        payloadImg = new Image(getStyle().resourceOver) {
            @Override
            public void setScale(float scaleXY) {
                //Do not scale
            }
        };
        payloadImg.setScale(2);
        payloadImg.getColor().a = .85f;
        payload = new ResourcePayloadObject();
        payload.name = particleName;
        setRightClickEvent(UIResourcesBoxMediator.TALOS_VFX_RIGHT_CLICK, payload.name);
        payload.className = getClass().getName();
    }

    @Override
    public Actor getDragActor() {
        return payloadImg;
    }

    @Override
    public ResourcePayloadObject getPayloadData() {
        return payload;
    }
}
