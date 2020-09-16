package games.rednblack.editor.renderer.systems.action;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import games.rednblack.editor.renderer.components.ActionComponent;
import games.rednblack.editor.renderer.systems.action.data.*;
import games.rednblack.editor.renderer.systems.action.logic.*;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

import java.util.HashMap;

/**
 * Created by Eduard on 10/13/2015.
 */
public class Actions {

    public static HashMap<String, ActionLogic> actionLogicMap = new HashMap<>();
    private static boolean initialized;

    private static void initialize() throws InstantiationException, IllegalAccessException {
        registerActionClass(MoveToAction.class);
        registerActionClass(MoveByAction.class);
        registerActionClass(SizeToAction.class);
        registerActionClass(SizeByAction.class);
        registerActionClass(ScaleToAction.class);
        registerActionClass(ScaleByAction.class);
        registerActionClass(RotateToAction.class);
        registerActionClass(RotateByAction.class);
        registerActionClass(ColorAction.class);
        registerActionClass(AlphaAction.class);

        registerActionClass(RunnableAction.class);
        registerActionClass(DelayAction.class);

        registerActionClass(ParallelAction.class);
        registerActionClass(SequenceAction.class);
        registerActionClass(RepeatAction.class);

        initialized = true;
    }

    public static <T extends ActionLogic> void registerActionClass(Class<T> type) throws IllegalAccessException, InstantiationException {
        if (!actionLogicMap.containsKey(type.getName())) {
            actionLogicMap.put(type.getName(), type.newInstance());
        }
    }

    static public <T extends ActionData> T actionData(Class<T> type) {
        Pool<T> pool = Pools.get(type);
        T action = pool.obtain();
        action.setPool(pool);
        return action;
    }

