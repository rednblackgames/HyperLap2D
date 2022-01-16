package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;

/**
 * Created by CyberJoe on 11/6/2015.
 */
public class CustomVariableModifyCommand extends EntityModifyRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.CustomVariableModifyCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    private Integer entityId;

    private boolean isAdding;
    private String key;
    private String value;

    @Override
    public void doAction() {
        process();
        if(isAdding) {
            addVariable(key, value);
        } else {
            removeVariable(key);
        }
        sendNotification(DONE);
    }

    @Override
    public void undoAction() {
        if(isAdding) {
            removeVariable(key);
        } else {
            addVariable(key, value);
        }
        sendNotification(DONE);
    }

    private void addVariable(String key, String value) {
        int entity = EntityUtils.getByUniqueId(entityId);
        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
        mainItemComponent.setCustomVars(key, value);
    }

    private void removeVariable(String key) {
        int entity = EntityUtils.getByUniqueId(entityId);
        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(entity, MainItemComponent.class);
        value = mainItemComponent.customVariables.get(key); //storing the backup
        mainItemComponent.removeCustomVars(key);
    }

    private void process() {
        if(entityId == null) {
            // First time call, need to prepare the data and fetch payload
            Object[] payload = getNotification().getBody();
            int item = (int) payload[0];
            entityId = EntityUtils.getEntityId(item);
            key = (String) payload[2];
            isAdding = false;
            if(((boolean) payload[1])) {
                value = (String) payload[3];
                isAdding = true;
            }
        }
    }

    public static Object addCustomVariable(int entity, String key, String value) {
        Object[] payload = new Object[4];
        payload[0] = entity;
        payload[1] = true; // is adding type
        payload[2] = key;
        payload[3] = value;

        return payload;
    }

    public static Object removeCustomVariable(int entity, String key) {
        Object[] payload = new Object[3];
        payload[0] = entity;
        payload[1] = true;
        payload[1] = false; // is adding type (removing)
        payload[2] = key;

        return payload;
    }
}
