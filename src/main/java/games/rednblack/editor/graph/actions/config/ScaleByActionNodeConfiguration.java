package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Action;
import static games.rednblack.editor.graph.actions.ActionFieldType.Interpolation;

public class ScaleByActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public ScaleByActionNodeConfiguration() {
        super("Action", "Scale By", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("scale", "Scale", true, ActionFieldType.Vector2));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("duration", "Duration", false, ActionFieldType.Float));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("interpolation", "Interpolation", false, Interpolation));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", Action));
    }
}
