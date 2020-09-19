package games.rednblack.editor.graph.actions.config.value;

import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.Vector2;

public class ValueVector2NodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public ValueVector2NodeConfiguration() {
        super("ValueVector2", "Vector2", "Value");
        addNodeOutput(
                new GraphNodeOutputImpl<ActionFieldType>("value", "Value", Vector2));
    }
}
