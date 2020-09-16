package games.rednblack.editor.graph;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.Graph;
import games.rednblack.editor.graph.data.GraphConnection;
import games.rednblack.editor.graph.data.GraphNode;
import games.rednblack.editor.graph.data.GraphNodeInput;
import games.rednblack.editor.graph.data.GraphNodeOutput;
import games.rednblack.editor.graph.data.GraphProperty;
import games.rednblack.editor.graph.data.NodeConfiguration;
import com.google.common.base.Supplier;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GraphBoxImpl<T extends FieldType> implements GraphBox<T> {
    private String id;
    private NodeConfiguration<T> configuration;
    private Table table;
    private List<GraphBoxPart<T>> graphBoxParts = new LinkedList<>();
    private Map<String, GraphBoxInputConnector<T>> inputConnectors = new HashMap<>();
    private Map<String, GraphBoxOutputConnector<T>> outputConnectors = new HashMap<>();

    public GraphBoxImpl(String id, NodeConfiguration<T> configuration, Skin skin) {
        this.id = id;
        this.configuration = configuration;
        table = new Table(skin);
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

    public void addGraphBoxPart(GraphBoxPart<T> graphBoxPart) {
        graphBoxParts.add(graphBoxPart);
        final Actor actor = graphBoxPart.getActor();
        table.add(actor).growX().row();
        final GraphBoxInputConnector<T> inputConnector = graphBoxPart.getInputConnector();
        if (inputConnector != null) {
            inputConnectors.put(inputConnector.getFieldId(),
                    new GraphBoxInputConnectorImpl<T>(inputConnector.getSide(),
                            new Supplier<Float>() {
                                @Override
                                public Float get() {
                                    return actor.getY() + actor.getHeight() / 2f;
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
                                    return actor.getY() + actor.getHeight() / 2f;
                                }
                            },
                            outputConnector.getFieldId()));
        }
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
    public Actor getActor() {
        return table;
    }

    @Override
    public JSONObject getData() {
        JSONObject result = new JSONObject();

        for (GraphBoxPart<T> graphBoxPart : graphBoxParts)
            graphBoxPart.serializePart(result);

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
