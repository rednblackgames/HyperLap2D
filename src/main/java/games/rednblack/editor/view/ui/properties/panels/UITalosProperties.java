package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.TintButton;
import games.rednblack.h2d.extension.talos.TalosComponent;
import org.apache.commons.lang3.math.NumberUtils;

public class UITalosProperties extends UIItemCollapsibleProperties implements RemoteEditablePanel {

    private static final String PREFIX = "games.rednblack.editor.view.ui.properties.panels.UITalosProperties";
    /** body: the slot number whose colour swatch was clicked */
    public static final String SCOPE_COLOR_CLICKED = PREFIX + ".SCOPE_COLOR_CLICKED";

    @Override
    public void setFieldValue(String key, Object value) {
        if (value == null) throw new IllegalArgumentException("null value for field: " + key);
        switch (key) {
            case "matrixTransform": setMatrixTransformEnabled(RemoteEditableSupport.toBool(value)); break;
            case "autoStart": setAutoStartEnabled(RemoteEditableSupport.toBool(value)); break;
            default: throw new IllegalArgumentException("Unknown field: " + key + " (supported: matrixTransform, autoStart)");
        }
    }
    @Override public java.util.List<String> validateFieldValues() { return new java.util.ArrayList<>(); }

    private static final int CHANNELS = TalosComponent.ScopeValue.CHANNELS;
    private static final int SLOT_GAP = 12;
    private static final String[] AXES = {"X", "Y", "Z", "W"};

    private final VisCheckBox matrixTransformCheckBox, autoStartCheckBox;
    private final OrderedMap<Integer, SlotFields> slots = new OrderedMap<>();
    /** What each slot held when last shown, so the numbers a slot does not show are kept as they are. */
    private final ObjectMap<Integer, float[]> shownValues = new ObjectMap<>();
    /** Slots and shapes the rows were built for, so typing in one does not rebuild the field being typed in. */
    private String shownShape = "";

    /** The widgets of one slot, as many as its shape needs. */
    private static class SlotFields {
        TalosComponent.ScopeKind kind;
        /** one field per number the slot shows, none for a colour */
        VisTextField[] fields;
        /** the swatch of a colour slot */
        TintButton color;
    }

    public UITalosProperties() {
        super("Talos VFX");

        matrixTransformCheckBox = StandardWidgetsFactory.createSwitch();
        autoStartCheckBox = StandardWidgetsFactory.createSwitch();
        matrixTransformCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        autoStartCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));

        rebuild(new Array<Integer>(), new ObjectMap<Integer, TalosComponent.ScopeKind>());
    }

    /**
     * One entry per global scope slot the effect reads, shaped after what the effect reads it as:
     * a single field for a number, two for a vector, a colour picker for a colour, and all four when
     * that cannot be told. Slots an anchor constraint drives are not passed here, the layout owns them.
     *
     * @param values slot number -> the four numbers it holds
     */
    public void setScopeSlots(Array<Integer> keys, ObjectMap<Integer, TalosComponent.ScopeKind> kinds,
                              ObjectMap<Integer, float[]> values) {
        StringBuilder shape = new StringBuilder();
        for (int key : keys) shape.append(key).append(':').append(kinds.get(key)).append(' ');
        if (!shape.toString().equals(shownShape)) {
            shownShape = shape.toString();
            rebuild(keys, kinds);
        }

        shownValues.clear();
        for (ObjectMap.Entry<Integer, SlotFields> slot : slots) {
            float[] value = values.get(slot.key);
            if (value == null) value = new float[CHANNELS];
            shownValues.put(slot.key, value.clone());

            if (slot.value.color != null) {
                slot.value.color.setColorValue(new Color(value[0], value[1], value[2], value[3]));
                continue;
            }
            for (int i = 0; i < slot.value.fields.length; i++) {
                String number = String.valueOf(value[i]);
                if (!number.equals(slot.value.fields[i].getText())) slot.value.fields[i].setText(number);
            }
        }
    }

    private void rebuild(Array<Integer> keys, ObjectMap<Integer, TalosComponent.ScopeKind> kinds) {
        mainTable.clearChildren();
        slots.clear();

        PropertyGrid grid = PropertyGrid.on(mainTable);
        grid.toggle("Matrix transform", matrixTransformCheckBox);
        grid.toggle("Auto start", autoStartCheckBox);

        if (keys.size == 0) return;

        grid.section("Global scopes");
        boolean firstSlot = true;
        for (final int key : keys) {
            // a slot is a block of rows, so it needs a little air to read apart from the next
            if (!firstSlot) grid.gap(SLOT_GAP);
            firstSlot = false;

            SlotFields slot = new SlotFields();
            slot.kind = kinds.get(key, TalosComponent.ScopeKind.UNKNOWN);
            slots.put(key, slot);
            String label = "Slot " + key;

            if (slot.kind == TalosComponent.ScopeKind.COLOR) {
                slot.color = StandardWidgetsFactory.createTintButton();
                slot.color.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        facade.sendNotification(SCOPE_COLOR_CLICKED, key);
                    }
                });
                grid.row(label, slot.color);
                continue;
            }

            slot.fields = new VisTextField[slot.kind.channels];
            for (int i = 0; i < slot.fields.length; i++) {
                slot.fields[i] = StandardWidgetsFactory.createTextField();
                slot.fields[i].addListener(new KeyboardListener(getUpdateEventName()));
            }

            if (slot.kind == TalosComponent.ScopeKind.NUMBER) {
                grid.row(label, slot.fields[0]);
            } else {
                for (int i = 0; i < slot.fields.length; i += 2) {
                    grid.pair(i == 0 ? label : "", AXES[i], slot.fields[i], AXES[i + 1], slot.fields[i + 1]);
                }
            }
        }
    }

    /** Shows the colour a slot now holds, once it has been picked. */
    public void setScopeColor(int key, Color color) {
        SlotFields slot = slots.get(key);
        if (slot != null && slot.color != null) slot.color.setColorValue(color);
    }

    /** @return the four numbers a slot holds as the panel shows them now */
    public float[] getScopeValue(int key) {
        float[] shown = shownValues.get(key);
        return shown == null ? new float[CHANNELS] : shown.clone();
    }

    /**
     * @return slot number -> its four numbers, with what the panel shows in place of what it held:
     * the numbers a slot does not show keep their value.
     */
    public ObjectMap<Integer, float[]> getScopeValues() {
        ObjectMap<Integer, float[]> values = new ObjectMap<>();
        for (ObjectMap.Entry<Integer, SlotFields> slot : slots) {
            float[] value = getScopeValue(slot.key);
            if (slot.value.color != null) {
                Color color = slot.value.color.getColorValue();
                value[0] = color.r;
                value[1] = color.g;
                value[2] = color.b;
                value[3] = color.a;
            } else {
                for (int i = 0; i < slot.value.fields.length; i++) {
                    value[i] = NumberUtils.toFloat(slot.value.fields[i].getText(), value[i]);
                }
            }
            values.put(slot.key, value);
        }
        return values;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    public boolean isMatrixTransformEnabled() {
        return matrixTransformCheckBox.isChecked();
    }

    public boolean isAutoStartEnabled() {
        return autoStartCheckBox.isChecked();
    }

    public void setMatrixTransformEnabled(boolean matrixTransform) {
        matrixTransformCheckBox.setChecked(matrixTransform);
    }

    public void setAutoStartEnabled(boolean autoStart) {
        autoStartCheckBox.setChecked(autoStart);
    }
}
