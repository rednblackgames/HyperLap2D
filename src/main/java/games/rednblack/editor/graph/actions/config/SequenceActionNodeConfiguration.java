package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.SameTypeOutputTypeFunction;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Action;
import static games.rednblack.editor.graph.actions.ActionFieldType.Sequence;

public class SequenceActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public SequenceActionNodeConfiguration() {
        super("SequenceAction", "Sequence", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("action0", "Action 0", true, Action));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("action1", "Action 1", true, Action));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", new SameTypeOutputTypeFunction<>("action0"), Action, Sequence));
    }
}
