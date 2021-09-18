package games.rednblack.editor.view.ui.properties.panels;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.event.CheckBoxChangeListener;
import games.rednblack.editor.event.KeyboardListener;
import games.rednblack.editor.event.SelectBoxChangeListener;
import games.rednblack.editor.renderer.data.PhysicsBodyDataVO;
import games.rednblack.editor.view.ui.properties.UIRemovableProperties;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

import java.util.HashMap;

/**
 * Created by CyberJoe on 7/5/2015.
 */
public class UIPhysicsProperties extends UIRemovableProperties {

    public static final String prefix = "games.rednblack.editor.view.ui.properties.panels.UIPhysicsProperties";
    public static final String CLOSE_CLICKED = prefix + ".CLOSE_CLICKED";

    private final HashMap<Integer, String> bodyTypes = new HashMap<>();

    private VisSelectBox<String> bodyTypeBox, shapeType;
    private VisValidatableTextField massField;
    private VisValidatableTextField centerOfMassXField;
    private VisValidatableTextField centerOfMassYField;
    private VisValidatableTextField rotationalInertiaField;
    private VisValidatableTextField dumpingField;
    private VisValidatableTextField angularDumpingField;
    private VisValidatableTextField gravityScaleField;
    private VisValidatableTextField densityField;
    private VisValidatableTextField frictionField;
    private VisValidatableTextField restitutionField;
    private VisValidatableTextField heightField;
    private VisCheckBox allowSleepBox;
    private VisCheckBox awakeBox;
    private VisCheckBox bulletBox;
    private VisCheckBox sensor;
    private VisCheckBox fixedRotation;

    public UIPhysicsProperties() {
        super("Physics");

        bodyTypes.put(0, "STATIC");
        bodyTypes.put(1, "KINEMATIC");
        bodyTypes.put(2, "DYNAMIC");

        initView();
        initListeners();
    }

    private void initView() {
        bodyTypeBox = StandardWidgetsFactory.createSelectBox(String.class);
        Array<String> types = new Array<>();
        bodyTypes.values().forEach(types::add);
        bodyTypeBox.setItems(types);

        shapeType = StandardWidgetsFactory.createSelectBox(String.class);
        types.clear();
        for (PhysicsBodyDataVO.ShapeType type : PhysicsBodyDataVO.ShapeType.values())
            types.add(type.toString());
        shapeType.setItems(types);

        Validators.FloatValidator floatValidator = new Validators.FloatValidator();

        massField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        centerOfMassXField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        centerOfMassYField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        rotationalInertiaField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        dumpingField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        angularDumpingField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        gravityScaleField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        densityField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        frictionField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        restitutionField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        heightField = StandardWidgetsFactory.createValidableTextField(floatValidator);
        allowSleepBox = StandardWidgetsFactory.createCheckBox("Allow Sleep");
        awakeBox = StandardWidgetsFactory.createCheckBox("Awake");
        bulletBox = StandardWidgetsFactory.createCheckBox("Bullet");
        sensor = StandardWidgetsFactory.createCheckBox("Sensor");
        fixedRotation = StandardWidgetsFactory.createCheckBox("Fixed Rotation");

        mainTable.add(new VisLabel("Body Type:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(bodyTypeBox).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Shape Type:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(shapeType).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Mass:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(massField).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Center of Mass:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(centerOfMassXField).width(50);
        mainTable.add(centerOfMassYField).width(50);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Rotational Inertia:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(rotationalInertiaField).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Dumping:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(dumpingField).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Angular Dumping:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(angularDumpingField).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Gravity Scale:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(gravityScaleField).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Density:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(densityField).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Friction:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(frictionField).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Restitution:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(restitutionField).width(100).colspan(2);
        mainTable.row().padTop(5);

        mainTable.add(new VisLabel("Height:", Align.right)).padRight(5).colspan(2).fillX();
        mainTable.add(heightField).width(100).colspan(2);
        mainTable.row().padTop(5);

        VisTable bottomTable = new VisTable();
        bottomTable.add(allowSleepBox).padRight(5);
        bottomTable.add(awakeBox).padRight(5);
        bottomTable.add(bulletBox).padRight(5);

        bottomTable.row();

        bottomTable.add(sensor).padRight(5);
        bottomTable.add(fixedRotation).colspan(2).left();

        mainTable.add(bottomTable).padBottom(5).colspan(4);
        mainTable.row().padTop(5);
    }

    private void updateEnabled() {
        massField.setDisabled(!bodyTypeBox.getSelected().equals(bodyTypes.get(2)));
        centerOfMassXField.setDisabled(!bodyTypeBox.getSelected().equals(bodyTypes.get(2)));
        centerOfMassYField.setDisabled(!bodyTypeBox.getSelected().equals(bodyTypes.get(2)));
        rotationalInertiaField.setDisabled(!bodyTypeBox.getSelected().equals(bodyTypes.get(2)));
    }

    private void initListeners() {
        bodyTypeBox.addListener(new SelectBoxChangeListener(getUpdateEventName()));
        bodyTypeBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateEnabled();
            }
        });
        shapeType.addListener(new SelectBoxChangeListener(getUpdateEventName()));

