package games.rednblack.editor.graph;

import games.rednblack.editor.graph.data.FieldType;

public interface GraphBoxInputConnector<T extends FieldType> {
    enum Side {
        Left, Top;
    }

    Side getSide();

    float getOffset();

    String getFieldId();
}
