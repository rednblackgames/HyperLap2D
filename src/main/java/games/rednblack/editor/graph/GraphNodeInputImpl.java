package games.rednblack.editor.graph;

import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.GraphNodeInput;

import java.util.Arrays;
import java.util.List;

public class GraphNodeInputImpl<T extends FieldType> implements GraphNodeInput<T> {
    private String id;
    private String name;
    private List<? extends T> acceptedTypes;
    private boolean required;
    private boolean mainConnection;

    public GraphNodeInputImpl(String id, String name, T... acceptedType) {
        this(id, name, false, acceptedType);
    }

    public GraphNodeInputImpl(String id, String name, boolean required, T... acceptedType) {
        this(id, name, required, false, acceptedType);
    }

    public GraphNodeInputImpl(String id, String name, boolean required, boolean mainConnection, T... acceptedType) {
        this.id = id;
        this.name = name;
        this.required = required;
        this.mainConnection = mainConnection;
        this.acceptedTypes = Arrays.asList(acceptedType);
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isMainConnection() {
        return mainConnection;
    }

    @Override
    public String getFieldId() {
        return id;
    }

    @Override
    public String getFieldName() {
        return name;
    }

    @Override
    public List<? extends T> getAcceptedPropertyTypes() {
        return acceptedTypes;
    }
}
