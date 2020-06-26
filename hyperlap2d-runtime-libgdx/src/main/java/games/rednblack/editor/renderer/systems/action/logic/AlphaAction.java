package games.rednblack.editor.renderer.systems.action.logic;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.systems.action.data.AlphaData;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

/**
 * Created by ZeppLondon on 10/29/15.
 */
public class AlphaAction<T extends AlphaData> extends TemporalAction<T> {
    @Override
    protected void update(float percent, Entity entity, T actionData) {
        TintComponent tintComponent = ComponentRetriever.get(entity, TintComponent.class);
        tintComponent.color.a = actionData.start + (actionData.end - actionData.start) * percent;
    }

    @Override
    public void begin(Entity entity, T actionData) {
        TintComponent tintComponent = ComponentRetriever.get(entity, TintComponent.class);
        actionData.start = tintComponent.color.a;
    }
}
