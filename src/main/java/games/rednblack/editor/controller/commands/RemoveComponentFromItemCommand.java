package games.rednblack.editor.controller.commands;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.components.RemovableComponent;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.HyperLap2DFacade;

/**
 * Created by CyberJoe on 7/2/2015.
 */
public class RemoveComponentFromItemCommand extends EntityModifyRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    private Entity entity;
    private Component component;

    private void collectData() {
        Object[] payload = getNotification().getBody();
        entity = (Entity) payload[0];
        Class<? extends Component> componentClass = (Class<? extends Component>) payload[1];
        component = entity.getComponent(componentClass);
    }

    @Override
    public void doAction() {
        collectData();
        if (component instanceof RemovableComponent) {
            ((RemovableComponent) component).onRemove();
        }
        entity.remove(component.getClass());

        HyperLap2DFacade.getInstance().sendNotification(DONE, entity);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        if (entity.getComponent(component.getClass()) == null) {
            entity.add(component);
        }

        HyperLap2DFacade.getInstance().sendNotification(DONE, entity);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object[] payload(Entity entity, Class<? extends Component> componentClass) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = componentClass;
        return payload;
    }
}
