package games.rednblack.editor.graph.producer;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import games.rednblack.editor.graph.GraphBox;
import games.rednblack.editor.graph.data.FieldType;

import java.util.Map;

public interface GraphBoxProducer<T extends FieldType> {
    String getType();

    boolean isCloseable();

    String getName();

    String getMenuLocation();

    GraphBox<T> createPipelineGraphBox(Skin skin, String id, Map<String, String> data);

    GraphBox<T> createDefault(Skin skin, String id);

    boolean isUnique();
}
