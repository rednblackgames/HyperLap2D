package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.util.adapter.AbstractListAdapter;
import com.kotcrab.vis.ui.util.adapter.SimpleListAdapter;
import com.kotcrab.vis.ui.widget.*;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.listener.ScrollFocusListener;

import java.util.Set;

public class ShaderManagerDialog extends H2DDialog {

    private final SimpleListAdapter<String> shadersListAdapter;
    private final Array<String> shaderItems = new Array<>();

    private final VisSelectBox<String> newShaderTypeSelectBox;

    public static final String prefix = "games.rednblack.editor.view.ui.dialog.ShaderManagerDialog";

    public static final String EDIT_FRAGMENT_SHADER = prefix + ".EDIT_FRAGMENT_SHADER";
    public static final String EDIT_VERTEX_SHADER = prefix + ".EDIT_VERTEX_SHADER";
    public static final String EDIT_FRAGMENT_SHADER_DONE = prefix + ".EDIT_FRAGMENT_SHADER_DONE";
    public static final String EDIT_VERTEX_SHADER_DONE = prefix + ".EDIT_VERTEX_SHADER_DONE";

    public static final String CREATE_NEW_SHADER = prefix + ".CREATE_NEW_SHADER";

    public ShaderManagerDialog() {
        super("Shader Manager");

        addCloseButton();
        getContentTable().top().left();

        shadersListAdapter = new ShaderResourceListAdapter(shaderItems);
        shadersListAdapter.setSelectionMode(AbstractListAdapter.SelectionMode.DISABLED);
        shadersListAdapter.getSelectionManager().setProgrammaticChangeEvents(false);

        ListView<String> shaderList = new ListView<>(shadersListAdapter);
        shaderList.getScrollPane().addListener(new ScrollFocusListener());

        getContentTable().add(shaderList.getMainTable()).uniformX().grow();

        VisTextField newShaderName = StandardWidgetsFactory.createTextField();
        newShaderName.setMessageText("New Shader Name");
        getButtonsTable().add(newShaderName).growX();

        VisTextButton newShaderButton = StandardWidgetsFactory.createTextButton("Create");
        newShaderButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (newShaderName.getText().isEmpty()) return;

                Object[] payload = new Object[2];
                payload[0] = newShaderName.getText();
                payload[1] = newShaderTypeSelectBox.getSelectedIndex();

                HyperLap2DFacade.getInstance().sendNotification(CREATE_NEW_SHADER, payload);
            }
        });

        String[] data = {"Simple", "Simple Array", "Distance Field", "Screen Reading"};
        newShaderTypeSelectBox = StandardWidgetsFactory.createSelectBox(String.class);
        newShaderTypeSelectBox.setItems(data);

        getButtonsTable().add(newShaderTypeSelectBox);
        getButtonsTable().add(newShaderButton).pad(2);
        getCell(getButtonsTable()).growX();
    }

    public void updateShaderList(Set<String> shaders) {
        shaderItems.clear();
        for (String item : shaders)
            shaderItems.add(item);
        shaderItems.sort();

        shadersListAdapter.itemsChanged();
    }

    @Override
    public float getPrefWidth() {
        return Sandbox.getInstance().getUIStage().getWidth() * 0.3f;
    }

    @Override
    public float getPrefHeight() {
        return Sandbox.getInstance().getUIStage().getHeight() * 0.4f;
    }

    public static class ShaderResourceListAdapter extends SimpleListAdapter<String> {

        public ShaderResourceListAdapter(Array<String> array) {
            super(array);
        }

        @Override
        protected VisTable createView(String item) {
            VisTable table = new VisTable();
            table.left();
            table.add(item).left().expandX();
            VisTable tableButton = new VisTable();
            VisTextButton editFragment = StandardWidgetsFactory.createTextButton("Edit Fragment");
            editFragment.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    HyperLap2DFacade.getInstance().sendNotification(EDIT_FRAGMENT_SHADER, item);
                }
            });
            tableButton.add(editFragment).pad(8);
            VisTextButton editVertex = StandardWidgetsFactory.createTextButton("Edit Vertex");
            editVertex.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    HyperLap2DFacade.getInstance().sendNotification(EDIT_VERTEX_SHADER, item);
                }
            });
            tableButton.add(editVertex).pad(8);
            VisTextButton deleteButton = StandardWidgetsFactory.createTextButton("Delete");
            deleteButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_DELETE_SHADER, item);
                }
            });
            tableButton.add(deleteButton).pad(8);
            table.add(tableButton).right().row();
            table.add(new Separator("menu")).padTop(2).padBottom(2).fill().expand().colspan(2).row();

            return table;
        }
    }
}
