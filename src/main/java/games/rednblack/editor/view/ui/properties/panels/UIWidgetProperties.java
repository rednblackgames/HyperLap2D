package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.renderer.widget.WidgetType;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * What a widget is: its type, the settings that type declares and which of its parts are in place.
 * Built out of the {@link WidgetType} descriptor, so a new widget type needs no panel of its own.
 */
public class UIWidgetProperties extends UIItemCollapsibleProperties {

    public static final String PREFIX = "games.rednblack.editor.view.ui.properties.panels.UIWidgetProperties";

    private static final float CHIP_GAP = 4;
    private static final String CHIP_BACKGROUND = "toolbar-over";
    private static final String CHIP_BACKGROUND_LIT = "toolbar-down";

    private final OrderedMap<String, Actor> settingFields = new OrderedMap<>();
    private final OrderedMap<String, VisTable> stateChips = new OrderedMap<>();
    private CurrentStateProvider currentStateProvider;
    private String shownState = null;
    private String structure = null;

    public UIWidgetProperties() {
        super("Widget");
    }

    /**
     * @param type          descriptor of the widget type, null for a plain state holder
     * @param assignedParts role -> name of the item filling it
     */
    public void setWidget(WidgetType type, String typeName, Array<String> states,
                          ObjectMap<String, String> settings, ObjectMap<String, String> assignedParts) {
        // Rebuilt only when what is shown changes shape, so typing in a field is never interrupted
        // by an unrelated update.
        String newStructure = typeName + "|" + states.toString(",") + "|" + assignedParts.toString();
        if (!newStructure.equals(structure)) {
            structure = newStructure;
            rebuild(type, typeName, states, assignedParts);
        }

        for (ObjectMap.Entry<String, Actor> field : settingFields) {
            String value = settings.get(field.key);
            if (field.value instanceof VisCheckBox) {
                ((VisCheckBox) field.value).setChecked(Boolean.parseBoolean(value));
            } else {
                ((VisTextField) field.value).setText(value == null ? "" : value);
            }
        }
    }

    private void rebuild(WidgetType type, String typeName, Array<String> states, ObjectMap<String, String> assignedParts) {
        mainTable.clearChildren();
        settingFields.clear();

        PropertyGrid grid = PropertyGrid.on(mainTable);
        // Values of unbounded length go in rows sized by the grid: a compact row takes the width of
        // its content and would push the whole properties box wider than its panel.
        grid.row("Type", PropertyGrid.valueEllipsized(typeName == null || typeName.isEmpty() ? "custom" : typeName));

        grid.section("States");
        // a row that fills the width it is given and asks for none, so it can never widen the panel
        grid.wideFill(createStateChips(states));

        if (type == null) return;

        if (type.properties.size > 0) {
            grid.section("Settings");
            for (WidgetType.Property property : type.properties) {
                if (property.kind == WidgetType.PropertyKind.BOOLEAN) {
                    VisCheckBox toggle = StandardWidgetsFactory.createSwitch();
                    toggle.addListener(new CheckBoxChangeListener(getUpdateEventName()));
                    settingFields.put(property.key, toggle);
                    grid.toggle(property.key, toggle);
                } else {
                    VisTextField field = StandardWidgetsFactory.createTextField();
                    field.addListener(new KeyboardListener(getUpdateEventName()));
                    settingFields.put(property.key, field);
                    grid.row(property.key, field);
                }
            }
        }

        if (type.parts.size > 0) {
            grid.section("Parts");
            for (WidgetType.Part part : type.parts) {
                String assigned = assignedParts.get(part.role);
                String status = assigned != null ? assigned : part.required ? "missing" : "not set";
                grid.row(part.role, PropertyGrid.valueEllipsized(status));
            }
        }
    }

    /**
     * The states as chips flowing over as many lines as they need, the one being shown lit up.
     * Lines are broken here rather than by a wrapping group: the width of the properties box is
     * fixed, and this way the height is right from the first layout pass.
     */
    private VisTable createStateChips(Array<String> states) {
        stateChips.clear();
        VisTable flow = new VisTable();
        VisTable line = new VisTable();
        float lineWidth = 0;
        float maxWidth = PropertyGrid.GRID_WIDTH - 2 * PropertyGrid.CONTENT_PAD;

        for (String state : states) {
            VisTable chip = new VisTable();
            // Measured with a background on: a background adds its borders to the width of the chip,
            // and one set only later would make every chip wider than the line it was fitted into.
            // The lit one differs only in colour, so swapping them later keeps the width.
            chip.setBackground(VisUI.getSkin().getDrawable(CHIP_BACKGROUND));
            chip.add(PropertyGrid.text(state)).pad(2, 7, 3, 7);
            chip.pack();
            stateChips.put(state, chip);

            if (lineWidth > 0 && lineWidth + CHIP_GAP + chip.getPrefWidth() > maxWidth) {
                flow.add(line).left().padBottom(CHIP_GAP).row();
                line = new VisTable();
                lineWidth = 0;
            }
            line.add(chip).padRight(CHIP_GAP);
            lineWidth += chip.getPrefWidth() + CHIP_GAP;
        }
        flow.add(line).left();

        shownState = null;
        return flow;
    }

    /** Asked on every frame, so the chips also follow the states the mouse triggers in the sandbox. */
    public void setCurrentStateProvider(CurrentStateProvider provider) {
        currentStateProvider = provider;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (currentStateProvider == null) return;

        String state = currentStateProvider.getCurrentState();
        if (state == null || state.equals(shownState)) return;
        shownState = state;

        Skin skin = VisUI.getSkin();
        for (ObjectMap.Entry<String, VisTable> chip : stateChips) {
            chip.value.setBackground(skin.getDrawable(chip.key.equals(state) ? CHIP_BACKGROUND_LIT : CHIP_BACKGROUND));
        }
    }

    public interface CurrentStateProvider {
        /** @return the state the widget shows right now, null if it is gone */
        String getCurrentState();
    }

    /** @return setting key -> value as currently entered */
    public ObjectMap<String, String> getSettings() {
        ObjectMap<String, String> values = new ObjectMap<>();
        for (ObjectMap.Entry<String, Actor> field : settingFields) {
            if (field.value instanceof VisCheckBox) {
                values.put(field.key, Boolean.toString(((VisCheckBox) field.value).isChecked()));
            } else {
                values.put(field.key, ((VisTextField) field.value).getText());
            }
        }
        return values;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
