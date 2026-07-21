package games.rednblack.editor.view.ui.properties.panels;

import com.kotcrab.vis.ui.widget.VisCheckBox;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

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

    public UITalosProperties() {
        super("Talos VFX");

        matrixTransformCheckBox = StandardWidgetsFactory.createCheckBox("Matrix Transform");
        autoStartCheckBox = StandardWidgetsFactory.createCheckBox("Auto Start");

        mainTable.add(matrixTransformCheckBox).left().row();
        mainTable.add(autoStartCheckBox).left().row();

        setListeners();
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

    public void setMatrixTransformEnabled(boolean scissorsEnabled) {
        matrixTransformCheckBox.setChecked(scissorsEnabled);
    }

    public void setAutoStartEnabled(boolean autoStart) {
        autoStartCheckBox.setChecked(autoStart);
    }

    private void setListeners() {
        matrixTransformCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        autoStartCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
    }
}
