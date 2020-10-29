package games.rednblack.editor.graph.data;

import java.util.Map;

public interface GraphProperty<T extends FieldType> {
    String getName();

    T getType();

    Map<String, Object> getData();
}
