package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.*;

public class AlphaActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public AlphaActionNodeConfiguration() {
        super("Action", "Alpha", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("alpha", "Alpha", true, Float));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("duration", "Duration", false, Float));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("interpolation", "Interpolation", false, Interpolation));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", Action));
    }
}
