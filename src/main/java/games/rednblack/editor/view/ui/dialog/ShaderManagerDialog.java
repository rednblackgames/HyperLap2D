package games.rednblack.editor.view.ui.dialog;
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.FormRow;
import games.rednblack.h2d.common.view.ui.ListTable;
import games.rednblack.h2d.common.view.ui.PropertyGrid;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.puremvc.Facade;

import java.util.Set;
import java.util.TreeSet;

public class ShaderManagerDialog extends H2DDialog {

    public static final String prefix = "games.rednblack.editor.view.ui.dialog.ShaderManagerDialog";

    public static final String EDIT_FRAGMENT_SHADER = prefix + ".EDIT_FRAGMENT_SHADER";
    public static final String EDIT_VERTEX_SHADER = prefix + ".EDIT_VERTEX_SHADER";
    public static final String EDIT_FRAGMENT_SHADER_DONE = prefix + ".EDIT_FRAGMENT_SHADER_DONE";
    public static final String EDIT_VERTEX_SHADER_DONE = prefix + ".EDIT_VERTEX_SHADER_DONE";

    public static final String CREATE_NEW_SHADER = prefix + ".CREATE_NEW_SHADER";

    /** The shader kinds offered when creating one, in the order the create notification expects. */
    private static final String[] SHADER_TYPES = {"Simple", "Distance Field", "Screen Reading"};

    private static final int ACTION_GAP = 4;

    private final Facade facade;

    private final VisTextField newShaderName;
    private final VisSelectBox<String> newShaderTypeSelectBox;
    private final VisTable listContainer;

    public ShaderManagerDialog(Facade facade) {
        super("Shader Manager");
        this.facade = facade;

        addCloseButton();
        closeOnEscape();
        getContentTable().top().left();

        newShaderName = StandardWidgetsFactory.createTextField();
        newShaderName.setMessageText("Shader name");

        newShaderTypeSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        newShaderTypeSelectBox.setItems(SHADER_TYPES);

        VisTextButton createButton = StandardWidgetsFactory.createTextButton("Create");
        createButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (newShaderName.getText().isEmpty()) return;

                Object[] payload = new Object[2];
                payload[0] = newShaderName.getText();
                payload[1] = newShaderTypeSelectBox.getSelectedIndex();

                facade.sendNotification(CREATE_NEW_SHADER, payload);
                newShaderName.setText("");
            }
        });

        // the table is rebuilt on every update, so it lives in a container the scroll pane keeps
        listContainer = new VisTable();
        listContainer.top();
        VisScrollPane scrollPane = StandardWidgetsFactory.createScrollPane(listContainer);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        // the dialog's content table is a plain Table, so the grid gets a body of its own, grown to
        // the dialog so that the list row underneath has height to take
        VisTable body = new VisTable();
        getContentTable().add(body).grow();

        PropertyGrid grid = PropertyGrid.on(body).dialogScale().padPanel();
        grid.section("New shader");
        grid.wideContent(new FormRow()
                .label("Name").field(newShaderName)
                .label("Type").compact(newShaderTypeSelectBox)
                .action(createButton));

        grid.section("Shaders");
        grid.wideGrow(scrollPane);

        updateShaderList(new TreeSet<>());
    }

    public void updateShaderList(Set<String> shaders) {
        Array<String> sorted = new Array<>();
        for (String shader : shaders) {
            sorted.add(shader);
        }
        sorted.sort();

        listContainer.clear();
        listContainer.add(buildShaderTable(sorted)).growX().top().row();
    }

    /** One row per shader: its name, then the actions that operate on it. */
    private ListTable buildShaderTable(Array<String> shaders) {
        ListTable table = new ListTable("Shader");
        if (shaders.isEmpty()) {
            return table.message("No shaders in this project yet");
        }
        for (String shader : shaders) {
            table.item(shader).action(shaderActions(shader));
        }
        return table;
    }

    private VisTable shaderActions(String shader) {
        VisTextButton editFragment = StandardWidgetsFactory.createTextButton("Fragment");
        StandardWidgetsFactory.addTooltip(editFragment, "Edit the fragment shader");
        editFragment.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(EDIT_FRAGMENT_SHADER, shader);
            }
        });

        VisTextButton editVertex = StandardWidgetsFactory.createTextButton("Vertex");
        StandardWidgetsFactory.addTooltip(editVertex, "Edit the vertex shader");
        editVertex.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(EDIT_VERTEX_SHADER, shader);
            }
        });

        VisImageButton deleteButton = StandardWidgetsFactory.createImageButton("trash-button");
        StandardWidgetsFactory.addTooltip(deleteButton, "Delete this shader");
        deleteButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                facade.sendNotification(MsgAPI.ACTION_DELETE_SHADER, shader);
            }
        });

        VisTable actions = new VisTable();
        actions.add(editFragment).height(PropertyGrid.FIELD_HEIGHT);
        actions.add(editVertex).height(PropertyGrid.FIELD_HEIGHT).padLeft(ACTION_GAP);
        actions.add(deleteButton).padLeft(ACTION_GAP + 2);
        return actions;
    }

    @Override
    public float getPrefWidth() {
        // a share of the stage, but never narrower than the form on top needs
        return Math.max(super.getPrefWidth(),
                PluginUIBridge.get().getSandbox().getUIStage().getWidth() * 0.3f);
    }

    @Override
    public float getPrefHeight() {
        return PluginUIBridge.get().getSandbox().getUIStage().getHeight() * 0.4f;
    }
}
