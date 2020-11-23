package games.rednblack.editor.view.ui.panel;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.VisSelectBox;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisValidatableTextField;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.ui.validator.StringNameValidator;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class ShaderUniformsPanel extends UIDraggablePanel {

    private final HyperLap2DFacade facade;
    private final VisTable addUniformTable, inputTable;
    private final VisValidatableTextField uniformName, input1, input2, input3, input4;
    private final VisSelectBox<String> uniformType;
    private final VisTextButton addButton;

    private final InputValidator integerValidator = new Validators.IntegerValidator();
    private final InputValidator floatValidator = new Validators.FloatValidator();

    public ShaderUniformsPanel() {
        super("Shader Uniforms");
        addCloseButton();
        uniformType = StandardWidgetsFactory.createSelectBox(String.class);
        uniformType.setItems("int", "float", "vec2", "vec3", "vec4");

        uniformName = StandardWidgetsFactory.createValidableTextField(new StringNameValidator());
        input1 = StandardWidgetsFactory.createValidableTextField(floatValidator);
        input2 = StandardWidgetsFactory.createValidableTextField(floatValidator);
        input3 = StandardWidgetsFactory.createValidableTextField(floatValidator);
        input4 = StandardWidgetsFactory.createValidableTextField(floatValidator);

        addButton = StandardWidgetsFactory.createTextButton("Add");

        facade = HyperLap2DFacade.getInstance();
        getContentTable().pad(5).padTop(10);

        addUniformTable = new VisTable();
        addUniformTable.defaults().padRight(3);
        addUniformTable.add("Name:");
        addUniformTable.add(uniformName).width(100);
        addUniformTable.add("Type:");
        addUniformTable.add(uniformType);

        addUniformTable.add("Value:");
        inputTable = new VisTable();
        inputTable.defaults().padRight(2).width(60);
        updateInputFields();
        addUniformTable.add(inputTable).growX();
        addUniformTable.add(addButton);

        setListeners();
    }

    public void setEmpty() {
        setEmpty("No item selected");
    }

    public void setEmpty(String message) {
        getContentTable().clear();
        getContentTable().add(message);
        pack();
    }

    public void updateView() {
        getContentTable().clear();

        getContentTable().add(addUniformTable).growX();
        pack();
    }

    private void setListeners() {
        uniformType.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                updateInputFields();
            }
        });
    }

    private void updateInputFields() {
        inputTable.clear();

        switch (uniformType.getSelected()) {
            case "int":
                input1.getValidators().clear();
                input1.addValidator(integerValidator);
                inputTable.add(input1);
                break;
            case "float":
                input1.getValidators().clear();
                input1.addValidator(floatValidator);
                inputTable.add(input1);
                break;
            case "vec2":
                input1.getValidators().clear();
                input1.addValidator(floatValidator);
                inputTable.add(input1);
                inputTable.add(input2);
                break;
            case "vec3":
                input1.getValidators().clear();
                input1.addValidator(floatValidator);

                inputTable.add(input1);
                inputTable.add(input2);
                inputTable.add(input3);
                break;
            case "vec4":
                input1.getValidators().clear();
                input1.addValidator(floatValidator);

                inputTable.add(input1);
                inputTable.add(input2);
                inputTable.add(input3);
                inputTable.add(input4);
                break;
        }
        pack();
    }

    @Override
    public float getPrefWidth() {
        return Math.max(super.getPrefWidth(), 250);
    }
}
