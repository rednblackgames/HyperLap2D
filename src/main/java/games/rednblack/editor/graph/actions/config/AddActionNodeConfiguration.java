package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Action;
import static games.rednblack.editor.graph.actions.ActionFieldType.Entity;

public class AddActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public AddActionNodeConfiguration() {
        super("Function", "Add Action", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("action", "Action", true, Action));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("entity", "Entity", true, Entity));
    }
}