package games.rednblack.editor.view.ui.panel;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.renderer.data.ShaderUniformVO;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.FormRow;
import games.rednblack.h2d.common.view.ui.ListTable;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

public class ShaderUniformsPanel extends UIDraggablePanel {
    private static final String prefix = "games.rednblack.editor.view.ui.panel.ShaderUniformsPanel";
    public static final String ADD_BUTTON_CLICKED = prefix + ".ADD_BUTTON_CLICKED";
    public static final String REMOVE_BUTTON_CLICKED = prefix + ".REMOVE_BUTTON_CLICKED";

    private static final int MIN_WIDTH = 560;
    /** Wide enough for the longest form: a vec4 needs four value fields on the same line. */
    private static final int MAX_WIDTH = 900;
    /** Each component of a uniform value gets a field of its own, four of them for a vec4. */
    private static final int VALUE_WIDTH = 74;
    private static final int VALUE_MIN_WIDTH = 56;

    private final Facade facade;
    private final VisTable mainTable, inputTable;
    private final VisValidatableTextField input1, input2, input3, input4;
    private final VisSelectBox<String> uniformName;
    private final VisTextButton addButton;
    private final VisLabel uniformType;

    private final InputValidator integerValidator = new Validators.IntegerValidator();
    private final InputValidator floatValidator = new Validators.FloatValidator();

    private ObjectMap<String, String> uniforms;
    private ObjectMap<String, ShaderUniformVO> customUniforms;

    public ShaderUniformsPanel() {
        super("Shader Uniforms");
        addCloseButton();

        facade = Facade.getInstance();

        uniformType = PropertyGrid.value("");
        uniformName = StandardWidgetsFactory.createSelectBox(String.class);
        input1 = StandardWidgetsFactory.createValidableTextField(floatValidator);
        input2 = StandardWidgetsFactory.createValidableTextField(floatValidator);
        input3 = StandardWidgetsFactory.createValidableTextField(floatValidator);
        input4 = StandardWidgetsFactory.createValidableTextField(floatValidator);
        addButton = StandardWidgetsFactory.createTextButton("Add");

        inputTable = new VisTable();

        mainTable = new VisTable();
        getContentTable().add(mainTable).growX();

        setListeners();
    }

    public void setEmpty() {
        setEmpty("No item selected");
    }

    public void setEmpty(String message) {
        mainTable.clear();
        PropertyGrid.on(mainTable).dialogScale().padPanel().wideCentered(PropertyGrid.value(message));
        invalidateHeight();
    }

    public void updateView(ObjectMap<String, String> uniforms, ObjectMap<String, ShaderUniformVO> customUniforms) {
        clearInputs();
        mainTable.clear();

        if (uniforms.size == 0)
            return;

        this.uniforms = uniforms;
        this.customUniforms = customUniforms;
        uniformName.setItems(uniforms.keys().toArray());

        PropertyGrid grid = PropertyGrid.on(mainTable).dialogScale().padPanel();
        grid.section("Add uniform");
        grid.wideContent(new FormRow()
                .label("Uniform").field(uniformName)
                .label("Type").compact(uniformType)
                .label("Value").group(inputTable)
                .action(addButton));

        grid.section("Uniforms");
        grid.wideContent(createUniformsList());

        invalidateHeight();
    }

