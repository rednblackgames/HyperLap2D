package games.rednblack.editor.graph;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public abstract class GraphChangedListener implements EventListener {
    @Override
    public boolean handle(Event event) {
        if (!(event instanceof GraphChangedEvent)) return false;

        return graphChanged((GraphChangedEvent) event);
    }

    protected abstract boolean graphChanged(GraphChangedEvent event);
}
