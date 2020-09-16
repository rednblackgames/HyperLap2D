package games.rednblack.editor.graph;

import games.rednblack.editor.graph.data.FieldType;

public interface GraphBoxOutputConnector<T extends FieldType> {
    enum Side {
        Right, Bottom;
    }

    Side getSide();

    float getOffset();

    String getFieldId();
}