    private static void checkInit() {
        if (!initialized) try {
            initialize();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static ActionData moveTo(float x, float y) {
        return moveTo(x, y, 0, null);
    }

    public static ActionData moveTo(float x, float y, float duration) {
        return moveTo(x, y, duration, null);
    }

    public static ActionData moveTo(float x, float y, float duration, Interpolation interpolation) {
        MoveToData actionData = actionData(MoveToData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setEndX(x);
        actionData.setEndY(y);
        actionData.logicClassName = MoveToAction.class.getName();
        return (actionData);
    }

    public static ActionData moveBy(float x, float y) {
        return moveBy(x, y, 0, null);
    }

    public static ActionData moveBy(float x, float y, float duration) {
        return moveBy(x, y, duration, null);
    }

    public static ActionData moveBy(float x, float y, float duration, Interpolation interpolation) {
        MoveByData actionData = actionData(MoveByData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setAmountX(x);
        actionData.setAmountY(y);
        actionData.logicClassName = MoveByAction.class.getName();
        return actionData;
    }

    static public ActionData run(Runnable runnable) {
        RunnableData actionData = actionData(RunnableData.class);
        actionData.setRunnable(runnable);
        actionData.logicClassName = RunnableAction.class.getName();
        return actionData;
    }

    static public RotateToData rotateTo(float end) {
        return rotateTo(end, 0, null);
    }

    static public RotateToData rotateTo(float end, float duration) {
        return rotateTo(end, duration, null);
    }

    static public RotateToData rotateTo(float end, float duration, Interpolation interpolation) {
        RotateToData actionData = actionData(RotateToData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setEnd(end);
        actionData.logicClassName = RotateToAction.class.getName();
        return actionData;
    }

    static public RotateByData rotateBy(float amount) {
        return rotateBy(amount, 0, null);
    }

    static public RotateByData rotateBy(float amount, float duration) {
        return rotateBy(amount, duration, null);
    }

    static public RotateByData rotateBy(float amount, float duration, Interpolation interpolation) {
        RotateByData actionData = actionData(RotateByData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setAmount(amount);
        actionData.logicClassName = RotateByAction.class.getName();
        return actionData;
    }

    public static SizeToData sizeTo(float width, float height) {
        return sizeTo(width, height, 0, null);
    }

    public static SizeToData sizeTo(float width, float height, float duration) {
        return sizeTo(width, height, duration, null);
    }

    public static SizeToData sizeTo(float width, float height, float duration, Interpolation interpolation) {
        SizeToData actionData = actionData(SizeToData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setEndWidth(width);
        actionData.setEndHeight(height);
        actionData.logicClassName = SizeToAction.class.getName();
        return actionData;
    }

    public static SizeByData sizeBy(float width, float height) {
        return sizeBy(width, height, 0, null);
    }

    public static SizeByData sizeBy(float width, float height, float duration) {
        return sizeBy(width, height, duration, null);
    }

    public static SizeByData sizeBy(float width, float height, float duration, Interpolation interpolation) {
        SizeByData actionData = actionData(SizeByData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setAmountWidth(width);
        actionData.setAmountHeight(height);
        actionData.logicClassName = SizeByAction.class.getName();
        return actionData;
    }

    public static ScaleToData scaleTo(float width, float height) {
        return scaleTo(width, height, 0, null);
    }

    public static ScaleToData scaleTo(float width, float height, float duration) {
        return scaleTo(width, height, duration, null);
    }

    public static ScaleToData scaleTo(float width, float height, float duration, Interpolation interpolation) {
        ScaleToData actionData = actionData(ScaleToData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setEndX(width);
        actionData.setEndY(height);
        actionData.logicClassName = ScaleToAction.class.getName();
        return actionData;
    }

    public static ScaleByData scaleBy(float width, float height) {
        return scaleBy(width, height, 0, null);
    }

    public static ScaleByData scaleBy(float width, float height, float duration) {
        return scaleBy(width, height, duration, null);
    }

    public static ScaleByData scaleBy(float width, float height, float duration, Interpolation interpolation) {
        ScaleByData actionData = actionData(ScaleByData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setAmountX(width);
        actionData.setAmountY(height);
        actionData.logicClassName = ScaleByAction.class.getName();
        return actionData;
    }

    public static ColorData color(Color color) {
        return color(color, 0, null);
    }

    public static ColorData color(Color color, float duration) {
        return color(color, duration, null);
    }

    public static ColorData color(Color color, float duration, Interpolation interpolation) {
        ColorData actionData = actionData(ColorData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setEndColor(color);
        actionData.logicClassName = ColorAction.class.getName();
        return actionData;
    }

    public static AlphaData alpha(float alpha) {
        return alpha(alpha, 0, null);
    }

    public static AlphaData alpha(float alpha, float duration) {
        return alpha(alpha, duration, null);
    }

    public static AlphaData alpha(float alpha, float duration, Interpolation interpolation) {
        AlphaData actionData = actionData(AlphaData.class);
        actionData.setDuration(duration);
        actionData.setInterpolation(interpolation);
        actionData.setEnd(alpha);
        actionData.logicClassName = AlphaAction.class.getName();
        return actionData;
    }

    public static AlphaData fadeIn(float duration) {
        return alpha(1, duration, null);
    }

    public static AlphaData fadeIn(float duration, Interpolation interpolation) {
        return alpha(1, duration, interpolation);
    }


    public static AlphaData fadeOut(float duration) {
        return alpha(0, duration, null);
    }

    public static AlphaData fadeOut(float duration, Interpolation interpolation) {
        return alpha(0, duration, interpolation);
    }

    public static DelayData delay(float duration) {
        DelayData actionData = actionData(DelayData.class);
        actionData.setDuration(duration);
        actionData.logicClassName = DelayAction.class.getName();
        return actionData;
    }

    public static DelayData delay(float duration, ActionData delayedAction) {
        DelayData actionData = actionData(DelayData.class);
        actionData.setDuration(duration);
        actionData.setDelegatedAction(delayedAction);
        actionData.logicClassName = DelayAction.class.getName();
        return actionData;
    }

    static public ParallelData parallel(ActionData... actionsData) {
        ParallelData actionData = actionData(ParallelData.class);
        actionData.setActionsData(actionsData);
        actionData.logicClassName = ParallelAction.class.getName();
        return actionData;
    }

    static public SequenceData sequence(ActionData... actionsData) {
        SequenceData actionData = actionData(SequenceData.class);
        actionData.setActionsData(actionsData);
        actionData.logicClassName = SequenceAction.class.getName();
        return actionData;
    }

    static public RepeatData repeat(int count, ActionData action) {
        RepeatData actionData = actionData(RepeatData.class);
        actionData.setRepeatCount(count);
        actionData.setDelegatedAction(action);
        actionData.logicClassName = RepeatAction.class.getName();
        return actionData;
    }

    static public RepeatData forever(ActionData action) {
        RepeatData actionData = actionData(RepeatData.class);
        actionData.setRepeatCount(RepeatData.FOREVER);
        actionData.setDelegatedAction(action);
        actionData.logicClassName = RepeatAction.class.getName();
        return actionData;
    }

    public static void addAction(PooledEngine engine, final Entity entity, ActionData data) {
        checkInit();
        ActionComponent actionComponent;
        actionComponent = ComponentRetriever.get(entity, ActionComponent.class);

        if (actionComponent == null) {
            actionComponent = engine.createComponent(ActionComponent.class);
            entity.add(actionComponent);
        }

        actionComponent.dataArray.add(data);
    }

    public static void removeActions(Entity entity) {
        ActionComponent actionComponent = ComponentRetriever.get(entity, ActionComponent.class);
        if (actionComponent != null) {
            actionComponent.reset(); // action component with empty data array will be removed later by ActionSystem
        }
    }

    public static void removeAction(Entity entity, ActionData data) {
        ActionComponent actionComponent = ComponentRetriever.get(entity, ActionComponent.class);
        if (actionComponent != null) {
            if (actionComponent.dataArray.contains(data, true)) {
                actionComponent.dataArray.removeValue(data, true);
                if (data.getPool() != null)
                    data.getPool().free(data);
            }
        }
    }
}
