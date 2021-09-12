package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.spinner.Spinner;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.event.NumberSelectorOverlapListener;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UICircleShapeProperties extends UIRemovableProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UICircleShapeProperties";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";

    private Spinner radiusSpinner;

    public UICircleShapeProperties() {
        super("Circle Shape");
        radiusSpinner = StandardWidgetsFactory.createNumberSelector("default", 1.0f, 0.1f, 1000f, 0.1f);

        mainTable.add(new VisLabel("Radius:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(radiusSpinner).left().colspan(2);
        mainTable.row().padTop(5);

        addListeners();
    }

    public void setRadius(float radius) {
        radiusSpinner.getTextField().setText(String.valueOf(radius));
    }

    public String getRadius() {
        return radiusSpinner.getTextField().getText();
    }

    @Override
    public void onRemove() {
        HyperLap2DFacade.getInstance().sendNotification(CLOSE_CLICKED);
    }

    private void addListeners() {
        radiusSpinner.addListener(new NumberSelectorOverlapListener(getUpdateEventName()));
    }
}
