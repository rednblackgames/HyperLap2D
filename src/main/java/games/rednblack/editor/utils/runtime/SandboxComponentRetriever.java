package games.rednblack.editor.utils.runtime;

import games.rednblack.editor.renderer.ecs.BaseComponentMapper;
import games.rednblack.editor.renderer.ecs.Component;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;

public class SandboxComponentRetriever {

    public static <T extends Component> T get(int entity, Class<T> type) {
        return ComponentRetriever.get(entity, type, Sandbox.getInstance().getEngine());
    }

    public static <T extends Component> BaseComponentMapper<T> getMapper(Class<T> type) {
        return ComponentRetriever.getMapper(type, Sandbox.getInstance().getEngine());
    }
}
