package games.rednblack.editor.view.ui.properties.panels;

import com.kotcrab.vis.ui.widget.VisSelectBox;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.view.ui.properties.RemoteEditablePanel;
import games.rednblack.editor.view.ui.properties.RemoteEditableSupport;
import games.rednblack.editor.view.ui.properties.UIItemCollapsibleProperties;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * Created by azakhary on 8/2/2015.
 */
public class UIImageItemProperties extends UIItemCollapsibleProperties implements RemoteEditablePanel {

    @Override
    public void setFieldValue(String key, Object value) {
        if (value == null) throw new IllegalArgumentException("null value for field: " + key);
        switch (key) {
            case "renderMode":
                if (!RemoteEditableSupport.contains(renderModeBox, value.toString())) throw new IllegalArgumentException("renderMode '" + value + "' not valid; allowed: REPEAT, SINGLE");
                setRenderMode(value.toString()); break;
            case "spriteType":
                if (!RemoteEditableSupport.contains(spriteTypeBox, value.toString())) throw new IllegalArgumentException("spriteType '" + value + "' not valid; allowed: SQUARE, POLYGON");
                setSpriteType(value.toString()); break;
            default: throw new IllegalArgumentException("Unknown field: " + key + " (supported: renderMode, spriteType)");
        }
    }

    @Override
    public java.util.List<String> validateFieldValues() { return new java.util.ArrayList<>(); }

    final private VisSelectBox<String> renderModeBox;
    final private VisSelectBox<String> spriteTypeBox;

    public UIImageItemProperties() {
        super("Render Properties");

        renderModeBox = StandardWidgetsFactory.createSelectBox(String.class);
        spriteTypeBox = StandardWidgetsFactory.createSelectBox(String.class);

        renderModeBox.setItems("REPEAT", "SINGLE");
        spriteTypeBox.setItems("SQUARE", "POLYGON");

        PropertyGrid grid = PropertyGrid.on(mainTable);
        grid.row("Render mode", renderModeBox);
        grid.row("Sprite type", spriteTypeBox);

        collapse(header);

        renderModeBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        spriteTypeBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
    }

    public void setRenderMode(String mode) {
        renderModeBox.setSelected(mode);
    }

    public String getRenderMode() {
        return renderModeBox.getSelected().toString();
    }

    public void setSpriteType(String mode) {
        spriteTypeBox.setSelected(mode);
    }

    public String getSpriteType() {
        return spriteTypeBox.getSelected().toString();
    }
}
