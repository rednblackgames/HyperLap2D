package games.rednblack.editor.view.ui.panel;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.util.InputValidator;
import com.kotcrab.vis.ui.util.Validators;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.data.ShaderUniformVO;
import games.rednblack.editor.view.ui.widget.actors.table.CellBody;
import games.rednblack.editor.view.ui.widget.actors.table.CellHeader;
import games.rednblack.h2d.common.UIDraggablePanel;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class ShaderUniformsPanel extends UIDraggablePanel {
    private static final String prefix = "games.rednblack.editor.view.ui.panel.ShaderUniformsPanel";
    public static final String ADD_BUTTON_CLICKED = prefix + ".ADD_BUTTON_CLICKED";
    public static final String REMOVE_BUTTON_CLICKED = prefix + ".REMOVE_BUTTON_CLICKED";

    private final HyperLap2DFacade facade;
    private final VisTable addUniformTable, inputTable, headerUniformsTable, uniformsTable;
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
        uniformType = StandardWidgetsFactory.createLabel("", "default", Align.center);

        uniformName = StandardWidgetsFactory.createSelectBox(String.class);
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
        addUniformTable.add(uniformName).width(140);
        addUniformTable.add("Type:");
        addUniformTable.add(uniformType).width(50);

        addUniformTable.add("Value:");
        inputTable = new VisTable();
        inputTable.defaults().padRight(2);
        addUniformTable.add(inputTable).width(248);
        addUniformTable.add(addButton);

        uniformsTable = new VisTable();
        uniformsTable.defaults().growX().minWidth(91);

        headerUniformsTable = new VisTable();
        headerUniformsTable.defaults().growX().minWidth(91);

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

    public void updateView(ObjectMap<String, String> uniforms, ObjectMap<String, ShaderUniformVO> customUniforms) {
        clearInputs();
        getContentTable().clear();

        if (uniforms.size == 0)
            return;

        this.uniforms = uniforms;
        this.customUniforms = customUniforms;
        uniformName.setItems(uniforms.keys().toArray());

        getContentTable().add(addUniformTable).growX().row();
        hSeparator(getContentTable());

        headerUniformsTable.clear();
        headerUniformsTable.add(new CellHeader("Name"));
        headerUniformsTable.add(new CellHeader("Type"));
        headerUniformsTable.add(new CellHeader("X"));
        headerUniformsTable.add(new CellHeader("Y"));
        headerUniformsTable.add(new CellHeader("Z"));
        headerUniformsTable.add(new CellHeader("W"));
        headerUniformsTable.add(new CellHeader("Edit"));

        getContentTable().add(headerUniformsTable).growX().row();
        hSeparator(getContentTable());

        getContentTable().add(uniformsTable).growX();

        updateUniformsTable();

        pack();
    }

    private void hSeparator(Table table) {
        table.add(new Separator()).padTop(2).fillX().expandX().padBottom(2).row();
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

    private void clearInputs() {
        uniformName.setDisabled(false);
        input1.setText("");
        input2.setText("");
        input3.setText("");
        input4.setText("");
    }

    private void updateInputFields() {
        inputTable.clear();
        if (uniformName.getSelected() == null)
            return;

        switch (uniforms.get(uniformName.getSelected())) {
            case "int":
                inputTable.defaults().width(240);
                input1.getValidators().clear();
                input1.addValidator(integerValidator);
                inputTable.add(input1);
                break;
            case "float":
                inputTable.defaults().width(240);
                input1.getValidators().clear();
                input1.addValidator(floatValidator);
                inputTable.add(input1);
                break;
            case "vec2":
                inputTable.defaults().width(120);
                input1.getValidators().clear();
                input1.addValidator(floatValidator);
                inputTable.add(input1);
                inputTable.add(input2);
                break;
            case "vec3":
                inputTable.defaults().width(80);
                input1.getValidators().clear();
                input1.addValidator(floatValidator);

                inputTable.add(input1);
                inputTable.add(input2);
                inputTable.add(input3);
                break;
            case "vec4":
                inputTable.defaults().width(60);
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

    private void updateUniformsTable() {
        uniformsTable.clear();
        uniformsTable.add();
        uniformsTable.add();
        uniformsTable.add();
        uniformsTable.add();
        uniformsTable.add();
        uniformsTable.add();
        uniformsTable.add().row();

        for (String key : customUniforms.keys()) {
            removeUniformFromList(key);

            uniformsTable.add(new CellBody(key));
            ShaderUniformVO uniformVO = customUniforms.get(key);

            uniformsTable.add(new CellBody(uniformVO.getType()));

            switch (uniformVO.getType()) {
                case "int" -> {
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.intValue))).colspan(4);
                }
                case "float" -> {
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue))).colspan(4);
                }
                case "vec2" -> {
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue)));
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue2)));
                }
                case "vec3" -> {
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue)));
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue2)));
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue3)));
                }
                case "vec4" -> {
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue)));
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue2)));
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue3)));
                    uniformsTable.add(new CellBody(String.valueOf(uniformVO.floatValue4)));
                }
            }

            VisTable editTable = new VisTable();
            LinkLabel editLabel = new LinkLabel("Edit");
            editLabel.setListener(url -> {
                addUniformFromList(key);
                uniformName.setSelected(key);
                uniformName.setDisabled(true);

                editUniform(uniformVO);
            });
            editTable.add(editLabel);
            editTable.add("/").padLeft(2).padRight(2);
            LinkLabel deleteLabel = new LinkLabel("Delete");
            deleteLabel.setListener(url -> {
                facade.sendNotification(REMOVE_BUTTON_CLICKED, key);
            });
            editTable.add(deleteLabel);
            uniformsTable.add(new CellBody(editTable));
            uniformsTable.row();
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
        return Math.min(Math.max(super.getPrefWidth(), 250), 667);
    }
}
