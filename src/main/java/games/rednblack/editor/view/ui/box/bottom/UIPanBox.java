package games.rednblack.editor.view.ui.box.bottom;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisTextField;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.editor.view.ui.box.UIBaseBox;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UIPanBox extends UIBaseBox {

    private static final String prefix = "games.rednblack.editor.view.ui.box.bottom.UIPanBox";
    public static final String PAN_VALUE_CHANGED = prefix + ".PAN_VALUE_CHANGED";

    private final Skin skin;

    private VisTextField xCoordField;
    private VisTextField yCoordField;

    public UIPanBox() {
        skin = VisUI.getSkin();
        init();
        setVisible(false);
    }

    @Override
    public void update() {
        setVisible(true);
    }

    private void init() {
        addSeparator(true).padRight(13).padLeft(13);
        add("Pan:").padRight(4);

        xCoordField = StandardWidgetsFactory.createTextField("light");
        xCoordField.addListener(new KeyboardListener(PAN_VALUE_CHANGED));
        xCoordField.setAlignment(Align.center);
        add(xCoordField).width(70);

        yCoordField = StandardWidgetsFactory.createTextField("light");
        yCoordField.addListener(new KeyboardListener(PAN_VALUE_CHANGED));
        yCoordField.setAlignment(Align.center);
        add(yCoordField).padLeft(5).width(70);
    }

    public void setCameraCoordinates(float x, float y) {
        xCoordField.setText(String.valueOf(RoundUtils.round(x, 1)));
        yCoordField.setText(String.valueOf(RoundUtils.round(y, 1)));
    }

    public float getCameraX() {
        return Float.parseFloat(xCoordField.getText());
    }

    public float getCameraY() {
        return Float.parseFloat(yCoordField.getText());
    }
}
