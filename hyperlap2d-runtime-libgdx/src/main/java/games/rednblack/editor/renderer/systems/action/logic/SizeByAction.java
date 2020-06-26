package games.rednblack.editor.renderer.systems.action.logic;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.systems.action.data.SizeByData;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

/**
 * Created by ZeppLondon on 10/28/15.
 */
public class SizeByAction<T extends SizeByData> extends RelativeTemporalAction<T> {
    @Override
    protected void updateRelative(float percentDelta, Entity entity, T actionData) {
        DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
        dimensionsComponent.width += actionData.amountWidth * percentDelta;
        dimensionsComponent.height += actionData.amountHeight * percentDelta;
    }
}
