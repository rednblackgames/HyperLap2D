package games.rednblack.editor.view.ui.properties.panels;

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
import games.rednblack.h2d.extension.talos.TalosComponent;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import org.apache.commons.lang3.math.NumberUtils;

public class UITalosProperties extends UIItemCollapsibleProperties implements RemoteEditablePanel {

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

    private final VisCheckBox matrixTransformCheckBox, autoStartCheckBox;
    private final OrderedMap<Integer, VisTextField[]> scopeFields = new OrderedMap<>();
    /** Slots the rows were built for, so typing in one does not rebuild the field being typed in. */
    private Array<Integer> shownScopeKeys = new Array<>();
    private static final int CHANNELS = TalosComponent.ScopeValue.CHANNELS;
    private static final int SLOT_GAP = 12;

    public UITalosProperties() {
        super("Talos VFX");

        matrixTransformCheckBox = StandardWidgetsFactory.createSwitch();
        autoStartCheckBox = StandardWidgetsFactory.createSwitch();
        matrixTransformCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        autoStartCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));

        rebuild();
    }

    /**
     * One row per global scope slot the effect reads: whatever the artist wired a global scope
     * module to inside it. What a slot holds drives the effect, so a scene, and a widget state, can
     * reach into it. Slots an anchor constraint drives are not passed here, the layout owns those.
     *
     * @param values slot number -> what it holds
     */
    public void setScopeSlots(Array<Integer> keys, ObjectMap<Integer, float[]> values) {
        if (!keys.equals(shownScopeKeys)) {
            shownScopeKeys = new Array<>(keys);
            rebuild();
        }

        for (ObjectMap.Entry<Integer, VisTextField[]> slot : scopeFields) {
            float[] value = values.get(slot.key);
            for (int i = 0; i < slot.value.length; i++) {
                String number = String.valueOf(value == null ? 0f : value[i]);
                if (!number.equals(slot.value[i].getText())) slot.value[i].setText(number);
            }
        }
    }

    private void rebuild() {
        mainTable.clearChildren();
        scopeFields.clear();

        PropertyGrid grid = PropertyGrid.on(mainTable);
        grid.toggle("Matrix transform", matrixTransformCheckBox);
        grid.toggle("Auto start", autoStartCheckBox);

        if (shownScopeKeys.size == 0) return;

        grid.section("Global scopes");
        boolean firstSlot = true;
        for (int key : shownScopeKeys) {
            // a slot is two rows, so one needs a little air to read apart from the next
            if (!firstSlot) grid.gap(SLOT_GAP);
            firstSlot = false;

            // a slot holds a numerical value: one number on its own, two for a position, four for a
            // colour, so all four are there and what the effect makes of them is its business
            VisTextField[] slot = new VisTextField[CHANNELS];
            for (int i = 0; i < CHANNELS; i++) {
                slot[i] = StandardWidgetsFactory.createTextField();
                slot[i].addListener(new KeyboardListener(getUpdateEventName()));
            }
            scopeFields.put(key, slot);

            grid.pair("Slot " + key, "X", slot[0], "Y", slot[1]);
            grid.pair("", "Z", slot[2], "W", slot[3]);
        }
    }

    /** @return slot number -> the four numbers entered for it */
    public ObjectMap<Integer, float[]> getScopeValues() {
        ObjectMap<Integer, float[]> values = new ObjectMap<>();
        for (ObjectMap.Entry<Integer, VisTextField[]> slot : scopeFields) {
            float[] value = new float[CHANNELS];
            for (int i = 0; i < CHANNELS; i++) value[i] = NumberUtils.toFloat(slot.value[i].getText(), 0f);
            values.put(slot.key, value);
        }
        return values;
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
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
