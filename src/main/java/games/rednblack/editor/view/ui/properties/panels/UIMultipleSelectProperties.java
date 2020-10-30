package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.view.ui.properties.UIAbstractProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UIMultipleSelectProperties extends UIAbstractProperties {
    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UIMultipleSelectProperties";

    public VisLabel selectionCount;
    public UIMultipleSelectProperties() {
        pad(5);
        selectionCount = StandardWidgetsFactory.createLabel("0", "default", Align.right);
        add("Multiple Selection (");
        add(selectionCount);
        add(")").row();
    }

    public void setSelectionCount(int count) {
        selectionCount.setText(count);
    }

    @Override
    public String getPrefix() {
        return this.getClass().getCanonicalName();
    }
}
