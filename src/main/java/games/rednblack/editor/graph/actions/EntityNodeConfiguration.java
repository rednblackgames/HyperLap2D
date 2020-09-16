package games.rednblack.editor.graph.actions;

import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Entity;

public class EntityNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public EntityNodeConfiguration() {
        super("Entity", "Entity", "Entity");

        addNodeOutput(
                new GraphNodeOutputImpl<>("entity", "Entity",
                        Entity));
    }
}
