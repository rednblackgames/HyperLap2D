package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import games.rednblack.editor.graph.GraphBox;
import games.rednblack.editor.graph.actions.config.value.ValueBooleanNodeConfiguration;
import games.rednblack.editor.graph.actions.config.value.ValueColorNodeConfiguration;
import games.rednblack.editor.graph.actions.config.value.ValueFloatNodeConfiguration;
import games.rednblack.editor.graph.actions.producer.value.ValueBooleanBoxProducer;
import games.rednblack.editor.graph.actions.producer.value.ValueColorBoxProducer;
import games.rednblack.editor.graph.actions.producer.value.ValueFloatBoxProducer;
import games.rednblack.editor.graph.producer.GraphBoxProducer;
import games.rednblack.editor.graph.producer.GraphBoxProducerImpl;
import games.rednblack.editor.graph.GraphContainer;
import games.rednblack.editor.graph.PopupMenuProducer;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.actions.config.EntityNodeConfiguration;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.H2DDialog;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class NodeEditorDialog extends H2DDialog {

    private final GraphContainer<ActionFieldType> graphContainer;
    GraphBoxProducerImpl<ActionFieldType> entityProducer;

    private final Set<GraphBoxProducer<ActionFieldType>> graphBoxProducers = new LinkedHashSet<>();

    public NodeEditorDialog() {
        super("Node Editor");

        addCloseButton();
        setResizable(true);

        graphBoxProducers.add(new ValueColorBoxProducer<>(new ValueColorNodeConfiguration()));
        graphBoxProducers.add(new ValueFloatBoxProducer<>(new ValueFloatNodeConfiguration()));
        graphBoxProducers.add(new ValueBooleanBoxProducer<>(new ValueBooleanNodeConfiguration()));

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

        for (final GraphBoxProducer<ActionFieldType> producer : graphBoxProducers) {
            String menuLocation = producer.getMenuLocation();
            if (menuLocation != null) {
                String[] menuSplit = menuLocation.split("/");
                PopupMenu targetMenu = findOrCreatePopupMenu(popupMenu, menuSplit, 0);
                final String title = producer.getName();
                MenuItem valueMenuItem = new MenuItem(title);
                valueMenuItem.addListener(
                        new ClickListener(Input.Buttons.LEFT) {
                            @Override
                            public void clicked(InputEvent event, float x, float y) {
                                String id = UUID.randomUUID().toString().replace("-", "");
                                GraphBox<ActionFieldType> graphBox = producer.createDefault(skin, id);
                                graphContainer.addGraphBox(graphBox, title, true, popupX, popupY);
                            }
                        });
                targetMenu.addItem(valueMenuItem);
            }
        }

        return popupMenu;
    }

    private PopupMenu findOrCreatePopupMenu(PopupMenu popupMenu, String[] menuSplit, int startIndex) {
        for (Actor child : popupMenu.getChildren()) {
            MenuItem childMenuItem = (MenuItem) child;
            if (childMenuItem.getLabel().getText().toString().equals(menuSplit[startIndex]) && childMenuItem.getSubMenu() != null) {
                if (startIndex + 1 < menuSplit.length) {
                    return findOrCreatePopupMenu(childMenuItem.getSubMenu(), menuSplit, startIndex + 1);
                } else {
                    return childMenuItem.getSubMenu();
                }
            }
        }

        PopupMenu createdPopup = new PopupMenu();
        MenuItem createdMenuItem = new MenuItem(menuSplit[startIndex]);
        createdMenuItem.setSubMenu(createdPopup);
        popupMenu.addItem(createdMenuItem);
        if (startIndex + 1 < menuSplit.length) {
            return findOrCreatePopupMenu(createdPopup, menuSplit, startIndex + 1);
        } else {
            return createdPopup;
        }
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
