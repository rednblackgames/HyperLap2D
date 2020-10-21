package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Action;
import static games.rednblack.editor.graph.actions.ActionFieldType.Param;

public class RepeatActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public RepeatActionNodeConfiguration() {
        super("RepeatAction", "Repeat", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("count", "Count", true, ActionFieldType.Float, Param));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("action0", "Action", true, Action));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", Action));
    }
}