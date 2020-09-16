package games.rednblack.editor.graph.data;

import org.json.simple.JSONObject;

public interface GraphNode<T extends FieldType> {
    String getId();

    String getType();

    JSONObject getData();

    boolean isInputField(String fieldId);

    NodeConfiguration<T> getConfiguration();
}
