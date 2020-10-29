package games.rednblack.editor.graph.producer;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import games.rednblack.editor.graph.GraphBoxImpl;
import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.GraphNodeInput;
import games.rednblack.editor.graph.data.GraphNodeOutput;
import games.rednblack.editor.graph.data.NodeConfiguration;

import java.util.Iterator;
import java.util.Map;

public class GraphBoxProducerImpl<T extends FieldType> implements GraphBoxProducer<T> {
    protected NodeConfiguration<T> configuration;
    private boolean isUnique;

    public GraphBoxProducerImpl(NodeConfiguration<T> configuration) {
        this(configuration, false);
    }

    public GraphBoxProducerImpl(NodeConfiguration<T> configuration, boolean isUnique) {
        this.configuration = configuration;
        this.isUnique = isUnique;
    }

    @Override
    public String getType() {
        return configuration.getType();
    }

    @Override
    public boolean isCloseable() {
        return !isUnique;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public String getMenuLocation() {
        return configuration.getMenuLocation();
    }

    @Override
    public GraphBoxImpl<T> createPipelineGraphBox(Skin skin, String id, Map<String, String> data) {
        return createPipelineGraphBoxConfig(skin, id, configuration);
    }

    public GraphBoxImpl<T> createPipelineGraphBoxConfig(Skin skin, String id, NodeConfiguration<T> configuration) {
        GraphBoxImpl<T> start = new GraphBoxImpl<T>(id, configuration, skin);
        Iterator<GraphNodeInput<T>> inputIterator = configuration.getNodeInputs().values().iterator();
        Iterator<GraphNodeOutput<T>> outputIterator = configuration.getNodeOutputs().values().iterator();
        while (inputIterator.hasNext() || outputIterator.hasNext()) {
            GraphNodeInput<T> input = null;
            GraphNodeOutput<T> output = null;
            while (inputIterator.hasNext()) {
                input = inputIterator.next();
                if (input.isMainConnection()) {
                    start.addTopConnector(input);
                    input = null;
                } else {
                    break;
                }
            }
            while (outputIterator.hasNext()) {
                output = outputIterator.next();
                if (output.isMainConnection()) {
                    start.addBottomConnector(output);
                    output = null;
                } else {
                    break;
                }
            }

            if (input != null && output != null) {
                start.addTwoSideGraphPart(skin, input, output);
            } else if (input != null) {
                start.addInputGraphPart(skin, input);
            } else if (output != null) {
                start.addOutputGraphPart(skin, output);
            }
        }

        return start;
    }

    @Override
    public GraphBoxImpl<T> createDefault(Skin skin, String id) {
        return createPipelineGraphBox(skin, id, null);
    }

    @Override
    public boolean isUnique() {
        return isUnique;
    }
}

