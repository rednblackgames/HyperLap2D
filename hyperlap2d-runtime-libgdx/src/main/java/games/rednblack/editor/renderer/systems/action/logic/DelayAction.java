package games.rednblack.editor.renderer.systems.action.logic;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.systems.action.Actions;
import games.rednblack.editor.renderer.systems.action.data.DelayData;

/**
 * Created by ZeppLondon on 10/15/2015.
 */
public class DelayAction<T extends DelayData> extends DelegateAction<T> {

    @Override
    protected boolean delegate(float delta, Entity entity, T actionData) {
        if (actionData.passedTime < actionData.duration) {
            actionData.passedTime += delta;
            if (actionData.passedTime < actionData.duration) return false;
        }

        if (actionData.delegatedData != null) {
            ActionLogic logic = Actions.actionLogicMap.get(actionData.delegatedData.logicClassName);

            return logic.act(delta, entity, actionData.delegatedData);
        }
        return true;
    }
}
