package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.command.TransformCommandBuilder;

public class CenterOriginPointCommand extends EntityModifyRevertibleCommand {

    private int entity;
    private final Vector2 backupOrigin = new Vector2();

    @Override
    public void doAction() {
        entity = notification.getBody();

        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        backupOrigin.set(transformComponent.originX, transformComponent.originY);

        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

        TransformCommandBuilder commandBuilder = new TransformCommandBuilder();
        commandBuilder.begin(entity, sandbox.getEngine());
        commandBuilder.setOrigin(dimensionsComponent.width / 2f, dimensionsComponent.height / 2f);
        commandBuilder.execute(HyperLap2DFacade.getInstance());
    }

    @Override
    public void undoAction() {
        TransformCommandBuilder commandBuilder = new TransformCommandBuilder();
        commandBuilder.begin(entity, sandbox.getEngine());
        commandBuilder.setOrigin(backupOrigin.x, backupOrigin.y);
        commandBuilder.execute(HyperLap2DFacade.getInstance());
    }
}
