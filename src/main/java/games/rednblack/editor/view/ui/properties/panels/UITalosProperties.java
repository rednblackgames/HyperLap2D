package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UITalosProperties extends UIItemCollapsibleProperties {
    private VisCheckBox matrixTransformCheckBox;

    public UITalosProperties() {
        super("Talos VFX");

        matrixTransformCheckBox = StandardWidgetsFactory.createCheckBox();

        mainTable.add(StandardWidgetsFactory.createLabel("Matrix Transform", Align.right)).padRight(5).width(120).right();
        mainTable.add(matrixTransformCheckBox).left().row();

        setListeners();
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }

    public boolean isMatrixTransformEnabled() {
        return matrixTransformCheckBox.isChecked();
    }

    public void setMatrixTransformEnabled(boolean scissorsEnabled) {
        matrixTransformCheckBox.setChecked(scissorsEnabled);
    }

    private void setListeners() {
        matrixTransformCheckBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
    }
}
