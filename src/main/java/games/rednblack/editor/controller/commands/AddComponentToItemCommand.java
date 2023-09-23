package games.rednblack.editor.controller.commands;

import com.artemis.Component;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

/**
 * Created by CyberJoe on 7/2/2015.
 */
public class AddComponentToItemCommand extends EntityModifyRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.AddComponentToItemCommand";
    public static final String DONE = CLASS_NAME + ".DONE";

    private String entityId;
    private Class<? extends Component> component;

    private void collectData() {
        Object[] payload = getNotification().getBody();
        entityId = (String) payload[0];
        component = (Class<? extends Component>) payload[1];
    }

    @Override
    public void doAction() {
        collectData();

        int entity = EntityUtils.getByUniqueId(entityId);
        Component newComponent = Sandbox.getInstance().getEngine().edit(entity).create(component);
        sandbox.getEngine().inject(newComponent);

        Facade.getInstance().sendNotification(DONE, entity);
        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        Sandbox.getInstance().getEngine().edit(entity).remove(component);
        Sandbox.getInstance().getEngine().process();

        Facade.getInstance().sendNotification(DONE, entity);
        Facade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object[] payload(int entity, Class<? extends Component> component) {
        Object[] payload = new Object[2];
        payload[0] = EntityUtils.getEntityId(entity);
        payload[1] = component;
        return payload;
    }
}
