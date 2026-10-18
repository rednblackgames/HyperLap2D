package games.rednblack.editor.remote;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.controller.commands.SelectionPayload;
import games.rednblack.editor.controller.commands.component.UpdateWidgetPartDataCommand;
import games.rednblack.editor.proxy.PluginUIBridge;
import games.rednblack.editor.proxy.WidgetEditingProxy;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WidgetPartComponent;
import games.rednblack.editor.renderer.data.WidgetOverrideSequenceVO;
import games.rednblack.editor.renderer.data.WidgetOverrideTransitionVO;
import games.rednblack.editor.renderer.data.WidgetPartVO;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.InterpolationMap;
import games.rednblack.editor.renderer.widget.ChoiceOverrideHandler;
import games.rednblack.editor.renderer.widget.ColorPreviewHandler;
import games.rednblack.editor.renderer.widget.InterpolableOverrideHandler;
import games.rednblack.editor.renderer.widget.SequencedOverrideHandler;
import games.rednblack.editor.renderer.widget.StateOverrideHandler;
import games.rednblack.editor.renderer.widget.ToggleOverrideHandler;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.remote.RemoteWidgetRequest;
import games.rednblack.h2d.common.remote.RemoteWidgetResult;
import games.rednblack.puremvc.Facade;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The widget operations of the remote bridge that no properties panel field covers: making a widget,
 * setting a state override on one of its parts, and showing it in a state. Run on the render thread.
 *
 * An override is checked the way the editor would only ever produce it: for a state of the widget
 * other than its default one, on a property the item has and the widget does not drive itself, with
 * a value its handler can read, and with transitions and sequences only where they can play.
 */
final class RemoteWidgetOps {

    private RemoteWidgetOps() {
    }

