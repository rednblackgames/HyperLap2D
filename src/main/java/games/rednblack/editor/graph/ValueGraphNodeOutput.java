package games.rednblack.editor.graph;

import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.GraphNodeOutput;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ValueGraphNodeOutput<T extends FieldType> implements GraphNodeOutput<T> {
    private String fieldName;
    private T fieldType;

    public ValueGraphNodeOutput(String fieldName, T fieldType) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
    }

    @Override
    public boolean isMainConnection() {
        return false;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String getFieldId() {
        return "value";
    }

    @Override
    public Collection<? extends T> getProducableFieldTypes() {
        return Collections.singleton(fieldType);
    }

    @Override
    public boolean supportsMultiple() {
        return true;
    }

    @Override
    public T determineFieldType(Map<String, T> inputs) {
        return fieldType;
    }
}
