package games.rednblack.editor.graph;

import games.rednblack.editor.graph.data.GraphConnection;

public class GraphConnectionImpl implements GraphConnection {
    private String nodeFrom;
    private String fieldFrom;
    private String nodeTo;
    private String fieldTo;

    public GraphConnectionImpl(String nodeFrom, String fieldFrom, String nodeTo, String fieldTo) {
        this.nodeFrom = nodeFrom;
        this.fieldFrom = fieldFrom;
        this.nodeTo = nodeTo;
        this.fieldTo = fieldTo;
    }

    @Override
    public String getNodeFrom() {
        return nodeFrom;
    }

    public String getFieldFrom() {
        return fieldFrom;
    }

    public String getNodeTo() {
        return nodeTo;
    }

    public String getFieldTo() {
        return fieldTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphConnectionImpl that = (GraphConnectionImpl) o;

        if (!nodeFrom.equals(that.nodeFrom)) return false;
        if (!fieldFrom.equals(that.fieldFrom)) return false;
        if (!nodeTo.equals(that.nodeTo)) return false;
        return fieldTo.equals(that.fieldTo);
    }

    @Override
    public int hashCode() {
        int result = nodeFrom.hashCode();
        result = 31 * result + fieldFrom.hashCode();
        result = 31 * result + nodeTo.hashCode();
        result = 31 * result + fieldTo.hashCode();
        return result;
    }
}
