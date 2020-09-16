package games.rednblack.editor.graph.data;

import java.util.Collection;
import java.util.Map;

public interface GraphNodeOutput<T extends FieldType> {
    boolean isMainConnection();

    String getFieldName();

    String getFieldId();

    Collection<? extends T> getProducableFieldTypes();

    T determineFieldType(Map<String, T> inputs);

    boolean supportsMultiple();
}
