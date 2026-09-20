package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.OrderedMap;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.renderer.data.WidgetOverrideSequenceVO;
import games.rednblack.editor.renderer.data.WidgetOverrideTransitionVO;
import games.rednblack.editor.renderer.widget.handlers.CoreStateOverrides;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * How an item takes part in the widget it sits in: the role it plays and what the state being shown
 * overrides on it. Overrides are made by editing the item while that state is shown; here they are
 * listed and can be dropped one by one.
 */
public class UIWidgetPartProperties extends UIItemCollapsibleProperties {

    public static final String PREFIX = "games.rednblack.editor.view.ui.properties.panels.UIWidgetPartProperties";
    /** body: key of the override to drop from the state being shown */
    public static final String RESET_OVERRIDE_CLICKED = PREFIX + ".RESET_OVERRIDE_CLICKED";

    public static final String NO_ROLE = "none";
    /** Shown in a dropdown for "nothing chosen", since an empty entry is impossible to click. */
    public static final String NONE = "none";

    private static final int PROPERTY_PAD_TOP = 16;
    private static final int PROPERTY_PAD_BOTTOM = 10;

    private VisSelectBox<String> roleBox;
    private final OrderedMap<String, VisCheckBox> toggleSwitches = new OrderedMap<>();
    private final OrderedMap<String, TransitionFields> transitionFields = new OrderedMap<>();
    private final OrderedMap<String, SequenceFields> sequenceFields = new OrderedMap<>();
    /** Overridden properties as they are listed: alphabetical, visibility left out. */
    private final Array<String> listedKeys = new Array<>();
    private String structure = null;

    public UIWidgetPartProperties() {
        super("Widget Part");
    }

    /** Everything the panel shows about a part, gathered by the mediator. */
    public static class PartData {
        /** roles the widget type declares, empty when it has none */
        public Array<String> roles = new Array<>();
        public String role = "";
        public String state = "";
        public boolean defaultState;
        /** property key -> whether it is on, for the properties that are simply on or off */
        public OrderedMap<String, Boolean> toggles = new OrderedMap<>();
        /** property key -> value overridden by the state being shown */
        public ObjectMap<String, String> overrides = new ObjectMap<>();
        /** property key -> how it gets to that value, for the animated ones */
        public ObjectMap<String, WidgetOverrideTransitionVO> transitions = new ObjectMap<>();
        /** property key -> what plays around that value, for the ones that play */
        public ObjectMap<String, WidgetOverrideSequenceVO> sequences = new ObjectMap<>();
        /** the properties that can be animated at all: numbers and colors */
        public ObjectSet<String> interpolableKeys = new ObjectSet<>();
        /** the properties that can open and close on another animation */
        public ObjectSet<String> sequencedKeys = new ObjectSet<>();
        /** property key -> the values it may take on this item, for the ones that are a known list */
        public ObjectMap<String, Array<String>> choices = new ObjectMap<>();
        /** names of the interpolation functions to choose from */
        public Array<String> functions = new Array<>();
    }

    public void setPart(PartData data) {
        Array<String> roles = data.roles;
        String role = data.role;
        String state = data.state;
        ObjectMap<String, String> overrides = data.overrides;
        ObjectMap<String, WidgetOverrideTransitionVO> transitions = data.transitions;
        // A map keeps no order of its own, so the blocks would sit in a different place every time
        // they are built. Sorted, a property is always where it was left.
        listedKeys.clear();
        for (String key : overrides.keys()) {
            // a property that is simply on or off has a switch under the heading instead
            if (!data.toggles.containsKey(key)) listedKeys.add(key);
        }
        listedKeys.sort();

        // which properties are animated is part of the shape, how (duration, function) is not:
        // typing a duration must not rebuild the field being typed in
        Array<String> animated = transitions.keys().toArray();
        animated.sort();

        StringBuilder shape = new StringBuilder(roles.toString(",")).append('|').append(state)
                .append('|').append(data.toggles.orderedKeys());
        for (String key : listedKeys) shape.append('|').append(key).append('=').append(overrides.get(key));
        String newStructure = shape.append('|').append(animated.toString(",")).toString();

        if (!newStructure.equals(structure)) {
            structure = newStructure;
            rebuild(data);
        }

        if (roleBox != null) roleBox.setSelected(role == null || role.isEmpty() ? NO_ROLE : role);
        for (ObjectMap.Entry<String, VisCheckBox> toggle : toggleSwitches) {
            toggle.value.setChecked(data.toggles.get(toggle.key, true));
        }

        for (ObjectMap.Entry<String, TransitionFields> fields : transitionFields) {
            WidgetOverrideTransitionVO transition = transitions.get(fields.key);
            fields.value.animate.setChecked(transition != null);
            if (transition == null || fields.value.duration == null) continue;

            String duration = String.valueOf(transition.duration);
            if (!duration.equals(fields.value.duration.getText())) fields.value.duration.setText(duration);
            fields.value.function.setSelected(transition.interpolation);
        }

        for (ObjectMap.Entry<String, SequenceFields> fields : sequenceFields) {
            WidgetOverrideSequenceVO sequence = data.sequences.get(fields.key);
            fields.value.enter.setSelected(sequence == null || sequence.enter.isEmpty() ? NONE : sequence.enter);
            fields.value.exit.setSelected(sequence == null || sequence.exit.isEmpty() ? NONE : sequence.exit);
        }
    }

