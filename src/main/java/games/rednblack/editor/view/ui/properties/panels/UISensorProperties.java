package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
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

    private VisValidatableTextField sensorSpanPercentBottom;
    private VisValidatableTextField sensorSpanPercentLeft;
    private VisValidatableTextField sensorSpanPercentRight;
    private VisValidatableTextField sensorSpanPercentTop;
    
    public UISensorProperties() {
        super("Physics Sensors");

        initView();
        initTooltip();
        initListeners();
    }

    public void initView() {
        // sensors
        sensorBottom = StandardWidgetsFactory.createCheckBox("Bottom");
        sensorLeft = StandardWidgetsFactory.createCheckBox("Left");
        sensorRight = StandardWidgetsFactory.createCheckBox("Right");
        sensorTop = StandardWidgetsFactory.createCheckBox("Top");

        Validators.FloatValidator floatValidator = new Validators.FloatValidator();
        sensorSpanPercentBottom = StandardWidgetsFactory.createValidableTextField(floatValidator);
        sensorSpanPercentLeft = StandardWidgetsFactory.createValidableTextField(floatValidator);
        sensorSpanPercentRight = StandardWidgetsFactory.createValidableTextField(floatValidator);
        sensorSpanPercentTop = StandardWidgetsFactory.createValidableTextField(floatValidator);

        mainTable.add(new VisLabel("Add sensors to body:", Align.left)).padRight(5).colspan(2).fillX();
        mainTable.row().padTop(5);
        
        // table
        VisTable sensorTable = new VisTable();
        sensorTable.defaults().left();
        sensorTable.add(sensorTop).padRight(5);
        sensorTable.add(sensorSpanPercentTop).width(50).padRight(5);
        sensorTable.row();
        sensorTable.add(sensorLeft).padRight(5);
        sensorTable.add(sensorSpanPercentLeft).width(50).padRight(5);
        sensorTable.row();
        sensorTable.add(sensorRight).padRight(5);
        sensorTable.add(sensorSpanPercentRight).width(50).padRight(5);
        sensorTable.row();
        sensorTable.add(sensorBottom).padRight(5);
        sensorTable.add(sensorSpanPercentBottom).width(50).padRight(5);
        
        mainTable.add(sensorTable).padBottom(5).colspan(2);
        mainTable.row().padTop(5);
    }
    
    /**
     * Initializes the tooltips.
     */
    private void initTooltip() {
        StandardWidgetsFactory.addTooltip(sensorBottom, "Adds a sensor to the bottom of the body. The value gives the percentage of the body width where 1.0 equals 100%.");
        StandardWidgetsFactory.addTooltip(sensorLeft, "Adds a sensor to the left of the body. The value gives the percentage of the body height where 1.0 equals 100%.");
        StandardWidgetsFactory.addTooltip(sensorRight, "Adds a sensor to the right of the body. The value gives the percentage of the body height where 1.0 equals 100%.");
        StandardWidgetsFactory.addTooltip(sensorTop, "Adds a sensor to the top of the body. The value gives the percentage of the body width where 1.0 equals 100%.");
    }
    
    private void initListeners() {
        sensorBottom.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        sensorLeft.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        sensorRight.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        sensorTop.addListener(new CheckBoxChangeListener(getUpdateEventName()));

        sensorSpanPercentBottom.addListener(new KeyboardListener(getUpdateEventName()));
        sensorSpanPercentLeft.addListener(new KeyboardListener(getUpdateEventName()));
        sensorSpanPercentRight.addListener(new KeyboardListener(getUpdateEventName()));
        sensorSpanPercentTop.addListener(new KeyboardListener(getUpdateEventName()));
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
    
    public VisTextField getSensorSpanPercentBottomTextfield() {
    	return sensorSpanPercentBottom;
    }
    
    public VisTextField getSensorSpanPercentLeftTextfield() {
    	return sensorSpanPercentLeft;
    }
    
    public VisTextField getSensorSpanPercentRightTextfield() {
    	return sensorSpanPercentRight;
    }
    
    public VisTextField getSensorSpanPercentTopTextfield() {
    	return sensorSpanPercentTop;
    }

	@Override
	public void onRemove() {
        HyperLap2DFacade.getInstance().sendNotification(CLOSE_CLICKED);
	}
    
}
