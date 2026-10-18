package games.rednblack.editor.plugin.mcp.tools.widget;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.plugin.mcp.server.McpJson;
import games.rednblack.editor.plugin.mcp.server.RenderThread;
import games.rednblack.editor.plugin.mcp.tools.McpContext;
import games.rednblack.editor.plugin.mcp.tools.McpToolResult;
import games.rednblack.editor.plugin.mcp.tools.RemoteOps;
import games.rednblack.editor.plugin.mcp.tools.Tool;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WidgetPartComponent;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.InterpolationMap;
import games.rednblack.editor.renderer.widget.ChoiceOverrideHandler;
import games.rednblack.editor.renderer.widget.ColorPreviewHandler;
import games.rednblack.editor.renderer.widget.InterpolableOverrideHandler;
import games.rednblack.editor.renderer.widget.SequencedOverrideHandler;
import games.rednblack.editor.renderer.widget.StateOverrideHandler;
import games.rednblack.editor.renderer.widget.ToggleOverrideHandler;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.h2d.common.remote.RemoteWidgetRequest;
import games.rednblack.h2d.common.remote.RemoteWidgetResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Widgets: what types there are, what a widget is made of, making one, and the looks of its states.
 *
 * Reading is done here from the runtime data; changes go through the editor (RemoteOps), which
 * checks them and makes them undoable. Widget settings and part roles are changed with
 * update_component (keys widget and widgetPart), like any other panel.
 */
public final class WidgetTools {

    private WidgetTools() {
    }

    private static McpToolResult format(RemoteWidgetResult r, String okText) {
        if (r.ok) return McpToolResult.text(okText);
        StringBuilder sb = new StringBuilder("error: ").append(r.error != null ? r.error : "rejected");
        if (r.validationErrors != null && !r.validationErrors.isEmpty()) {
            sb.append(" | validation: ").append(String.join("; ", r.validationErrors));
        }
        return McpToolResult.error(sb.toString());
    }

    private static void stringProperty(JsonWriter w, String name, String description) throws IOException {
        w.object(name);
        w.set("type", "string");
        w.set("description", description);
        w.pop();
    }

    /** What a state can say about a property, as the handler of that property makes it. */
    private static void writeOverrideKind(JsonWriter w, StateOverrideHandler handler, int entity) throws IOException {
        String kind;
        if (handler instanceof ToggleOverrideHandler) kind = "toggle";
        else if (handler instanceof ChoiceOverrideHandler) kind = "choice";
        else if (handler instanceof InterpolableOverrideHandler) kind = ((InterpolableOverrideHandler) handler).getChannelCount() == 4 ? "color" : "number";
        else if (handler instanceof ColorPreviewHandler) kind = "color";
        else kind = "text";
        w.set("kind", kind);
        if (handler instanceof InterpolableOverrideHandler) w.set("animatable", true);
        if (handler instanceof SequencedOverrideHandler) w.set("plays", true);
        if (entity != -1 && handler instanceof ChoiceOverrideHandler) {
            Array<String> choices = ((ChoiceOverrideHandler) handler).getChoices(entity);
            if (choices != null && choices.size > 0) {
                w.array("choices");
                for (String choice : choices) w.value(choice);
                w.pop();
            }
        }
    }

