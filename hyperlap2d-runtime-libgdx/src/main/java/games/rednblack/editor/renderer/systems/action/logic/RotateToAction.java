package games.rednblack.editor.renderer.systems.action.logic;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.systems.action.data.RotateToData;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

/**
 * Created by ZeppLondon on 10/16/2015.
 */
public class RotateToAction<T extends RotateToData> extends TemporalAction<T> {
    @Override
    protected void update(float percent, Entity entity, T actionData) {
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
        transformComponent.rotation = (actionData.start + (actionData.end - actionData.start) * percent);
    }

    @Override
    public void begin(Entity entity, T actionData) {
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
        actionData.start = transformComponent.rotation;
    }
}
