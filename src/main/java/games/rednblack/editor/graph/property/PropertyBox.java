package games.rednblack.editor.graph.property;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import games.rednblack.editor.graph.GraphBox;
import games.rednblack.editor.graph.data.FieldType;
import games.rednblack.editor.graph.data.GraphProperty;

public interface PropertyBox<T extends FieldType> extends GraphProperty<T>, Disposable {
    Actor getActor();

    GraphBox<T> createPropertyBox(Skin skin, String id, float x, float y);
}
