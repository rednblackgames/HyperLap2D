package games.rednblack.editor.graph.data;

public interface Graph<T extends GraphNode<W>, U extends GraphConnection, V extends GraphProperty<W>, W extends FieldType> {
    T getNodeById(String id);

    V getPropertyByName(String name);

    Iterable<? extends U> getConnections();

    Iterable<? extends T> getNodes();

    Iterable<? extends V> getProperties();
}
