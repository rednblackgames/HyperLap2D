package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.*;

public class RotateByActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public RotateByActionNodeConfiguration() {
        super("RotateByAction", "Rotate By", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("degree", "Degree", true, ActionFieldType.Float, Param));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("duration", "Duration", false, ActionFieldType.Float, Param));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("interpolation", "Interpolation", false, Interpolation, Param));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", Action));
    }
}
