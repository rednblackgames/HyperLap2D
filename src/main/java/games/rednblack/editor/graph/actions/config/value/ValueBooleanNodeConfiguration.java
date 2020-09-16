package games.rednblack.editor.graph.actions.config.value;

import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

public class ValueBooleanNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {
    public ValueBooleanNodeConfiguration() {
        super("ValueBoolean", "Boolean", "Value");
        addNodeOutput(
                new GraphNodeOutputImpl<ActionFieldType>("value", "Value", ActionFieldType.Boolean));
    }
}
