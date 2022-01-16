package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.CompositeTransformComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateCompositeDataCommand extends EntityModifyRevertibleCommand {
    private Integer entityId;
    private CompositeItemVO backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        CompositeItemVO vo = (CompositeItemVO) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        backup = new CompositeItemVO();
        backup.loadFromEntity(entity, sandbox.getEngine(), sandbox.sceneControl.sceneLoader.getEntityFactory());

        CompositeTransformComponent transformComponent = SandboxComponentRetriever.get(entity, CompositeTransformComponent.class);
        transformComponent.automaticResize = vo.automaticResize;
        transformComponent.scissorsEnabled = vo.scissorsEnabled;
        transformComponent.renderToFBO = vo.renderToFBO;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);

        if (!transformComponent.renderToFBO) {
            String tag = SandboxComponentRetriever.get(entity, MainItemComponent.class).itemIdentifier;
            sandbox.getSceneControl().sceneLoader.getFrameBufferManager().dispose(tag);
        }
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        CompositeTransformComponent transformComponent = SandboxComponentRetriever.get(entity, CompositeTransformComponent.class);
        transformComponent.automaticResize = backup.automaticResize;
        transformComponent.scissorsEnabled = backup.scissorsEnabled;
        transformComponent.renderToFBO = backup.renderToFBO;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);

        if (!transformComponent.renderToFBO) {
            String tag = SandboxComponentRetriever.get(entity, MainItemComponent.class).itemIdentifier;
            sandbox.getSceneControl().sceneLoader.getFrameBufferManager().dispose(tag);
        }
    }

    public static Object payload(int entity, CompositeItemVO vo) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = vo;

        return payload;
    }
}