    /** @return the entity with that uniqueId in the scene, -1 when there is none */
    private static int find(Engine engine, int entity, String uniqueId) {
        MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class, engine);
        if (main != null && uniqueId.equals(main.uniqueId)) return entity;
        NodeComponent node = ComponentRetriever.get(entity, NodeComponent.class, engine);
        if (node == null) return -1;
        Integer[] children = node.children.begin();
        try {
            for (int i = 0, n = node.children.size; i < n; i++) {
                int found = find(engine, children[i], uniqueId);
                if (found != -1) return found;
            }
        } finally {
            node.children.end();
        }
        return -1;
    }

    public static final class ListWidgetTypesTool implements Tool {
        private final McpContext ctx;
        public ListWidgetTypesTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "list_widget_types"; }
        @Override public String description() {
            return "List the widget types convert_to_widget can make: for each, its states (the first is the default, which "
                    + "is the item's own look, and 'builds on' tells which state another inherits overrides from), its parts "
                    + "(role, whether required, and the properties the widget sets on that part itself, which cannot be overridden), "
                    + "and its settings (key, kind BOOLEAN/INT/FLOAT/STRING/INTERPOLATION/CHOICE/LIST, default, choices). "
                    + "Also lists overrideProperties: every property a state can override with set_widget_override, its kind "
                    + "(toggle true/false, number, color as 8 digit hex RRGGBBAA, choice, text), whether it can be animated "
                    + "with a transition and whether it plays (so it can have a sequence).";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            Engine engine = ctx.api().getEngine();
            String json = RenderThread.run(() -> McpJson.object(w -> {
                w.array("types");
                for (String name : WidgetTypes.names()) {
                    WidgetType type = WidgetTypes.get(name);
                    w.object();
                    w.set("type", name);
                    w.array("states");
                    for (String state : type.states) {
                        w.object();
                        w.set("name", state);
                        String parent = type.parents.get(state);
                        if (parent != null) w.set("buildsOn", parent);
                        w.pop();
                    }
                    w.pop();
                    w.array("parts");
                    for (WidgetType.Part part : type.parts) {
                        w.object();
                        w.set("role", part.role);
                        w.set("required", part.required);
                        if (part.drivenKeys.size > 0) {
                            w.array("drivenProperties");
                            for (String key : part.drivenKeys) w.value(key);
                            w.pop();
                        }
                        w.pop();
                    }
                    w.pop();
                    w.array("settings");
                    for (WidgetType.Property property : type.properties) {
                        w.object();
                        w.set("key", property.key);
                        w.set("kind", property.kind.name());
                        w.set("default", property.defaultValue);
                        if (property.choices.size > 0) {
                            w.array("choices");
                            for (String choice : property.choices) w.value(choice);
                            w.pop();
                        }
                        w.pop();
                    }
                    w.pop();
                    if (type.wrapRole != null) w.set("wrapsContentInto", type.wrapRole);
                    w.pop();
                }
                w.pop();

                w.array("overrideProperties");
                WidgetStateSystem stateSystem = engine == null ? null : engine.getSystem(WidgetStateSystem.class);
                if (stateSystem != null) {
                    for (String key : new TreeSet<>(java.util.Arrays.asList(stateSystem.getHandlerKeys()))) {
                        w.object();
                        w.set("key", key);
                        writeOverrideKind(w, stateSystem.getHandler(key), -1);
                        w.pop();
                    }
                }
                w.pop();

                w.array("interpolations");
                for (String name : new TreeSet<>(InterpolationMap.map.keySet())) w.value(name);
                w.pop();
            }), 5000);
            return McpToolResult.text(json);
        }
    }

    public static final class GetWidgetTool implements Tool {
        private final McpContext ctx;
        public GetWidgetTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "get_widget"; }
        @Override public String description() {
            return "Describe one widget: type, states, the state it shows, its default state, its settings, and its parts "
                    + "(every item inside it, nested widgets included as parts but not looked into). For each part: uniqueId, "
                    + "name, role, the properties a state can override on it (kind, choices, animatable, plays; the ones the "
                    + "widget sets itself are left out) and its overrides per state with their transitions and sequences.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            stringProperty(w, "widgetId", "The widget's uniqueId (from list_entities, where widgets have widgetType).");
            w.pop();
            w.name("required");
            w.array();
            w.value("widgetId");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            String widgetId = args == null ? "" : args.getString("widgetId", "");
            if (ctx.api().getSceneLoader() == null) return McpToolResult.error("error: no scene is open");
            Engine engine = ctx.api().getEngine();
            String json = RenderThread.run(() -> {
                int widget = find(engine, ctx.api().getSceneLoader().getRoot(), widgetId);
                WidgetComponent component = widget == -1 ? null : ComponentRetriever.get(widget, WidgetComponent.class, engine);
                if (component == null) return null;
                return McpJson.object(w -> {
                    WidgetType type = WidgetTypes.get(component.widgetType);
                    w.set("widgetId", widgetId);
                    w.set("type", component.widgetType);
                    w.set("defaultState", component.defaultState);
                    w.set("shownState", component.getState());
                    w.array("states");
                    for (String state : component.states) w.value(state);
                    w.pop();
                    w.object("settings");
                    for (ObjectMap.Entry<String, String> setting : component.properties) w.set(setting.key, setting.value);
                    w.pop();
                    w.array("parts");
                    writeParts(engine, widget, type, w);
                    w.pop();
                });
            }, 5000);
            return json == null ? McpToolResult.error("error: not a widget: " + widgetId) : McpToolResult.text(json);
        }

        private void writeParts(Engine engine, int parent, WidgetType type, JsonWriter w) throws IOException {
            NodeComponent node = ComponentRetriever.get(parent, NodeComponent.class, engine);
            if (node == null) return;
            WidgetStateSystem stateSystem = engine.getSystem(WidgetStateSystem.class);
            Integer[] children = node.children.begin();
            try {
                for (int i = 0, n = node.children.size; i < n; i++) {
                    int child = children[i];
                    writePart(engine, stateSystem, child, type, w);
                    // the parts of a nested widget are its own business
                    if (ComponentRetriever.get(child, WidgetComponent.class, engine) == null) writeParts(engine, child, type, w);
                }
            } finally {
                node.children.end();
            }
        }

        private void writePart(Engine engine, WidgetStateSystem stateSystem, int entity, WidgetType type, JsonWriter w) throws IOException {
            MainItemComponent main = ComponentRetriever.get(entity, MainItemComponent.class, engine);
            WidgetPartComponent part = ComponentRetriever.get(entity, WidgetPartComponent.class, engine);
            WidgetComponent nested = ComponentRetriever.get(entity, WidgetComponent.class, engine);
            String role = part == null || part.role == null ? "" : part.role;
            WidgetType.Part declared = type == null || role.isEmpty() ? null : type.getPart(role);

            w.object();
            w.set("uniqueId", main != null ? main.uniqueId : String.valueOf(entity));
            w.set("name", main != null ? main.itemIdentifier : "");
            w.set("role", role);
            if (nested != null) w.set("nestedWidget", nested.widgetType);

            if (stateSystem != null) {
                w.array("overridable");
                for (String key : new TreeSet<>(java.util.Arrays.asList(stateSystem.getHandlerKeys()))) {
                    StateOverrideHandler handler = stateSystem.getHandler(key);
                    if (!handler.supports(entity)) continue;
                    if (declared != null && declared.drivenKeys.contains(key, false)) continue;
                    w.object();
                    w.set("key", key);
                    writeOverrideKind(w, handler, entity);
                    w.pop();
                }
                w.pop();
            }

            if (part != null && part.overrides.size > 0) {
                w.object("overrides");
                for (ObjectMap.Entry<String, ObjectMap<String, String>> state : part.overrides) {
                    w.object(state.key);
                    for (ObjectMap.Entry<String, String> value : state.value) {
                        WidgetPartComponent.Transition transition = part.getTransition(state.key, value.key);
                        WidgetPartComponent.Sequence sequence = part.getSequence(state.key, value.key);
                        if (transition == null && sequence == null) {
                            w.set(value.key, value.value);
                            continue;
                        }
                        w.object(value.key);
                        w.set("value", value.value);
                        if (transition != null) {
                            w.object("transition");
                            w.set("duration", transition.duration);
                            w.set("interpolation", transition.interpolation);
                            w.pop();
                        }
                        if (sequence != null) {
                            w.object("sequence");
                            w.set("enter", sequence.enter);
                            w.set("exit", sequence.exit);
                            w.pop();
                        }
                        w.pop();
                    }
                    w.pop();
                }
                w.pop();
            }
            w.pop();
        }
    }

    public static final class ConvertToWidgetTool implements Tool {
        private final McpContext ctx;
        public ConvertToWidgetTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "convert_to_widget"; }
        @Override public String description() {
            return "Make a widget, like the editor's 'Convert to widget' menu: one composite becomes the widget itself; any "
                    + "other items (all in the same composite) are wrapped into a new composite that becomes it. A single "
                    + "image converted into a type that wants one (e.g. window) becomes its background. The type's default "
                    + "parts are added. The editor then shows the inside of the widget. Undoable. Returns the widget's uniqueId.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            w.object("entityIds");
            w.set("type", "array");
            w.object("items");
            w.set("type", "string");
            w.pop();
            w.set("description", "uniqueIds of the items to make the widget from.");
            w.pop();
            stringProperty(w, "widgetType", "The widget type (from list_widget_types), e.g. button, checkBox, slider, window.");
            w.pop();
            w.name("required");
            w.array();
            w.value("entityIds");
            w.value("widgetType");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteWidgetRequest req = new RemoteWidgetRequest();
            req.op = RemoteWidgetRequest.Op.CONVERT;
            req.widgetType = args.getString("widgetType", "");
            JsonValue ids = args.get("entityIds");
            if (ids != null && ids.isArray()) {
                for (JsonValue id = ids.child; id != null; id = id.next) req.entityIds.add(id.asString());
            } else if (ids != null) {
                req.entityIds.add(ids.asString());
            }
            RemoteWidgetResult r = new RemoteOps(ctx).widget(req, 5000);
            return format(r, r.widgetId != null ? r.widgetId : "ok");
        }
    }

    public static final class SetWidgetOverrideTool implements Tool {
        private final McpContext ctx;
        public SetWidgetOverrideTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "set_widget_override"; }
        @Override public String description() {
            return "Set, or with remove:true take away, what a widget state changes on one of its parts: e.g. the tint of the "
                    + "background when pressed. Not for the default state, which is the item's own look (change the item itself). "
                    + "The property must be one get_widget lists as overridable on that part; the value follows its kind: toggle "
                    + "'true'/'false' (visible can only be turned off), number, color as 8 digit hex RRGGBBAA, choice (one of its "
                    + "choices), text. Optional transition {duration seconds, interpolation} animates a number or color property "
                    + "to the value (null takes it away); optional sequence {enter, exit} plays animations around a property that "
                    + "plays (both empty take it away). Omitted transition/sequence keep what is there. Undoable.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            stringProperty(w, "entityId", "uniqueId of the part (an item inside the widget).");
            stringProperty(w, "state", "The state, one of the widget's states other than its default one.");
            stringProperty(w, "key", "The property, e.g. tint, x, y, visible, region, labelText.");
            w.object("value");
            w.set("description", "The value in that state, as text; numbers and booleans are accepted too. Not needed with remove.");
            w.pop();
            w.object("remove");
            w.set("type", "boolean");
            w.set("description", "Take the override away, with its transition and sequence.");
            w.pop();
            w.object("transition");
            w.set("type", "object");
            w.set("description", "{duration, interpolation}; null removes the transition.");
            w.pop();
            w.object("sequence");
            w.set("type", "object");
            w.set("description", "{enter, exit}: animations played before the value; both empty remove the sequence.");
            w.pop();
            w.pop();
            w.name("required");
            w.array();
            w.value("entityId");
            w.value("state");
            w.value("key");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteWidgetRequest req = new RemoteWidgetRequest();
            req.op = RemoteWidgetRequest.Op.SET_OVERRIDE;
            req.entityId = args.getString("entityId", "");
            req.state = args.getString("state", null);
            req.key = args.getString("key", null);
            req.remove = args.getBoolean("remove", false);
            JsonValue value = args.get("value");
            if (value != null && !value.isNull()) req.value = value.isBoolean() ? String.valueOf(value.asBoolean()) : asText(value);

            JsonValue transition = args.get("transition");
            if (transition != null) {
                req.setTransition = true;
                if (!transition.isNull()) {
                    JsonValue duration = transition.get("duration");
                    req.transitionDuration = duration == null || duration.isNull() ? null : duration.asFloat();
                    req.transitionInterpolation = transition.getString("interpolation", null);
                }
            }
            JsonValue sequence = args.get("sequence");
            if (sequence != null) {
                req.setSequence = true;
                if (!sequence.isNull()) {
                    req.sequenceEnter = sequence.getString("enter", "");
                    req.sequenceExit = sequence.getString("exit", "");
                }
            }
            return format(new RemoteOps(ctx).widget(req, 5000), "ok");
        }

        /** A number written the shortest way, so 1 is "1" and not "1.0". */
        private static String asText(JsonValue value) {
            if (value.isLong()) return String.valueOf(value.asLong());
            if (value.isDouble()) {
                double number = value.asDouble();
                return number == Math.rint(number) ? String.valueOf((long) number) : String.valueOf((float) number);
            }
            return value.asString();
        }
    }

    public static final class ShowWidgetStateTool implements Tool {
        private final McpContext ctx;
        public ShowWidgetStateTool(McpContext ctx) { this.ctx = ctx; }
        @Override public String name() { return "show_widget_state"; }
        @Override public String description() {
            return "Show a widget in one of its states in the editor, like picking it in the state bar, e.g. to take a "
                    + "screenshot of the pressed look. Records nothing. Showing the default state goes back to the item's own look.";
        }
        @Override
        public void writeInputSchema(JsonWriter w) throws IOException {
            w.set("type", "object");
            w.object("properties");
            stringProperty(w, "widgetId", "The widget's uniqueId.");
            stringProperty(w, "state", "One of the widget's states.");
            w.pop();
            w.name("required");
            w.array();
            w.value("widgetId");
            w.value("state");
            w.pop();
            w.set("additionalProperties", false);
        }
        @Override
        public McpToolResult call(JsonValue args) {
            RemoteWidgetRequest req = new RemoteWidgetRequest();
            req.op = RemoteWidgetRequest.Op.SHOW_STATE;
            req.entityId = args.getString("widgetId", "");
            req.state = args.getString("state", null);
            return format(new RemoteOps(ctx).widget(req, 5000), "ok");
        }
    }

    /** All of them, for the registry. */
    public static List<Tool> all(McpContext ctx) {
        List<Tool> tools = new ArrayList<>();
        tools.add(new ListWidgetTypesTool(ctx));
        tools.add(new GetWidgetTool(ctx));
        tools.add(new ConvertToWidgetTool(ctx));
        tools.add(new SetWidgetOverrideTool(ctx));
        tools.add(new ShowWidgetStateTool(ctx));
        return tools;
    }
}
