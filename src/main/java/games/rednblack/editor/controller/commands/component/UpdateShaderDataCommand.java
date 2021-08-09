package games.rednblack.editor.controller.commands.component;

import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateShaderDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private String backupName;
    private MainItemVO.RenderingLayer backupLayer;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        String shaderName = (String) payload[1];
        MainItemVO.RenderingLayer renderingLayer = (MainItemVO.RenderingLayer) payload[2];
        entityId = EntityUtils.getEntityId(entity);

        ShaderComponent shaderComponent = SandboxComponentRetriever.get(entity, ShaderComponent.class);
        if (shaderComponent == null) {
            cancel();
            return;
        }

        backupName = shaderComponent.shaderName;
        backupLayer = shaderComponent.renderingLayer;

        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        if(shaderName.equals("Default")) {
            shaderComponent.clear();
        } else {
            shaderComponent.setShader(shaderName, resourceManager.getShaderProgram(shaderName));
        }
        shaderComponent.renderingLayer = renderingLayer;

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);
        ShaderComponent shaderComponent = SandboxComponentRetriever.get(entity, ShaderComponent.class);
        if (shaderComponent == null) return;

        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        if(backupName.equals("Default")) {
            shaderComponent.clear();
        } else {
            shaderComponent.setShader(backupName, resourceManager.getShaderProgram(backupName));
        }

        shaderComponent.renderingLayer = backupLayer;

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, String shaderName, MainItemVO.RenderingLayer layer) {
        Object[] payload = new Object[3];
        payload[0] = entity;
        payload[1] = shaderName;
        payload[2] = layer;

        return payload;
    }
}
