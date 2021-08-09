package games.rednblack.editor.view.ui.dialog;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisTextButton;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddToLibraryAction;
import games.rednblack.editor.graph.*;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.actions.config.*;
import games.rednblack.editor.graph.actions.config.value.*;
import games.rednblack.editor.graph.actions.producer.ArrayActionBoxProducer;
import games.rednblack.editor.graph.actions.producer.EventActionBoxProducer;
import games.rednblack.editor.graph.actions.producer.ValueInterpolationBoxProducer;
import games.rednblack.editor.graph.data.Graph;
import games.rednblack.editor.graph.data.GraphConnection;
import games.rednblack.editor.graph.data.GraphValidator;
import games.rednblack.editor.graph.producer.GraphBoxProducer;
import games.rednblack.editor.graph.producer.GraphBoxProducerImpl;
import games.rednblack.editor.graph.producer.value.*;
import games.rednblack.editor.graph.property.PropertyBox;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.GraphConnectionVO;
import games.rednblack.editor.renderer.data.GraphGroupVO;
import games.rednblack.editor.renderer.data.GraphNodeVO;
import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.widget.actors.StaticGrid;
import games.rednblack.h2d.common.H2DDialog;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import games.rednblack.h2d.common.view.ui.widget.H2DPopupMenu;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class NodeEditorDialog extends H2DDialog implements Graph<GraphBox<ActionFieldType>, GraphConnection, PropertyBox<ActionFieldType>, ActionFieldType> {

    private GraphValidator<GraphBox<ActionFieldType>, GraphConnection, PropertyBox<ActionFieldType>, ActionFieldType> graphValidator = new GraphValidator<>();
    private final GraphContainer<ActionFieldType> graphContainer;

    private final GraphBoxProducerImpl<ActionFieldType> entityProducer;
    private final GraphBoxProducerImpl<ActionFieldType> addActionProducer;

    private final Set<GraphBoxProducer<ActionFieldType>> graphBoxProducers = new LinkedHashSet<>();

    private String actionName;

    public NodeEditorDialog() {
        super("Node Editor");

        StaticGrid gridView = new StaticGrid(this);
        getContentTable().addActor(gridView);

        addCloseButton();
        setResizable(true);

        entityProducer = new GraphBoxProducerImpl<>(new EntityNodeConfiguration(), true);
        addActionProducer = new GraphBoxProducerImpl<>(new AddActionNodeConfiguration(), true);

        graphBoxProducers.add(entityProducer);
        graphBoxProducers.add(addActionProducer);

        graphBoxProducers.add(new ValueColorBoxProducer<>(new ValueColorNodeConfiguration()));
        graphBoxProducers.add(new ValueFloatBoxProducer<>(new ValueFloatNodeConfiguration()));
        graphBoxProducers.add(new ValueBooleanBoxProducer<>(new ValueBooleanNodeConfiguration()));
        graphBoxProducers.add(new ValueVector2BoxProducer<>(new ValueVector2NodeConfiguration()));
        graphBoxProducers.add(new ValueInterpolationBoxProducer(new ValueInterpolationNodeConfiguration()));
        graphBoxProducers.add(new ValueParamBoxProducer<>(new ValueParamNodeConfiguration()));

        try {
            graphBoxProducers.add(new ArrayActionBoxProducer(ParallelActionNodeConfiguration.class));
            graphBoxProducers.add(new ArrayActionBoxProducer(SequenceActionNodeConfiguration.class));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }

        graphBoxProducers.add(new GraphBoxProducerImpl<>(new MoveToActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new MoveByActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new SizeToActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new SizeByActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new ScaleToActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new ScaleByActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new RotateToActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new RotateByActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new RepeatActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new ForeverActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new FadeInActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new FadeOutActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new DelayActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new ColorActionNodeConfiguration()));
        graphBoxProducers.add(new GraphBoxProducerImpl<>(new AlphaActionNodeConfiguration()));
        graphBoxProducers.add(new EventActionBoxProducer<>(new EventActionNodeConfiguration()));

        graphContainer = new GraphContainer<>(VisUI.getSkin(), new PopupMenuProducer() {
            @Override
            public H2DPopupMenu createPopupMenu(float x, float y) {
                return createGraphPopupMenu(x, y);
            }
        });
        graphContainer.setParentWindow(this);

        getContentTable().add(graphContainer).grow();

        addListener(
                new GraphChangedListener() {
                    @Override
                    protected boolean graphChanged(GraphChangedEvent event) {
                        if (event.isStructure())
                            updatePipelineValidation();
                        for (GraphBox<ActionFieldType> graphBox : graphContainer.getGraphBoxes()) {
                            graphBox.graphChanged(event, graphContainer.getValidationResult().hasErrors(),
                                    NodeEditorDialog.this);
                        }

                        event.stop();
                        return true;
                    }
                });

        updatePipelineValidation();

        VisTextButton cancelButton = StandardWidgetsFactory.createTextButton("Cancel");
        cancelButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        VisTextButton groupButton = StandardWidgetsFactory.createTextButton("Create Group");
        groupButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                graphContainer.createNodeGroup();
            }
        });
        VisTextButton saveButton = StandardWidgetsFactory.createTextButton("Save");
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Object[] payload = AddToLibraryAction.getPayload(actionName, graphContainer.serializeGraph());
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_ADD_TO_LIBRARY_ACTION, payload);
                close();
            }
        });

        getButtonsTable().add(groupButton).width(140).pad(2).right();
        getButtonsTable().add(cancelButton).width(65).pad(2).right();
        getButtonsTable().add(saveButton).width(65).pad(2).right();
        getCell(getButtonsTable()).right();

        pack();
    }

    private void updatePipelineValidation() {
        graphContainer.setValidationResult(graphValidator.validateGraph(this, "end"));
    }

    private H2DPopupMenu createGraphPopupMenu(final float popupX, final float popupY) {
        H2DPopupMenu popupMenu = new H2DPopupMenu();

        for (final GraphBoxProducer<ActionFieldType> producer : graphBoxProducers) {
            if (producer.isUnique())
                continue;
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

        PopupMenu createdPopup = new H2DPopupMenu();
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
        return Sandbox.getInstance().getUIStage().getWidth() * 0.7f;
    }

    @Override
    public float getPrefHeight() {
        return Sandbox.getInstance().getUIStage().getHeight() * 0.8f;
    }

    @Override
    public GraphBox<ActionFieldType> getNodeById(String id) {
        return graphContainer.getGraphBoxById(id);
    }

    @Override
    public PropertyBox<ActionFieldType> getPropertyByName(String name) {
        return null;
    }

    @Override
    public Iterable<? extends GraphConnection> getConnections() {
        return graphContainer.getConnections();
    }

    @Override
    public Iterable<? extends GraphBox<ActionFieldType>> getNodes() {
        return graphContainer.getGraphBoxes();
    }

    @Override
    public Iterable<? extends PropertyBox<ActionFieldType>> getProperties() {
        return null;
    }

    private GraphBoxProducer<ActionFieldType> findProducerByType(String type) {
        for (GraphBoxProducer<ActionFieldType> graphBoxProducer : graphBoxProducers) {
            if (graphBoxProducer.getType().equals(type))
                return graphBoxProducer;
        }
        return null;
    }

    public void loadGraph(GraphVO graph) {
        for (GraphNodeVO node : graph.nodes) {
            String type = node.type;
            String id = node.id;
            float x = node.x;
            float y = node.y;
            Map<String, String> data = node.data;

            GraphBoxProducer<ActionFieldType> producer = findProducerByType(type);
            if (producer == null)
                throw new IllegalArgumentException("Unable to find pipeline producer for type: " + type);
            GraphBox<ActionFieldType> graphBox = producer.createPipelineGraphBox(skin, id, data);
            graphContainer.addGraphBox(graphBox, producer.getName(), producer.isCloseable(), x, y);
        }
        for (GraphConnectionVO connection : graph.connections) {
            String fromNode = connection.fromNode;
            String fromField = connection.fromField;
            String toNode = connection.toNode;
            String toField = connection.toField;

            graphContainer.addGraphConnection(fromNode, fromField, toNode, toField);
        }
        ArrayList<GraphGroupVO> groups = graph.groups;
        for (GraphGroupVO group : groups) {
            String name = group.name;
            Set<String> nodeIds = new HashSet<>(group.nodes);
            graphContainer.addNodeGroup(name, nodeIds);
        }
    }

    public void loadData(String actionName) {
        this.actionName = actionName;

        graphContainer.clear();
        getTitleLabel().setText(actionName);

        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, GraphVO> items = projectManager.currentProjectInfoVO.libraryActions;

        if (items.get(actionName) != null) {
            loadGraph(items.get(actionName));
        } else {
            String id = UUID.randomUUID().toString().replace("-", "");
            GraphBox<ActionFieldType> graphBox = entityProducer.createDefault(VisUI.getSkin(), id);
            graphContainer.addGraphBox(graphBox, "Entity", false, 0, 0);

            graphBox = addActionProducer.createDefault(VisUI.getSkin(), "end");
            graphContainer.addGraphBox(graphBox, "Add Action", false, getPrefWidth() - 270, 0);
        }

        graphContainer.adjustCanvas();
    }
}
