package games.rednblack.editor.graph;

import com.badlogic.gdx.scenes.scene2d.Event;
import org.json.simple.JSONObject;

public class GetSerializedGraph extends Event {
    private String id;
    private JSONObject graph;

    public GetSerializedGraph(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public JSONObject getGraph() {
        return graph;
    }

    public void setGraph(JSONObject graph) {
        this.graph = graph;
    }
}