        massField.addListener(new KeyboardListener(getUpdateEventName()));
        centerOfMassXField.addListener(new KeyboardListener(getUpdateEventName()));
        centerOfMassYField.addListener(new KeyboardListener(getUpdateEventName()));
        rotationalInertiaField.addListener(new KeyboardListener(getUpdateEventName()));
        dumpingField.addListener(new KeyboardListener(getUpdateEventName()));
        angularDumpingField.addListener(new KeyboardListener(getUpdateEventName()));
        gravityScaleField.addListener(new KeyboardListener(getUpdateEventName()));
        densityField.addListener(new KeyboardListener(getUpdateEventName()));
        frictionField.addListener(new KeyboardListener(getUpdateEventName()));
        restitutionField.addListener(new KeyboardListener(getUpdateEventName()));
        heightField.addListener(new KeyboardListener(getUpdateEventName()));

        allowSleepBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        awakeBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        bulletBox.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        sensor.addListener(new CheckBoxChangeListener(getUpdateEventName()));
        fixedRotation.addListener(new CheckBoxChangeListener(getUpdateEventName()));
    }

    public int getBodyType() {
        for(Integer key: bodyTypes.keySet()) {
            if(bodyTypes.get(key).equals(bodyTypeBox.getSelected())) {
                return key;
            }
        }

        return 0;
    }

    public void setBodyType(int type) {
        bodyTypeBox.setSelected(bodyTypes.get(type));
        updateEnabled();
    }

    public void setShapeType(PhysicsBodyDataVO.ShapeType type) {
        shapeType.setSelected(type.toString());
    }

    public PhysicsBodyDataVO.ShapeType getShapeType() {
        return PhysicsBodyDataVO.ShapeType.valueOf(shapeType.getSelected());
    }

    public VisValidatableTextField getMassField() {
        return massField;
    }

    public VisValidatableTextField getCenterOfMassXField() {
        return centerOfMassXField;
    }

    public VisValidatableTextField getCenterOfMassYField() {
        return centerOfMassYField;
    }

    public VisValidatableTextField getRotationalInertiaField() {
        return rotationalInertiaField;
    }

    public VisValidatableTextField getDumpingField() {
        return dumpingField;
    }

    public VisValidatableTextField getGravityScaleField() {
        return gravityScaleField;
    }

    public VisValidatableTextField getDensityField() {
        return densityField;
    }

    public VisValidatableTextField getFrictionField() {
        return frictionField;
    }

    public VisValidatableTextField getRestitutionField() {
        return restitutionField;
    }

    public VisValidatableTextField getHeightField() {
        return heightField;
    }

    public VisValidatableTextField getAngularDampingField() { return angularDumpingField; }

    public VisCheckBox getAllowSleepBox() {
        return allowSleepBox;
    }

    public VisCheckBox getAwakeBox() {
        return awakeBox;
    }

    public VisCheckBox getBulletBox() {
        return bulletBox;
    }

    public VisCheckBox getSensorBox() { return sensor; }

    public VisCheckBox getFixedRotationBox() { return fixedRotation; }

    @Override
    public void onRemove() {
        HyperLap2DFacade.getInstance().sendNotification(CLOSE_CLICKED);
    }
}
