package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.controller.commands.component.UpdateParticleDataCommand;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;
import games.rednblack.editor.renderer.data.ParticleEffectVO;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

public class UIParticlePropertiesMediator extends UIItemPropertiesMediator<Entity, UIParticleProperties> {

    private static final String TAG = UIParticlePropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIParticlePropertiesMediator() {
        super(NAME, new UIParticleProperties());
    }

    @Override
    protected void translateObservableDataToView(Entity item) {
        viewComponent.setMatrixTransformEnabled(item.getComponent(ParticleComponent.class).transform);
    }

    @Override
    protected void translateViewToItemData() {
        ParticleEffectVO payloadVo = new ParticleEffectVO();
        payloadVo.transform = viewComponent.isMatrixTransformEnabled();

        Object payload = UpdateParticleDataCommand.payload(observableReference, payloadVo);
        facade.sendNotification(MsgAPI.ACTION_UPDATE_PARTICLE_DATA, payload);
    }
}
