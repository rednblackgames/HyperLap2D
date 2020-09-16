package games.rednblack.editor.graph.property;

import com.badlogic.gdx.scenes.scene2d.Actor;
import org.json.simple.JSONObject;

public interface PropertyDefaultBox {
    Actor getActor();

    JSONObject serializeData();
}
