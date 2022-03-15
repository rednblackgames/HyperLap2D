package games.rednblack.editor.controller.commands;

import com.artemis.Component;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.utils.runtime.ComponentCloner;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Created by CyberJoe on 7/2/2015.
 */
public class RemoveComponentFromItemCommand extends EntityModifyRevertibleCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.RemoveComponentFromItemCommand";
    public static final String DONE = CLASS_NAME + "DONE";

    private int entity;
    private Component component;
    private Class<? extends Component> componentClass;

    private void collectData() {
        Object[] payload = getNotification().getBody();
        entity = (int) payload[0];
        componentClass = (Class<? extends Component>) payload[1];
        component = ComponentCloner.get(SandboxComponentRetriever.get(entity, componentClass), true);
    }

    @Override
    public void doAction() {
        collectData();
        sandbox.getEngine().edit(entity).remove(component.getClass());
        sandbox.getEngine().process();

        HyperLap2DFacade.getInstance().sendNotification(DONE, entity);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);

        Sandbox.getInstance().getSceneControl().sceneLoader.getRenderer().removeSpecialEntity(entity);
    }

    @Override
    public void undoAction() {
        if (SandboxComponentRetriever.get(entity, component.getClass()) == null) {
            Component newComponent = sandbox.getEngine().edit(entity).create(componentClass);
            ComponentCloner.set(newComponent, component);
        }

        HyperLap2DFacade.getInstance().sendNotification(DONE, entity);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object[] payload(int entity, Class<? extends Component> componentClass) {
        Object[] payload = new Object[2];
        payload[0] = entity;
        payload[1] = componentClass;
        return payload;
    }
}
