package games.rednblack.editor.graph.data;

import java.util.HashMap;

public interface GraphNode<T extends FieldType> {
    String getId();

    String getType();

    HashMap<String, String> getData();

    boolean isInputField(String fieldId);

    NodeConfiguration<T> getConfiguration();
}
