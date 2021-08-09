package games.rednblack.editor.graph.config;

import games.rednblack.editor.graph.data.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class NodeConfigurationImpl<T extends FieldType> implements NodeConfiguration<T> {
    private String type;
    private String name;
    private String menuLocation;
    private Map<String, GraphNodeInput<T>> nodeInputs = new LinkedHashMap<>();
    private Map<String, GraphNodeOutput<T>> nodeOutputs = new LinkedHashMap<>();

    public NodeConfigurationImpl(String type, String name, String menuLocation) {
        this.type = type;
        this.name = name;
        this.menuLocation = menuLocation;
    }

    public void addNodeInput(GraphNodeInput<T> input) {
        nodeInputs.put(input.getFieldId(), input);
    }

    public void addNodeOutput(GraphNodeOutput<T> output) {
        nodeOutputs.put(output.getFieldId(), output);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMenuLocation() {
        return menuLocation;
    }

    @Override
    public Map<String, GraphNodeInput<T>> getNodeInputs() {
        return nodeInputs;
    }

    @Override
    public Map<String, GraphNodeOutput<T>> getNodeOutputs() {
        return nodeOutputs;
    }

    @Override
    public boolean isValid(Map<String, T> inputTypes, Iterable<? extends GraphProperty<T>> properties) {
        for (GraphNodeOutput<T> nodeOutput : nodeOutputs.values()) {
            T output = nodeOutput.determineFieldType(inputTypes);
            if (output == null)
                return false;
        }

        return true;
    }
}
