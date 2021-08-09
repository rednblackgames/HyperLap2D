package games.rednblack.editor.graph.data;

import java.util.*;

public class GraphValidator<T extends GraphNode<W>, U extends GraphConnection, V extends GraphProperty<W>, W extends FieldType> {
    public ValidationResult<T, U, V, W> validateGraph(Graph<T, U, V, W> graph, String nodeEnd) {
        ValidationResult<T, U, V, W> result = new ValidationResult<>();

        T end = graph.getNodeById(nodeEnd);
        if (end == null)
            return result;

        // Check duplicate property names
        if (graph.getProperties() != null) {
            Map<String, V> propertyNames = new HashMap<>();
            for (V property : graph.getProperties()) {
                String propertyName = property.getName();
                if (propertyNames.containsKey(propertyName)) {
                    result.addErrorProperty(property);
                    result.addErrorProperty(propertyNames.get(propertyName));
                }
                propertyNames.put(propertyName, property);
            }
        }

        boolean cyclic = isCyclic(result, graph, nodeEnd);
        if (!cyclic) {
            // Do other Validation
            validateNode(result, graph, nodeEnd, new HashMap<String, NodeOutputs<W>>());
        }
        return result;
    }

    private NodeOutputs<W> validateNode(ValidationResult<T, U, V, W> result, Graph<T, U, V, W> graph, String nodeId,
                                        Map<String, NodeOutputs<W>> nodeOutputs) {
        // Check if already validated
        NodeOutputs<W> outputs = nodeOutputs.get(nodeId);
        if (outputs != null)
            return outputs;

        T thisNode = graph.getNodeById(nodeId);
        Set<String> validatedFields = new HashSet<>();
        Map<String, W> inputsTypes = new HashMap<>();
        for (U incomingConnection : getIncomingConnections(graph, nodeId)) {
            String fieldTo = incomingConnection.getFieldTo();
            GraphNodeInput<W> input = thisNode.getConfiguration().getNodeInputs().get(fieldTo);
            T remoteNode = graph.getNodeById(incomingConnection.getNodeFrom());
            GraphNodeOutput<W> output = remoteNode.getConfiguration().getNodeOutputs().get(incomingConnection.getFieldFrom());

            // Validate the actual output is accepted by the input
            List<? extends W> acceptedPropertyTypes = input.getAcceptedPropertyTypes();
            if (!outputAcceptsPropertyType(output, acceptedPropertyTypes)) {
                result.addErrorConnection(incomingConnection);
            }

            validatedFields.add(fieldTo);
            NodeOutputs<W> outputFromRemoteNode = validateNode(result, graph, incomingConnection.getNodeFrom(), nodeOutputs);
            inputsTypes.put(fieldTo, outputFromRemoteNode.outputs.get(incomingConnection.getFieldFrom()));
        }

        for (GraphNodeInput<W> input : thisNode.getConfiguration().getNodeInputs().values()) {
            if (input.isRequired() && !validatedFields.contains(input.getFieldId())) {
                result.addErrorConnector(new NodeConnector(nodeId, input.getFieldId()));
            }
        }

        boolean valid = thisNode.getConfiguration().isValid(inputsTypes, graph.getProperties());
        if (!valid)
            result.addErrorNode(thisNode);

        Map<String, W> nodeOutputMap = new HashMap<>();
        for (GraphNodeOutput<W> value : thisNode.getConfiguration().getNodeOutputs().values()) {
            nodeOutputMap.put(value.getFieldId(), value.determineFieldType(inputsTypes));
        }

        NodeOutputs<W> nodeOutput = new NodeOutputs<>(nodeOutputMap);
        nodeOutputs.put(nodeId, nodeOutput);
        return nodeOutput;
    }

    private Iterable<U> getIncomingConnections(Graph<T, U, V, W> graph, String nodeId) {
        List<U> result = new LinkedList<>();
        for (U connection : graph.getConnections()) {
            if (connection.getNodeTo().equals(nodeId))
                result.add(connection);
        }
        return result;
    }

    private boolean outputAcceptsPropertyType(GraphNodeOutput<? extends FieldType> output, List<? extends FieldType> acceptedPropertyTypes) {
        Collection<? extends FieldType> producablePropertyTypes = output.getProducableFieldTypes();
        for (FieldType acceptedFieldType : acceptedPropertyTypes) {
            if (producablePropertyTypes.contains(acceptedFieldType))
                return true;
        }
        return false;
    }

    // This function is a variation of DFSUtil() in
    // https://www.geeksforgeeks.org/archives/18212
    private boolean isCyclicUtil(ValidationResult<T, U, V, W> validationResult, Graph<T, U, V, W> graph, String nodeId, Set<String> visited,
                                 Set<String> recStack) {
        // Mark the current node as visited and
        // part of recursion stack
        if (recStack.contains(nodeId)) {
            validationResult.addErrorNode(graph.getNodeById(nodeId));
            return true;
        }

        if (visited.contains(nodeId))
            return false;

        visited.add(nodeId);
        recStack.add(nodeId);

        Set<String> connectedNodes = new HashSet<>();
        for (U incomingConnection : getIncomingConnections(graph, nodeId)) {
            connectedNodes.add(incomingConnection.getNodeFrom());
        }

        for (String connectedNode : connectedNodes) {
            if (isCyclicUtil(validationResult, graph, connectedNode, visited, recStack)) {
                return true;
            }
        }
        recStack.remove(nodeId);

        return false;
    }

    private boolean isCyclic(ValidationResult<T, U, V, W> validationResult, Graph<T, U, V, W> graph, String start) {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        // Call the recursive helper function to
        // detect cycle in different DFS trees
        if (isCyclicUtil(validationResult, graph, start, visited, recStack)) {
            return true;
        }

        for (T node : graph.getNodes()) {
            String nodeId = node.getId();
            if (!visited.contains(nodeId)) {
                validationResult.addWarningNode(node);
            }
        }
        return false;
    }

    private static class NodeOutputs<W> {
        private Map<String, W> outputs;

        public NodeOutputs(Map<String, W> outputs) {
            this.outputs = outputs;
        }
    }

    public static class ValidationResult<T extends GraphNode<W>, U extends GraphConnection, V extends GraphProperty<W>, W extends FieldType> {
        private final Set<T> errorNodes = new HashSet<>();
        private final Set<T> warningNodes = new HashSet<>();
        private final Set<U> errorConnections = new HashSet<>();
        private final Set<NodeConnector> errorConnectors = new HashSet<>();
        private final Set<V> errorProperties = new HashSet<>();

        public void addErrorNode(T node) {
            errorNodes.add(node);
        }

        public void addWarningNode(T node) {
            warningNodes.add(node);
        }

        public void addErrorConnection(U connection) {
            errorConnections.add(connection);
        }

        public void addErrorConnector(NodeConnector nodeConnector) {
            errorConnectors.add(nodeConnector);
        }

        public void addErrorProperty(V property) {
            errorProperties.add(property);
        }

        public Set<T> getErrorNodes() {
            return errorNodes;
        }

        public Set<T> getWarningNodes() {
            return warningNodes;
        }

        public Set<U> getErrorConnections() {
            return errorConnections;
        }

        public Set<NodeConnector> getErrorConnectors() {
            return errorConnectors;
        }

        public Set<V> getErrorProperties() {
            return errorProperties;
        }

        public boolean hasErrors() {
            return !errorNodes.isEmpty() || !errorConnections.isEmpty() || !errorConnectors.isEmpty() || !errorProperties.isEmpty();
        }

        public boolean hasWarnings() {
            return !warningNodes.isEmpty();
        }
    }
}
