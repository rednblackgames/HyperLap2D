package games.rednblack.editor.graph;

import com.badlogic.gdx.scenes.scene2d.Event;
import org.json.simple.JSONObject;

public class RequestGraphOpen extends Event {
    private String id;
    private JSONObject jsonObject;

    public RequestGraphOpen(String id, JSONObject jsonObject) {
        this.id = id;
        this.jsonObject = jsonObject;
    }

    public String getId() {
        return id;
    }

    public JSONObject getJsonObject() {
        return jsonObject;
    }
}
