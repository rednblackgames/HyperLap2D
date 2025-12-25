package games.rednblack.editor.graph.data;

import com.badlogic.gdx.utils.ObjectMap;

public interface GraphNode<T extends FieldType> {
    String getId();

    String getType();

    ObjectMap<String, String> getData();

    boolean isInputField(String fieldId);

    NodeConfiguration<T> getConfiguration();
}
