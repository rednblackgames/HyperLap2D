package games.rednblack.editor.controller.commands.component;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.components.ShaderComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;

public class UpdateShaderDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private String backup;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        String shaderName = (String) payload[1];
        entityId = EntityUtils.getEntityId(entity);

        ShaderComponent shaderComponent = ComponentRetriever.get(entity, ShaderComponent.class);

        backup = shaderComponent.shaderName;

        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        if(shaderName.equals("Default")) {
            shaderComponent.clear();
        } else {
            shaderComponent.setShader(shaderName, resourceManager.getShaderProgram(shaderName));
        }

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);
        ShaderComponent shaderComponent = ComponentRetriever.get(entity, ShaderComponent.class);

        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        if(backup.equals("Default")) {
            shaderComponent.clear();
        } else {
            shaderComponent.setShader(backup, resourceManager.getShaderProgram(backup));
        }

        facade.sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(Entity entity, String shaderName) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = shaderName;

        return payload;
    }
}
