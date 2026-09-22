package games.rednblack.editor.utils.widget;

import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.renderer.components.LayoutComponent;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.widget.WidgetComponent;
import games.rednblack.editor.renderer.components.widget.WidgetPartComponent;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import games.rednblack.editor.renderer.ecs.Engine;
import games.rednblack.editor.renderer.systems.WidgetStateSystem;
import games.rednblack.editor.renderer.widget.StateOverrideHandler;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.renderer.widget.WidgetTypes;
import games.rednblack.editor.renderer.widget.handlers.CoreStateOverrides;

import java.util.Objects;

/**
 * Turns ordinary edits into widget state overrides.
 *
 * While a widget shows a state other than its default one, whatever is changed on its parts belongs
 * to that state only. Rather than teaching every command about states, the recorder looks at the
 * result: after each command it compares the live value of every overridable property with its
 * base value. A difference becomes an override of the shown state, a match removes the override.
 *
 * Edits made while the default state is shown are not touched: they change the base look.
 */
public class WidgetStateRecorder {

    private final Engine engine;
    private final WidgetStateSystem stateSystem;

    private final ComponentMapper<WidgetComponent> widgetCM;
    private final ComponentMapper<WidgetPartComponent> partCM;
    private final ComponentMapper<NodeComponent> nodeCM;
    private final ComponentMapper<MainItemComponent> mainItemCM;
    private final ComponentMapper<LayoutComponent> layoutCM;

    /**
     * uniqueId -> (property key -> base value), for the properties the shown state does not
     * override. Their base value is simply what they were the last time they were looked at, which
     * is what makes a later change detectable.
     */
    private final ObjectMap<String, ObjectMap<String, String>> knownBase = new ObjectMap<>();
    private boolean overridesChanged;
    private static final int MAX_INHERITANCE = 8;

    public WidgetStateRecorder(Engine engine) {
        this.engine = engine;
        stateSystem = engine.getSystem(WidgetStateSystem.class);
        widgetCM = engine.getMapper(WidgetComponent.class);
        partCM = engine.getMapper(WidgetPartComponent.class);
        nodeCM = engine.getMapper(NodeComponent.class);
        mainItemCM = engine.getMapper(MainItemComponent.class);
        layoutCM = engine.getMapper(LayoutComponent.class);
    }

    /**
     * Makes the widget show another state: edits of the state being left are recorded first, then
     * the parts are brought to the look of the new one.
     */
    public void switchState(int widgetEntity, String newState) {
        WidgetComponent widget = widgetCM.get(widgetEntity);
        if (widget == null) return;

        record(widgetEntity);

        widget.currentState = widget.defaultState.equals(newState) || !widget.hasState(newState) ? null : newState;
        knownBase.clear();

        // show the new state now and take note of the base values the coming edits are compared to
        record(widgetEntity);
    }

    /**
     * Records the edits made to the parts of the widget since the last call as overrides of the
     * state it shows. Does nothing while the default state is shown.
     *
     * @return true if any override has been added, changed or removed
     */
    public boolean record(int widgetEntity) {
        WidgetComponent widget = widgetCM.get(widgetEntity);
        if (widget == null) return false;

        boolean recording = !widget.getState().equals(widget.defaultState);
        overridesChanged = false;
        visitParts(widgetEntity, widget, widget.getState(), recording);
        return overridesChanged;
    }

    public void reset() {
        knownBase.clear();
    }

    private void visitParts(int parent, WidgetComponent widget, String state, boolean recording) {
        NodeComponent node = nodeCM.get(parent);
        if (node == null) return;

        for (int i = 0; i < node.children.size; i++) {
            int child = node.children.get(i);

            stateSystem.applyNow(child);
            if (recording) recordEntity(child, widget, state);

            // parts of a nested widget follow that widget, not this one
            if (!widgetCM.has(child)) visitParts(child, widget, state, recording);
        }
    }

    private void recordEntity(int entity, WidgetComponent widget, String state) {
        MainItemComponent mainItem = mainItemCM.get(entity);
        if (mainItem == null || mainItem.uniqueId == null) return;

        ObjectMap<String, String> entityBase = knownBase.get(mainItem.uniqueId);
        if (entityBase == null) {
            entityBase = new ObjectMap<>();
            knownBase.put(mainItem.uniqueId, entityBase);
        }

        WidgetPartComponent part = partCM.get(entity);

        for (String key : stateSystem.getHandlerKeys()) {
            StateOverrideHandler handler = stateSystem.getHandler(key);
            if (!handler.supports(entity) || !isRecordable(entity, widget, key)) continue;

            String live = handler.capture(entity);

            String base;
            if (part != null && part.baseSnapshot.containsKey(key)) {
                base = part.baseSnapshot.get(key);
            } else if (entityBase.containsKey(key)) {
                base = entityBase.get(key);
            } else {
                // first time this property is seen while the state is shown: that is its base value
                base = live;
            }
            entityBase.put(key, base);

            // What the state would show without an override of its own: the value a state it builds
            // on gives the property, or else the base one. Only a difference from that is its own.
            String reference = inheritedValue(part, widget, state, key, base);

            if (!Objects.equals(live, reference)) {
                if (part == null) {
                    part = partCM.create(entity);
                    part.appliedState = state;
                }
                ObjectMap<String, String> patch = part.overrides.get(state);
                if (patch == null) {
                    patch = new ObjectMap<>(2);
                    part.overrides.put(state, patch);
                }
                if (!Objects.equals(live, patch.put(key, live))) overridesChanged = true;
                part.baseSnapshot.put(key, base);
            } else if (part != null) {
                ObjectMap<String, String> patch = part.overrides.get(state);
                if (patch != null) {
                    if (patch.remove(key) != null) overridesChanged = true;
                    if (patch.size == 0) part.overrides.remove(state);
                }
                // how a value that is no longer overridden is reached, and what plays around it,
                // are both of no use any more
                part.removeTransition(state, key);
                part.removeSequence(state, key);
                // still off its base while an inherited value holds it, which restoring needs to know
                if (Objects.equals(reference, base)) part.baseSnapshot.remove(key);
            }
        }
    }

    /** @return the value the nearest state this one builds on gives the property, else the base one */
    private String inheritedValue(WidgetPartComponent part, WidgetComponent widget, String state, String key, String base) {
        if (part == null) return base;

        int depth = 0;
        for (String link = widget.parentOf(state); link != null && depth++ < MAX_INHERITANCE; link = widget.parentOf(link)) {
            ObjectMap<String, String> overrides = part.overrides.get(link);
            if (overrides != null && overrides.containsKey(key)) return overrides.get(key);
        }
        return base;
    }

    /**
     * A position driven by layout constraints moves on its own whenever a sibling changes, and a part
     * the widget drives, such as the knob of a progress bar, is moved by the widget: neither is an
     * edit of the state.
     */
    private boolean isRecordable(int entity, WidgetComponent widget, String key) {
        WidgetPartComponent part = partCM.get(entity);
        WidgetType type = WidgetTypes.get(widget.widgetType);
        if (part != null && type != null && part.role != null && !part.role.isEmpty()) {
            WidgetType.Part role = type.getPart(part.role);
            if (role != null && role.drivenKeys.contains(key, false)) return false;
        }

        LayoutComponent layout = layoutCM.get(entity);
        if (layout == null) return true;

        if (CoreStateOverrides.X.equals(key)) return layout.left == null && layout.right == null;
        if (CoreStateOverrides.Y.equals(key)) return layout.top == null && layout.bottom == null;
        return true;
    }
}
