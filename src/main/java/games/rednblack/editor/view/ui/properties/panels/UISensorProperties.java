package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.widget.VisCheckBox;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

/**
 * The properties view for the sensors.
 * 
 * @author Jan-Thierry Wegener
 */
public class UISensorProperties extends UIRemovableProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UISensorProperties";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";

    private VisCheckBox sensorBottom;
    private VisCheckBox sensorLeft;
    private VisCheckBox sensorRight;
    private VisCheckBox sensorTop;
    
    public UISensorProperties() {
        super("Sensors");

        initView();
        initListeners();
    }

    public void initView() {
        // sensors
        sensorBottom = StandardWidgetsFactory.createCheckBox("Bottom");
        sensorLeft = StandardWidgetsFactory.createCheckBox("Left");
        sensorRight = StandardWidgetsFactory.createCheckBox("Right");
        sensorTop = StandardWidgetsFactory.createCheckBox("Top");

        mainTable.add(new VisLabel("Add sensors to body:", Align.right)).padRight(5).colspan(3).fillX();
        mainTable.row().padTop(5);
        
        // table
        VisTable sensorTable = new VisTable();
        sensorTable.add(sensorTop).padRight(5).colspan(3).fillX();
        sensorTable.row();
        sensorTable.add(sensorLeft).padRight(5);
        sensorTable.add(new VisLabel(""));
        sensorTable.add(sensorRight).padRight(5);
        sensorTable.row();
        sensorTable.add(sensorBottom).padRight(5).colspan(3).fillX();
        
        mainTable.add(sensorTable).padBottom(5).colspan(4);
        mainTable.row().padTop(5);
    }
    
    private void initListeners() {
        sensorBottom.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        sensorLeft.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        sensorRight.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        sensorTop.addListener(new CheckBoxChangeListener(getUpdateEventName()));
    }

    public VisCheckBox getSensorBottomBox() {
    	return sensorBottom;
    }
    
    public VisCheckBox getSensorLeftBox() {
    	return sensorLeft;
    }
    
    public VisCheckBox getSensorRightBox() {
    	return sensorRight;
    }
    
    public VisCheckBox getSensorTopBox() {
    	return sensorTop;
    }

	@Override
	public void onRemove() {
        HyperLap2DFacade.getInstance().sendNotification(CLOSE_CLICKED);
	}
    
}
