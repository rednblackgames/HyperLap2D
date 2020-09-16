package games.rednblack.editor.graph.data;

import java.util.Map;

public interface NodeConfiguration<T extends FieldType> {
    String getType();

    String getName();

    String getMenuLocation();

    Map<String, GraphNodeInput<T>> getNodeInputs();

    Map<String, GraphNodeOutput<T>> getNodeOutputs();

    boolean isValid(Map<String, T> inputTypes, Iterable<? extends GraphProperty<T>> properties);
}