    static void handle(RemoteWidgetRequest req) {
        RemoteWidgetResult result = new RemoteWidgetResult();
        try {
            if (req.op == null) {
                result.error = "missing op";
            } else {
                Engine engine = PluginUIBridge.get(Facade.getInstance()).getSandbox().getEngine();
                switch (req.op) {
                    case CONVERT:
                        convert(engine, req, result);
                        break;
                    case SET_OVERRIDE:
                        setOverride(engine, req, result);
                        break;
                    case SHOW_STATE:
                        showState(engine, req, result);
                        break;
                }
            }
        } catch (Throwable t) {
            result.ok = false;
            result.error = "widget operation failed: " + t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        req.handle.complete(result);
    }

    private static void convert(Engine engine, RemoteWidgetRequest req, RemoteWidgetResult result) {
        if (WidgetTypes.get(req.widgetType) == null) {
            result.error = "unknown widget type: " + req.widgetType + " (known: " + String.join(", ", WidgetTypes.names()) + ")";
            return;
        }
        if (req.entityIds == null || req.entityIds.isEmpty()) {
            result.error = "no items to convert";
            return;
        }

        Set<Integer> items = new LinkedHashSet<>();
        int parent = Integer.MIN_VALUE;
        for (String id : req.entityIds) {
            int entity = entity(engine, id);
            if (entity == -1) {
                result.error = "entity not found: " + id;
                return;
            }
            ParentNodeComponent parentNode = ComponentRetriever.get(entity, ParentNodeComponent.class, engine);
            int itsParent = parentNode == null ? -1 : parentNode.parentEntity;
            if (parent != Integer.MIN_VALUE && itsParent != parent) {
                result.error = "the items must all be in the same composite";
                return;
            }
            parent = itsParent;
            items.add(entity);
        }

        int first = items.iterator().next();
        boolean single = items.size() == 1 && EntityUtils.getType(first) == EntityFactory.COMPOSITE_TYPE;
        if (single && ComponentRetriever.get(first, WidgetComponent.class, engine) != null) {
            result.error = "the composite is already a widget";
            return;
        }

        // the same road as the menu: the items are selected, then converted
        Facade facade = Facade.getInstance();
        facade.sendNotification(MsgAPI.ACTION_SET_SELECTION,
                items.size() == 1 ? SelectionPayload.single(first) : SelectionPayload.multiple(items));
        facade.sendNotification(MsgAPI.ACTION_CONVERT_TO_WIDGET, req.widgetType);

        // a composite becomes the widget itself; anything else is wrapped into a new one
        int widget = first;
        if (!single) {
            ParentNodeComponent parentNode = ComponentRetriever.get(first, ParentNodeComponent.class, engine);
            widget = parentNode == null ? -1 : parentNode.parentEntity;
        }
        if (widget == -1 || ComponentRetriever.get(widget, WidgetComponent.class, engine) == null) {
            result.error = "the conversion did not happen";
            return;
        }
        result.widgetId = EntityUtils.getEntityId(widget);
        result.ok = true;
    }

    private static void setOverride(Engine engine, RemoteWidgetRequest req, RemoteWidgetResult result) {
        int entity = entity(engine, req.entityId);
        if (entity == -1) {
            result.error = "entity not found: " + req.entityId;
            return;
        }
        ParentNodeComponent parentNode = ComponentRetriever.get(entity, ParentNodeComponent.class, engine);
        int widgetEntity = parentNode == null ? -1 : WidgetEditingProxy.findWidget(parentNode.parentEntity);
        if (widgetEntity == -1) {
            result.error = "entity is not inside a widget";
            return;
        }
        WidgetComponent widget = ComponentRetriever.get(widgetEntity, WidgetComponent.class, engine);

        if (req.state == null || !widget.states.contains(req.state, false)) {
            result.error = "unknown state: " + req.state + " (this widget has: " + widget.states.toString(", ") + ")";
            return;
        }
        if (req.state.equals(widget.defaultState)) {
            result.error = "'" + req.state + "' is the default state, which is the item's own look: change the item itself (update_component)";
            return;
        }

        WidgetStateSystem stateSystem = engine.getSystem(WidgetStateSystem.class);
        StateOverrideHandler handler = req.key == null ? null : stateSystem.getHandler(req.key);
        if (handler == null) {
            result.error = "unknown property: " + req.key + " (properties: " + String.join(", ", stateSystem.getHandlerKeys()) + ")";
            return;
        }
        if (!handler.supports(entity)) {
            result.error = "this item has no '" + req.key + "' to override";
            return;
        }
        if (WidgetEditingProxy.isDriven(entity, req.key)) {
            result.error = "the widget sets '" + req.key + "' of this part itself";
            return;
        }

        WidgetPartVO current = new WidgetPartVO();
        WidgetPartComponent part = ComponentRetriever.get(entity, WidgetPartComponent.class, engine);
        if (part != null) current.loadFromComponent(part);
        WidgetPartVO changed = new WidgetPartVO(current);

        if (req.remove) {
            changed.setOverride(req.state, req.key, null);
        } else {
            String value = req.value;
            if (value == null) {
                result.error = "a value is needed, unless the override is removed";
                return;
            }
            String problem = checkValue(entity, handler, value);
            if (problem != null) {
                result.validationErrors.add(req.key + ": " + problem);
                return;
            }
            // a switch's base is on: being on in a state is having nothing to say about it
            if (handler instanceof ToggleOverrideHandler && "true".equals(value)) changed.setOverride(req.state, req.key, null);
            else changed.setOverride(req.state, req.key, value);

            if (req.setTransition && !applyTransition(changed, handler, req, result)) return;
            if (req.setSequence && !applySequence(changed, entity, handler, req, result)) return;
        }

        if (!changed.equals(current)) {
            Facade.getInstance().sendNotification(MsgAPI.ACTION_UPDATE_WIDGET_PART_DATA,
                    UpdateWidgetPartDataCommand.payload(entity, changed));
        }
        result.ok = true;
    }

    /** @return what is wrong with the value, null if the property can take it */
    static String checkValue(int entity, StateOverrideHandler handler, String value) {
        if (handler instanceof ToggleOverrideHandler) {
            return "true".equals(value) || "false".equals(value) ? null : "must be true or false";
        }
        if (handler instanceof ChoiceOverrideHandler) {
            Array<String> choices = ((ChoiceOverrideHandler) handler).getChoices(entity);
            if (choices != null && choices.size > 0 && !choices.contains(value, false)) {
                return "'" + value + "' is not one of " + choices.toString(", ");
            }
            return null;
        }
        if (handler instanceof InterpolableOverrideHandler) {
            InterpolableOverrideHandler numbers = (InterpolableOverrideHandler) handler;
            float[] channels = new float[numbers.getChannelCount()];
            return numbers.parseChannels(value, channels) ? null
                    : "cannot be read as " + (channels.length == 1 ? "a number" : channels.length + " numbers (a colour as an 8 digit hex)");
        }
        if (handler instanceof ColorPreviewHandler) {
            return ((ColorPreviewHandler) handler).toColor(entity, value, new Color()) ? null : "cannot be read as a colour";
        }
        return null;
    }

    private static boolean applyTransition(WidgetPartVO vo, StateOverrideHandler handler, RemoteWidgetRequest req, RemoteWidgetResult result) {
        if (req.transitionDuration == null) {
            vo.setTransition(req.state, req.key, null);
            return true;
        }
        if (!(handler instanceof InterpolableOverrideHandler)) {
            result.validationErrors.add("transition: '" + req.key + "' is not made of numbers, so it cannot travel to its value");
            return false;
        }
        if (req.transitionDuration < 0) {
            result.validationErrors.add("transition: the duration cannot be negative");
            return false;
        }
        String interpolation = req.transitionInterpolation == null || req.transitionInterpolation.isEmpty()
                ? WidgetOverrideTransitionVO.DEFAULT_INTERPOLATION : req.transitionInterpolation;
        if (!InterpolationMap.map.containsKey(interpolation)) {
            result.validationErrors.add("transition: unknown interpolation '" + interpolation + "' (known: "
                    + String.join(", ", InterpolationMap.map.keySet()) + ")");
            return false;
        }
        vo.setTransition(req.state, req.key, new WidgetOverrideTransitionVO(req.transitionDuration, interpolation));
        return true;
    }

    private static boolean applySequence(WidgetPartVO vo, int entity, StateOverrideHandler handler, RemoteWidgetRequest req, RemoteWidgetResult result) {
        String enter = req.sequenceEnter == null ? "" : req.sequenceEnter;
        String exit = req.sequenceExit == null ? "" : req.sequenceExit;
        if (enter.isEmpty() && exit.isEmpty()) {
            vo.setSequence(req.state, req.key, null);
            return true;
        }
        if (!(handler instanceof SequencedOverrideHandler)) {
            result.validationErrors.add("sequence: '" + req.key + "' does not play, so nothing can play before it");
            return false;
        }
        if (handler instanceof ChoiceOverrideHandler) {
            Array<String> choices = ((ChoiceOverrideHandler) handler).getChoices(entity);
            for (String name : new String[]{enter, exit}) {
                if (!name.isEmpty() && choices != null && choices.size > 0 && !choices.contains(name, false)) {
                    result.validationErrors.add("sequence: '" + name + "' is not one of " + choices.toString(", "));
                    return false;
                }
            }
        }
        vo.setSequence(req.state, req.key, new WidgetOverrideSequenceVO(enter, exit));
        return true;
    }

    private static void showState(Engine engine, RemoteWidgetRequest req, RemoteWidgetResult result) {
        int entity = entity(engine, req.entityId);
        WidgetComponent widget = entity == -1 ? null : ComponentRetriever.get(entity, WidgetComponent.class, engine);
        if (widget == null) {
            result.error = "not a widget: " + req.entityId;
            return;
        }
        if (req.state == null || !widget.states.contains(req.state, false)) {
            result.error = "unknown state: " + req.state + " (this widget has: " + widget.states.toString(", ") + ")";
            return;
        }
        WidgetEditingProxy.get().switchState(req.entityId, req.state);
        result.ok = true;
    }

    /** @return the entity with that uniqueId, -1 when there is none */
    private static int entity(Engine engine, String id) {
        if (id == null) return -1;
        int entity = EntityUtils.getByUniqueId(id);
        if (entity < 0 || !engine.getEntityManager().isActive(entity)
                || ComponentRetriever.get(entity, MainItemComponent.class, engine) == null) return -1;
        return entity;
    }
}
