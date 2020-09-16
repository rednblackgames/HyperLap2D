package games.rednblack.editor.graph.actions.config.value;

import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

public class ValueFloatNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public ValueFloatNodeConfiguration() {
        super("ValueFloat", "Float", "Value");
        addNodeOutput(
                new GraphNodeOutputImpl<ActionFieldType>("value", "Value", ActionFieldType.Float));
    }
}

