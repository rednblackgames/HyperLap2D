package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Action;

public class ForeverActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public ForeverActionNodeConfiguration() {
        super("ForeverAction", "Forever", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("action0", "Action", true, Action));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", Action));
    }
}
