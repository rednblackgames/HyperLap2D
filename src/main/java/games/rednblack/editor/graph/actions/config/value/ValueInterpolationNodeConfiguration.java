package games.rednblack.editor.graph.actions.config.value;

import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Interpolation;

public class ValueInterpolationNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public ValueInterpolationNodeConfiguration() {
        super("ValueInterpolation", "Interpolation", "Math");
        addNodeOutput(
                new GraphNodeOutputImpl<ActionFieldType>("value", "Value", Interpolation));
    }
}
