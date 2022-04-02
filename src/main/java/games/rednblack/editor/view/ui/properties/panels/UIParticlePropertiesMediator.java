package games.rednblack.editor.view.ui.properties.panels;

import games.rednblack.editor.controller.commands.component.UpdateParticleDataCommand;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;
import games.rednblack.editor.renderer.data.ParticleEffectVO;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.properties.UIItemPropertiesMediator;
import games.rednblack.h2d.common.MsgAPI;

public class UIParticlePropertiesMediator extends UIItemPropertiesMediator<UIParticleProperties> {

    private static final String TAG = UIParticlePropertiesMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIParticlePropertiesMediator() {
        super(NAME, new UIParticleProperties());
    }

    @Override
    protected void translateObservableDataToView(int item) {
        ParticleComponent particleComponent = SandboxComponentRetriever.get(item, ParticleComponent.class);
        viewComponent.setMatrixTransformEnabled(particleComponent.transform);
        viewComponent.setAutoStartEnabled(particleComponent.autoStart);
    }

    @Override
    protected void translateViewToItemData() {
        ParticleEffectVO payloadVo = new ParticleEffectVO();
        payloadVo.transform = viewComponent.isMatrixTransformEnabled();
        payloadVo.autoStart = viewComponent.isAutoStartEnabled();

        Object payload = UpdateParticleDataCommand.payload(observableReference, payloadVo);
        facade.sendNotification(MsgAPI.ACTION_UPDATE_PARTICLE_DATA, payload);
    }
}
