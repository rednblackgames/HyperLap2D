package games.rednblack.editor.renderer.systems.action;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.renderer.components.ActionComponent;
import games.rednblack.editor.renderer.systems.action.data.ActionData;
import games.rednblack.editor.renderer.systems.action.logic.ActionLogic;

/**
 * Created by ZeppLondon on 10/13/2015.
 */
public class ActionSystem extends IteratingSystem {
    private final ComponentMapper<ActionComponent> actionMapper;

    public ActionSystem() {
        super(Family.all(ActionComponent.class).get());
        actionMapper = ComponentMapper.getFor(ActionComponent.class);
    }

    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        ActionComponent actionComponent = actionMapper.get(entity);
        Array<ActionData> dataArray = actionComponent.dataArray;
        for (int i = 0; i < dataArray.size; i++) {
            ActionData data = dataArray.get(i);
            ActionLogic actionLogic = Actions.actionLogicMap.get(data.logicClassName);
            if (actionLogic.act(deltaTime, entity, data)) {
                dataArray.removeValue(data, true);
                if (data.getPool() != null)
                    data.getPool().free(data);
            }
        }

        if (dataArray.size == 0) {
            entity.remove(ActionComponent.class);
        }
    }
}
