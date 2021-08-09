package games.rednblack.editor.graph;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Align;
import com.google.common.base.Supplier;
import games.rednblack.editor.graph.data.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GraphBoxImpl<T extends FieldType> implements GraphBox<T> {
    private String id;
    private NodeConfiguration<T> configuration;
    private Table table, headerTable, partTable, footerTable;
    private List<GraphBoxPart<T>> graphBoxParts = new LinkedList<>();
    private Map<String, GraphBoxInputConnector<T>> inputConnectors = new HashMap<>();
    private Map<String, GraphBoxOutputConnector<T>> outputConnectors = new HashMap<>();

    private GraphContainer<T>.GraphBoxWindow window;

    private GraphBoxSerializeCallback serializeCallback;

    public GraphBoxImpl(String id, NodeConfiguration<T> configuration, Skin skin) {
        this.id = id;
        this.configuration = configuration;
        table = new Table(skin);
        headerTable = new Table(skin);
        partTable = new Table(skin);
        footerTable = new Table(skin);
        table.add(headerTable).padBottom(5).growX().row();
        table.add(partTable).growX().row();
        table.add(footerTable).growX();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return configuration.getType();
    }

    @Override
    public boolean isInputField(String fieldId) {
        return inputConnectors.containsKey(fieldId);
    }

    @Override
    public void graphChanged(GraphChangedEvent event, boolean hasErrors, Graph<? extends GraphNode<T>, ? extends GraphConnection, ? extends GraphProperty<T>, T> graph) {

    }

    @Override
    public NodeConfiguration<T> getConfiguration() {
        return configuration;
    }

    public void addTopConnector(GraphNodeInput<T> graphNodeInput) {
        inputConnectors.put(graphNodeInput.getFieldId(), new GraphBoxInputConnectorImpl<T>(GraphBoxInputConnector.Side.Top, new Supplier<Float>() {
            @Override
            public Float get() {
                return table.getWidth() / 2f;
            }
        }, graphNodeInput.getFieldId()));
    }

    public void addBottomConnector(GraphNodeOutput<T> graphNodeOutput) {
        outputConnectors.put(graphNodeOutput.getFieldId(), new GraphBoxOutputConnectorImpl<T>(GraphBoxOutputConnector.Side.Bottom,
                new Supplier<Float>() {
                    @Override
                    public Float get() {
                        return table.getWidth() / 2f;
                    }
                }, graphNodeOutput.getFieldId()));
    }

    public void addTwoSideGraphPart(Skin skin,
                                    GraphNodeInput<T> graphNodeInput,
                                    GraphNodeOutput<T> graphNodeOutput) {
        Table table = new Table();
        table.add(new Label(graphNodeInput.getFieldName(), skin)).grow();
        Label outputLabel = new Label(graphNodeOutput.getFieldName(), skin);
        outputLabel.setAlignment(Align.right);
        table.add(outputLabel).grow();
        table.row();

        GraphBoxPartImpl<T> graphBoxPart = new GraphBoxPartImpl<T>(table, null);
        graphBoxPart.setInputConnector(GraphBoxInputConnector.Side.Left, graphNodeInput);
        graphBoxPart.setOutputConnector(GraphBoxOutputConnector.Side.Right, graphNodeOutput);
        addGraphBoxPart(graphBoxPart);
    }

    public void addInputGraphPart(Skin skin,
                                  GraphNodeInput<T> graphNodeInput) {
        Table table = new Table();
        table.add(new Label(graphNodeInput.getFieldName(), skin)).grow().row();

        GraphBoxPartImpl<T> graphBoxPart = new GraphBoxPartImpl<T>(table, null);
        graphBoxPart.setInputConnector(GraphBoxInputConnector.Side.Left, graphNodeInput);
        addGraphBoxPart(graphBoxPart);
    }

    public void addOutputGraphPart(
            Skin skin,
            GraphNodeOutput<T> graphNodeOutput) {
        Table table = new Table();
        Label outputLabel = new Label(graphNodeOutput.getFieldName(), skin);
        outputLabel.setAlignment(Align.right);
        table.add(outputLabel).grow().row();

        GraphBoxPartImpl<T> graphBoxPart = new GraphBoxPartImpl<T>(table, null);
        graphBoxPart.setOutputConnector(GraphBoxOutputConnector.Side.Right, graphNodeOutput);
        addGraphBoxPart(graphBoxPart);
    }

    public void addHeaderGraphBoxPart(GraphBoxPart<T> graphBoxPart) {
        final Actor actor = graphBoxPart.getActor();
        headerTable.add(actor).growX().row();
    }

    public void addFooterGraphBoxPart(GraphBoxPart<T> graphBoxPart) {
        final Actor actor = graphBoxPart.getActor();
        footerTable.add(actor).padTop(5).growX().row();
    }

    public void addGraphBoxPart(GraphBoxPart<T> graphBoxPart) {
        graphBoxParts.add(graphBoxPart);
        final Actor actor = graphBoxPart.getActor();
        partTable.add(actor).growX().row();
        final GraphBoxInputConnector<T> inputConnector = graphBoxPart.getInputConnector();
        if (inputConnector != null) {
            inputConnectors.put(inputConnector.getFieldId(),
                    new GraphBoxInputConnectorImpl<T>(inputConnector.getSide(),
                            new Supplier<Float>() {
                                @Override
                                public Float get() {
                                    return actor.getY() + footerTable.getHeight() + actor.getHeight() / 2f;
                                }
                            },
                            inputConnector.getFieldId()));
        }
        final GraphBoxOutputConnector<T> outputConnector = graphBoxPart.getOutputConnector();
        if (outputConnector != null) {
            outputConnectors.put(outputConnector.getFieldId(),
                    new GraphBoxOutputConnectorImpl<T>(outputConnector.getSide(),
                            new Supplier<Float>() {
                                @Override
                                public Float get() {
                                    return actor.getY() + footerTable.getHeight() + actor.getHeight() / 2f;
                                }
                            },
                            outputConnector.getFieldId()));
        }
    }

    public void removeGraphBoxPart(int index) {
        GraphBoxPart<T> graphBoxPart = graphBoxParts.get(index);
        graphBoxPart.getActor().remove();
        GraphBoxInputConnector<T> inputConnector = graphBoxPart.getInputConnector();
        if (inputConnector != null) {
            window.connectorRemoved(id, inputConnector.getFieldId());
            inputConnectors.remove(inputConnector.getFieldId());
        }
        GraphBoxOutputConnector<T> outputConnector = graphBoxPart.getOutputConnector();
        if (outputConnector != null) {
            window.connectorRemoved(id, outputConnector.getFieldId());
            outputConnectors.remove(outputConnector.getFieldId());
        }
        graphBoxParts.remove(index);
    }

    public void invalidate() {
        if (window != null) {
            window.setSize(Math.max(150, window.getPrefWidth()), window.getPrefHeight());
            window.validate();
            //Two times according to `pack` function
            window.setSize(Math.max(150, window.getPrefWidth()), window.getPrefHeight());
            window.validate();
        }
    }

    public void setSerializeCallback(GraphBoxSerializeCallback serializeCallback) {
        this.serializeCallback = serializeCallback;
    }

    @Override
    public Map<String, GraphBoxInputConnector<T>> getInputs() {
        return inputConnectors;
    }

    @Override
    public Map<String, GraphBoxOutputConnector<T>> getOutputs() {
        return outputConnectors;
    }

    @Override
    public void addToWindow(GraphContainer<T>.GraphBoxWindow window) {
        this.window = window;
        window.add(table).grow().row();
    }

    @Override
    public Window getWindow() {
        return window;
    }

    @Override
    public Actor getActor() {
        return table;
    }

    @Override
    public HashMap<String, String> getData() {
        HashMap<String, String> result = new HashMap<>();

        for (GraphBoxPart<T> graphBoxPart : graphBoxParts)
            graphBoxPart.serializePart(result);

        if (serializeCallback != null)
            serializeCallback.serializeBox(result);

        if (result.isEmpty())
            return null;
        return result;
    }

    @Override
    public void dispose() {
        for (GraphBoxPart<T> part : graphBoxParts) {
            part.dispose();
        }
    }
}
