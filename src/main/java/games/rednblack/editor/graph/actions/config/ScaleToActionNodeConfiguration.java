package games.rednblack.editor.graph.actions.config;

import games.rednblack.editor.graph.GraphNodeInputImpl;
import games.rednblack.editor.graph.GraphNodeOutputImpl;
import games.rednblack.editor.graph.actions.ActionFieldType;
import games.rednblack.editor.graph.config.NodeConfigurationImpl;

import static games.rednblack.editor.graph.actions.ActionFieldType.*;

public class ScaleToActionNodeConfiguration extends NodeConfigurationImpl<ActionFieldType> {

    public ScaleToActionNodeConfiguration() {
        super("ScaleToAction", "Scale To", "Action");

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("scale", "Scale", true, ActionFieldType.Vector2, Param));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("duration", "Duration", false, ActionFieldType.Float, Param));

        addNodeInput(
                new GraphNodeInputImpl<ActionFieldType>("interpolation", "Interpolation", false, Interpolation, Param));

        addNodeOutput(
                new GraphNodeOutputImpl<>("action", "Action", Action));
    }
}
