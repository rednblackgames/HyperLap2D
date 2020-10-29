package games.rednblack.editor.graph.property;

import com.badlogic.gdx.scenes.scene2d.Actor;

import java.util.Map;

public interface PropertyDefaultBox {
    Actor getActor();

    Map<String, Object> serializeData();
}
