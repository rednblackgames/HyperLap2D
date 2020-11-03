package games.rednblack.editor.graph.data;

public class NodeConnector {
    private String nodeId;
    private String fieldId;

    public NodeConnector() {

    }

    public NodeConnector(String nodeId, String fieldId) {
        this.nodeId = nodeId;
        this.fieldId = fieldId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeConnector that = (NodeConnector) o;

        if (!nodeId.equals(that.nodeId)) return false;
        return fieldId.equals(that.fieldId);
    }

    @Override
    public int hashCode() {
        int result = nodeId.hashCode();
        result = 31 * result + fieldId.hashCode();
        return result;
    }
}
