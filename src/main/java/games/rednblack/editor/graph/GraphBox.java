package games.rednblack.editor.graph;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Disposable;
import games.rednblack.editor.graph.data.*;

import java.util.Map;

public interface GraphBox<T extends FieldType> extends GraphNode<T>, Disposable {
    Actor getActor();

    Map<String, GraphBoxInputConnector<T>> getInputs();

    Map<String, GraphBoxOutputConnector<T>> getOutputs();

    void addToWindow(GraphContainer<T>.GraphBoxWindow window);

    Window getWindow();

    void graphChanged(GraphChangedEvent event, boolean hasErrors, Graph<? extends GraphNode<T>, ? extends GraphConnection, ? extends GraphProperty<T>, T> graph);
}
