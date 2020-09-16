package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import games.rednblack.editor.graph.GraphBox;
import games.rednblack.editor.graph.GraphBoxProducerImpl;
import games.rednblack.editor.graph.GraphContainer;
import games.rednblack.editor.graph.PopupMenuProducer;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.actions.EntityNodeConfiguration;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;

import java.util.UUID;

public class NodeEditorDialog extends H2DDialog {

    private final GraphContainer<ActionFieldType> graphContainer;
    GraphBoxProducerImpl<ActionFieldType> entityProducer;

    public NodeEditorDialog() {
        super("Node Editor");

        addCloseButton();
        setResizable(true);

        entityProducer = new GraphBoxProducerImpl<>(new EntityNodeConfiguration());

        graphContainer = new GraphContainer<>(VisUI.getSkin(), new PopupMenuProducer() {
            @Override
            public PopupMenu createPopupMenu(float x, float y) {
                return createGraphPopupMenu(x, y);
            }
        });

        String id = UUID.randomUUID().toString().replace("-", "");
        GraphBox<ActionFieldType> graphBox = entityProducer.createDefault(VisUI.getSkin(), id);
        graphContainer.addGraphBox(graphBox, "Entity", false, 0, 0);

        getContentTable().add(graphContainer).grow();

        pack();
    }

    private PopupMenu createGraphPopupMenu(final float popupX, final float popupY) {
        PopupMenu popupMenu = new PopupMenu();

        MenuItem valueMenuItem = new MenuItem("Add Entity");
        valueMenuItem.addListener(
                new ClickListener(Input.Buttons.LEFT) {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        String id = UUID.randomUUID().toString().replace("-", "");
                        GraphBox<ActionFieldType> graphBox = entityProducer.createDefault(VisUI.getSkin(), id);
                        graphContainer.addGraphBox(graphBox, "Property", false, popupX, popupY);
                    }
                });
        popupMenu.addItem(valueMenuItem);
        return popupMenu;
    }

    @Override
    public float getPrefWidth() {
        return Sandbox.getInstance().getUIStage().getWidth();
    }

    @Override
    public float getPrefHeight() {
        return Sandbox.getInstance().getUIStage().getHeight();
    }
}
