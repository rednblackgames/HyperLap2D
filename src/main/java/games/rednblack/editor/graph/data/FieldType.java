package games.rednblack.editor.graph.data;

public interface FieldType {
    boolean accepts(Object value);

    Object convert(Object value);

    String name();
}
