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

    private static final int PROPERTY_PAD_TOP = 16;
    private static final int PROPERTY_PAD_BOTTOM = 10;

    private VisSelectBox<String> roleBox;
    private VisCheckBox visibleSwitch;
    private final OrderedMap<String, TransitionFields> transitionFields = new OrderedMap<>();
    /** Overridden properties as they are listed: alphabetical, visibility left out. */
    private final Array<String> listedKeys = new Array<>();
    private String structure = null;

    public UIWidgetPartProperties() {
        super("Widget Part");
    }

    /**
     * @param roles            roles the widget type declares, empty when it has none
     * @param overrides        property key -> value overridden by the state being shown
     * @param transitions      property key -> how it gets to that value, for the animated ones
     * @param interpolableKeys the properties that can be animated at all: numbers and colors
     * @param functions        names of the interpolation functions to choose from
     */
    public void setPart(Array<String> roles, String role, String state, boolean defaultState, boolean visible,
                        ObjectMap<String, String> overrides, ObjectMap<String, WidgetOverrideTransitionVO> transitions,
                        ObjectSet<String> interpolableKeys, Array<String> functions) {
        // A map keeps no order of its own, so the blocks would sit in a different place every time
        // they are built. Sorted, a property is always where it was left.
        listedKeys.clear();
        for (String key : overrides.keys()) {
            // visibility has its own switch under the heading
            if (!CoreStateOverrides.VISIBLE.equals(key)) listedKeys.add(key);
        }
        listedKeys.sort();

        // which properties are animated is part of the shape, how (duration, function) is not:
        // typing a duration must not rebuild the field being typed in
        Array<String> animated = transitions.keys().toArray();
        animated.sort();

        StringBuilder shape = new StringBuilder(roles.toString(",")).append('|').append(state);
        for (String key : listedKeys) shape.append('|').append(key).append('=').append(overrides.get(key));
        String newStructure = shape.append('|').append(animated.toString(",")).toString();

        if (!newStructure.equals(structure)) {
            structure = newStructure;
            rebuild(roles, state, defaultState, overrides, transitions, interpolableKeys, functions);
        }

        if (roleBox != null) roleBox.setSelected(role == null || role.isEmpty() ? NO_ROLE : role);
        visibleSwitch.setChecked(visible);

        for (ObjectMap.Entry<String, TransitionFields> fields : transitionFields) {
            WidgetOverrideTransitionVO transition = transitions.get(fields.key);
            fields.value.animate.setChecked(transition != null);
            if (transition == null || fields.value.duration == null) continue;

            String duration = String.valueOf(transition.duration);
            if (!duration.equals(fields.value.duration.getText())) fields.value.duration.setText(duration);
            fields.value.function.setSelected(transition.interpolation);
        }
    }

    private void rebuild(Array<String> roles, String state, boolean defaultState, ObjectMap<String, String> overrides,
                         ObjectMap<String, WidgetOverrideTransitionVO> transitions, ObjectSet<String> interpolableKeys,
                         Array<String> functions) {
        mainTable.clearChildren();
        transitionFields.clear();
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

        // Visibility is an override like the others, but it is a yes or no with nothing to reset, so
        // it gets a switch under the heading instead of a block of its own further down.
        visibleSwitch = StandardWidgetsFactory.createSwitch();
        visibleSwitch.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        grid.toggle("Visible", visibleSwitch);

        if (listedKeys.size == 0) {
            String hint = defaultState ? "The default state is the base look" : "Edit the item to override it";
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
            grid.row("Value", value);

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
            if (Character.isUpperCase(c) && !Character.isUpperCase(key.charAt(i - 1))) label.append(' ');
            label.append(c);
        }
        return label.toString();
    }

    /** @return the chosen role, empty for none or when the widget type declares no role */
    public String getRole() {
        if (roleBox == null) return null;
        String selected = roleBox.getSelected();
        return selected == null || NO_ROLE.equals(selected) ? "" : selected;
    }

    public boolean isVisibleInState() {
        return visibleSwitch.isChecked();
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
