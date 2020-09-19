package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Action;

public class RepeatActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public RepeatActionNodeConfiguration() {
        super("Action", "Repeat", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("count", "Count", true, ActionFieldType.Float));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("action", "Action", true, Action));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", Action));
    }
}