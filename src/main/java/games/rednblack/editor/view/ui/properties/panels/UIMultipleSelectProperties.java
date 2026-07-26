package games.rednblack.editor.view.ui.properties.panels;

import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.view.ui.properties.UIAbstractProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;

public class UIMultipleSelectProperties extends UIAbstractProperties {
    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UIMultipleSelectProperties";

    public VisLabel selectionCount;
    public UIMultipleSelectProperties() {
        selectionCount = PropertyGrid.value("0");
        PropertyGrid.on(this).padPanel().rowCompact("Selected items", selectionCount);
    }

    public void setSelectionCount(int count) {
        selectionCount.setText(count);
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }
}
