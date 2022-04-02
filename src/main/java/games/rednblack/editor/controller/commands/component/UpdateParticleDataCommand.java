package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.particle.ParticleComponent;
import games.rednblack.editor.renderer.data.ParticleEffectVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateParticleDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private ParticleEffectVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        ParticleEffectVO vo = (ParticleEffectVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        backup = new ParticleEffectVO();
        backup.loadFromEntity(entity, sandbox.getEngine(), sandbox.sceneControl.sceneLoader.getEntityFactory());

        ParticleComponent particleComponent = SandboxComponentRetriever.get(entity, ParticleComponent.class);
        particleComponent.transform = vo.transform;
        particleComponent.autoStart = vo.autoStart;

        particleComponent.particleEffect.setPosition(0, 0);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        ParticleComponent particleComponent = SandboxComponentRetriever.get(entity, ParticleComponent.class);
        particleComponent.transform = backup.transform;
        particleComponent.autoStart = backup.autoStart;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, ParticleEffectVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