    /** The shader's uniforms that carry a custom value, one row each. */
    private ListTable createUniformsList() {
        ListTable list = new ListTable("Name", "Type", "X", "Y", "Z", "W");
        if (customUniforms.size == 0) {
            return list.message("No custom uniform values yet");
        }
        for (String key : customUniforms.keys()) {
            removeUniformFromList(key);

            ShaderUniformVO uniformVO = customUniforms.get(key);
            String[] values = uniformValues(uniformVO);

            VisImageButton deleteButton = StandardWidgetsFactory.createImageButton("trash-button");
            deleteButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    facade.sendNotification(REMOVE_BUTTON_CLICKED, key);
                }
            });

            // Clicking the row loads the uniform back into the form above, as the other tables do.
            list.item(key, uniformVO.getType(), values[0], values[1], values[2], values[3])
                    .action(deleteButton)
                    .onClick(() -> {
                        addUniformFromList(key);
                        uniformName.setSelected(key);
                        uniformName.setDisabled(true);
                        editUniform(uniformVO);
                    });
        }
        return list;
    }

    /** The four components of a uniform value, empty where its type has none. */
    private static String[] uniformValues(ShaderUniformVO uniformVO) {
        String[] values = {"", "", "", ""};
        switch (uniformVO.getType()) {
            case "int" -> values[0] = String.valueOf(uniformVO.intValue);
            case "float" -> values[0] = String.valueOf(uniformVO.floatValue);
            case "vec2" -> {
                values[0] = String.valueOf(uniformVO.floatValue);
                values[1] = String.valueOf(uniformVO.floatValue2);
            }
            case "vec3" -> {
                values[0] = String.valueOf(uniformVO.floatValue);
                values[1] = String.valueOf(uniformVO.floatValue2);
                values[2] = String.valueOf(uniformVO.floatValue3);
            }
            case "vec4" -> {
                values[0] = String.valueOf(uniformVO.floatValue);
                values[1] = String.valueOf(uniformVO.floatValue2);
                values[2] = String.valueOf(uniformVO.floatValue3);
                values[3] = String.valueOf(uniformVO.floatValue4);
            }
        }
        return values;
    }

    private void setListeners() {
        uniformName.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (uniformName.getSelected() != null)
                    uniformType.setText(uniforms.get(uniformName.getSelected()));
                updateInputFields();
            }
        });

        addButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Object[] payload;
                switch (uniforms.get(uniformName.getSelected())) {
                    case "int":
                        if (!input1.isInputValid())
                            return;
                        payload = new Object[2];
                        payload[1] = Integer.valueOf(input1.getText());
                        break;
                    case "float":
                        if (!input1.isInputValid())
                            return;
                        payload = new Object[2];
                        payload[1] = Float.valueOf(input1.getText());
                        break;
                    case "vec2":
                        if (!input1.isInputValid() || !input2.isInputValid())
                            return;
                        payload = new Object[3];
                        payload[1] = Float.valueOf(input1.getText());
                        payload[2] = Float.valueOf(input2.getText());
                        break;
                    case "vec3":
                        if (!input1.isInputValid() || !input2.isInputValid() || !input3.isInputValid())
                            return;
                        payload = new Object[4];
                        payload[1] = Float.valueOf(input1.getText());
                        payload[2] = Float.valueOf(input2.getText());
                        payload[3] = Float.valueOf(input3.getText());
                        break;
                    case "vec4":
                        if (!input1.isInputValid() || !input2.isInputValid() || !input3.isInputValid() || !input4.isInputValid())
                            return;
                        payload = new Object[5];
                        payload[1] = Float.valueOf(input1.getText());
                        payload[2] = Float.valueOf(input2.getText());
                        payload[3] = Float.valueOf(input3.getText());
                        payload[4] = Float.valueOf(input4.getText());
                        break;
                    default:
                        payload = new Object[1];
                }

                String name = uniformName.getSelected();
                payload[0] = name;

                facade.sendNotification(ADD_BUTTON_CLICKED, payload);

                clearInputs();
            }
        });
    }

    /** Zero rather than empty: an empty field fails its validator and paints a red error border. */
    private void clearInputs() {
        uniformName.setDisabled(false);
        input1.setText("0");
        input2.setText("0");
        input3.setText("0");
        input4.setText("0");
    }

    /** One input per component of the selected uniform's type, sharing the field column. */
    private void updateInputFields() {
        inputTable.clear();
        if (uniformName.getSelected() == null)
            return;

        String type = uniforms.get(uniformName.getSelected());
        input1.getValidators().clear();
        input1.addValidator("int".equals(type) ? integerValidator : floatValidator);

        switch (type) {
            case "int", "float" -> addInputs(input1);
            case "vec2" -> addInputs(input1, input2);
            case "vec3" -> addInputs(input1, input2, input3);
            case "vec4" -> addInputs(input1, input2, input3, input4);
        }
        invalidateHeight();
    }

    private void addInputs(VisValidatableTextField... inputs) {
        for (int i = 0; i < inputs.length; i++) {
            inputTable.add(inputs[i]).growX().prefWidth(VALUE_WIDTH).minWidth(VALUE_MIN_WIDTH)
                    .height(PropertyGrid.FIELD_HEIGHT)
                    .padLeft(i == 0 ? 0 : PropertyGrid.SUB_LABEL_GAP);
        }
    }

    public void removeUniformFromList(String uniform) {
        Array<String> list = new Array<>(uniformName.getItems());
        list.removeValue(uniform, false);
        uniformName.setItems(list);
    }

    public void addUniformFromList(String uniform) {
        Array<String> list = new Array<>(uniformName.getItems());
        list.add(uniform);
        uniformName.setItems(list);
    }

    private void editUniform(ShaderUniformVO uniformVO) {
        switch (uniformVO.getType()) {
            case "int" -> {
                input1.setText(String.valueOf(uniformVO.intValue));
            }
            case "float" -> {
                input1.setText(String.valueOf(uniformVO.floatValue));
            }
            case "vec2" -> {
                input1.setText(String.valueOf(uniformVO.floatValue));
                input2.setText(String.valueOf(uniformVO.floatValue2));
            }
            case "vec3" -> {
                input1.setText(String.valueOf(uniformVO.floatValue));
                input2.setText(String.valueOf(uniformVO.floatValue2));
                input3.setText(String.valueOf(uniformVO.floatValue3));
            }
            case "vec4" -> {
                input1.setText(String.valueOf(uniformVO.floatValue));
                input2.setText(String.valueOf(uniformVO.floatValue2));
                input3.setText(String.valueOf(uniformVO.floatValue3));
                input4.setText(String.valueOf(uniformVO.floatValue4));
            }
        }
    }

    @Override
    public float getPrefWidth() {
        return Math.min(Math.max(super.getPrefWidth(), MIN_WIDTH), MAX_WIDTH);
    }
}
