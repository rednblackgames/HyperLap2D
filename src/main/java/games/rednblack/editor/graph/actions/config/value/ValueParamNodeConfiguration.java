package games.rednblack.editor.graph.actions.config.value;

import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

public class ValueParamNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public ValueParamNodeConfiguration() {
        super("ValueParam", "Param", "Value");
        addNodeOutput(
                new GraphNodeOutputImpl<ActionFieldType>("value", "Value", ActionFieldType.Param));
    }
}