    private void rebuild(PartData data) {
        Array<String> roles = data.roles;
        String state = data.state;
        ObjectMap<String, String> overrides = data.overrides;
        ObjectMap<String, WidgetOverrideTransitionVO> transitions = data.transitions;
        ObjectSet<String> interpolableKeys = data.interpolableKeys;
        Array<String> functions = data.functions;

        mainTable.clearChildren();
        transitionFields.clear();
        sequenceFields.clear();
        // every overridden property is a block of its own here, not a heading inside one panel, so
        // its title gets more room above it and more room before the rows it belongs to
        PropertyGrid grid = PropertyGrid.on(mainTable).sectionPad(PROPERTY_PAD_TOP, PROPERTY_PAD_BOTTOM);

        roleBox = null;
        if (roles.size > 0) {
            roleBox = StandardWidgetsFactory.createSelectBox(String.class);
            Array<String> items = new Array<>(roles);
            items.insert(0, NO_ROLE);
            roleBox.setItems(items);
            roleBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
            grid.row("Role", roleBox);
        }

        grid.section("Overrides in \"" + state + "\"");

        // A property that is simply on or off is a yes or no with nothing to reset, so it gets a
        // switch under the heading rather than a block of its own further down.
        toggleSwitches.clear();
        for (String key : data.toggles.orderedKeys()) {
            VisCheckBox toggle = StandardWidgetsFactory.createSwitch();
            toggle.addListener(new CheckBoxChangeListener(getUpdateEventName()));
            toggleSwitches.put(key, toggle);
            grid.toggle(label(key), toggle);
        }

        if (listedKeys.size == 0) {
            String hint = data.defaultState ? "The default state is the base look" : "Edit the item to override it";
            grid.wide(PropertyGrid.text(hint));
            return;
        }

        for (int i = 0; i < listedKeys.size; i++) {
            final String key = listedKeys.get(i);

            // Every overridden property gets a titled rule of its own, so one no longer runs into
            // the next. Numbers and colors can travel to their value instead of jumping there, and
            // the switch saying so sits on that title line: what is animated is then plain to see,
            // and the fields of the travel follow underneath only when it is switched on.
            TransitionFields fields = null;
            if (interpolableKeys.contains(key)) {
                fields = new TransitionFields();
                fields.animate = StandardWidgetsFactory.createSwitch();
                fields.animate.addListener(new CheckBoxChangeListener(getUpdateEventName()));

                VisTable animate = new VisTable();
                animate.add(PropertyGrid.text("Animate")).padRight(PropertyGrid.LABEL_GAP);
                animate.add(fields.animate);
                grid.section(label(key), animate);

                transitionFields.put(key, fields);
            } else {
                grid.section(label(key));
            }

            VisTextButton reset = StandardWidgetsFactory.createTextButton("Reset");
            reset.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    facade.sendNotification(RESET_OVERRIDE_CLICKED, key);
                }
            });

            VisTable value = new VisTable();
            PropertyGrid.elastic(value.add(PropertyGrid.valueEllipsized(overrides.get(key)))).left();
            value.add(reset).height(PropertyGrid.FIELD_HEIGHT).padLeft(PropertyGrid.BUTTON_GAP);

            // An animation can open and close on another one: the state starts on its entry, keeps
            // playing the value, and goes out on its exit while the next state is already coming in.
            Array<String> choices = data.choices.get(key);
            boolean chained = data.sequencedKeys.contains(key) && choices != null && choices.size > 0;
            if (!chained) {
                grid.row("Value", value);
            } else {
                SequenceFields sequence = new SequenceFields();
                sequence.enter = createChoiceBox(choices);
                sequence.exit = createChoiceBox(choices);
                sequenceFields.put(key, sequence);

                grid.row("Enter", sequence.enter);
                grid.row("Loop", value);
                grid.row("Exit", sequence.exit);
            }

            if (fields == null || !transitions.containsKey(key)) continue;

            fields.duration = StandardWidgetsFactory.createTextField();
            fields.duration.addListener(new KeyboardListener(getUpdateEventName()));

            // the field takes the whole field column, with the unit tucked after it, instead of the
            // fixed narrow box a plain unit row would give it
            VisTable duration = new VisTable();
            duration.add(fields.duration).growX().height(PropertyGrid.FIELD_HEIGHT);
            duration.add(PropertyGrid.text("s")).padLeft(PropertyGrid.LABEL_GAP);
            grid.row("Duration", duration);

            fields.function = StandardWidgetsFactory.createSelectBox(String.class);
            fields.function.setItems(functions);
            fields.function.addListener(new SelectBoxChangeListener(getUpdateEventName()));
            grid.row("Function", fields.function);
        }
    }

    /**
     * @return property key -> transition for every override switched to animated. One that has just
     * been switched on has no fields yet and comes with the defaults.
     */
    public ObjectMap<String, WidgetOverrideTransitionVO> getTransitions() {
        ObjectMap<String, WidgetOverrideTransitionVO> transitions = new ObjectMap<>();
        for (ObjectMap.Entry<String, TransitionFields> fields : transitionFields) {
            if (!fields.value.animate.isChecked()) continue;

            WidgetOverrideTransitionVO transition = new WidgetOverrideTransitionVO();
            if (fields.value.duration != null) {
                transition.duration = Math.max(0, NumberUtils.toFloat(fields.value.duration.getText(), transition.duration));
                if (fields.value.function.getSelected() != null) transition.interpolation = fields.value.function.getSelected();
            }
            transitions.put(fields.key, transition);
        }
        return transitions;
    }

    private static class TransitionFields {
        VisCheckBox animate;
        /** null while the override is not animated */
        VisTextField duration;
        VisSelectBox<String> function;
    }

    /**
     * Turns a property key into something to read: {@code scaleY} becomes {@code Scale Y} and
     * {@code y} becomes {@code Y}. Keys are field names, headings are words.
     */
    private static String label(String key) {
        StringBuilder label = new StringBuilder(key.length() + 4);
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (i == 0) {
                label.append(Character.toUpperCase(c));
                continue;
            }
            char previous = key.charAt(i - 1);
            boolean word = Character.isUpperCase(c) && !Character.isUpperCase(previous);
            boolean number = Character.isDigit(c) && !Character.isDigit(previous);
            if (word || number) label.append(' ');
            label.append(c);
        }
        return label.toString();
    }

    private VisSelectBox<String> createChoiceBox(Array<String> choices) {
        VisSelectBox<String> box = StandardWidgetsFactory.createSelectBox(String.class);
        Array<String> items = new Array<>(choices);
        items.insert(0, NONE);
        box.setItems(items);
        box.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        return box;
    }

    /** @return property key -> what plays around its value, only for the keys that have something */
    public ObjectMap<String, WidgetOverrideSequenceVO> getSequences() {
        ObjectMap<String, WidgetOverrideSequenceVO> sequences = new ObjectMap<>();
        for (ObjectMap.Entry<String, SequenceFields> fields : sequenceFields) {
            WidgetOverrideSequenceVO sequence = new WidgetOverrideSequenceVO(
                    chosen(fields.value.enter), chosen(fields.value.exit));
            if (!sequence.isEmpty()) sequences.put(fields.key, sequence);
        }
        return sequences;
    }

    private static String chosen(VisSelectBox<String> box) {
        String selected = box.getSelected();
        return selected == null || NONE.equals(selected) ? "" : selected;
    }

    private static class SequenceFields {
        VisSelectBox<String> enter;
        VisSelectBox<String> exit;
    }

    /** @return the chosen role, empty for none or when the widget type declares no role */
    public String getRole() {
        if (roleBox == null) return null;
        String selected = roleBox.getSelected();
        return selected == null || NO_ROLE.equals(selected) ? "" : selected;
    }

    /** @return property key -> whether its switch is on */
    public ObjectMap<String, Boolean> getToggles() {
        ObjectMap<String, Boolean> values = new ObjectMap<>();
        for (ObjectMap.Entry<String, VisCheckBox> toggle : toggleSwitches) {
            values.put(toggle.key, toggle.value.isChecked());
        }
        return values;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
