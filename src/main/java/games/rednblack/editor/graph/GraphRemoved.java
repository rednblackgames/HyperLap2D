package games.rednblack.editor.graph;

import com.badlogic.gdx.scenes.scene2d.Event;

public class GraphRemoved extends Event {
    private String id;

    public GraphRemoved(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
