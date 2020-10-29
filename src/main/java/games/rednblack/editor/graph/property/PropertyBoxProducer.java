package games.rednblack.editor.graph.property;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import games.rednblack.editor.graph.data.FieldType;

import java.util.Map;

public interface PropertyBoxProducer<T extends FieldType> {
    T getType();

    PropertyBox<T> createPropertyBox(Skin skin, String name, Map<String, Object> jsonObject);

    PropertyBox<T> createDefaultPropertyBox(Skin skin);
}
