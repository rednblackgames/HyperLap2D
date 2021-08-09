package games.rednblack.editor.view.stage.tools.transformStrategy;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.h2d.common.command.TransformCommandBuilder;

public class LabelStrategy extends AbstractTransformStrategy {

    @Override
    public void calculate(float mouseDx, float mouseDy, int anchor, int entity, TransformCommandBuilder transformCommandBuilder, Vector2 mousePoint, float lastTransformAngle, float lastEntityAngle) {
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

        float newX = transformComponent.x;
        float newY = transformComponent.y;
        float newWidth = dimensionsComponent.width;
        float newHeight = dimensionsComponent.height;

        switch (anchor) {
            case NormalSelectionFollower.LB:
                newX = newX + mouseDx;
                newY = newY + mouseDy;
                newWidth = newWidth - mouseDx;
                newHeight = newHeight - mouseDy;
                break;
            case NormalSelectionFollower.L:
                newX = newX + mouseDx;
                newWidth = newWidth - mouseDx;
                break;
            case NormalSelectionFollower.LT:
                newX = newX + mouseDx;
                newWidth = newWidth - mouseDx;
                newHeight = newHeight + mouseDy;
                break;
            case NormalSelectionFollower.T:
                newHeight = newHeight + mouseDy;
                break;
            case NormalSelectionFollower.B:
                newY = newY + mouseDy;
                newHeight = newHeight - mouseDy;
                break;
            case NormalSelectionFollower.RB:
                newY = newY + mouseDy;
                newWidth = newWidth + mouseDx;
                newHeight = newHeight - mouseDy;
                break;
            case NormalSelectionFollower.R:
                newWidth = newWidth + mouseDx;
                break;
            case NormalSelectionFollower.RT:
                newHeight = newHeight + mouseDy;
                newWidth = newWidth + mouseDx;
                break;
        }

        transformComponent.x = newX;
        transformComponent.y = newY;
        dimensionsComponent.width = newWidth;
        dimensionsComponent.height = newHeight;

        transformCommandBuilder.setSize(newWidth, newHeight);
        transformCommandBuilder.setPos(newX, newY);

        // Origin
        origin(mouseDx, mouseDy, anchor, transformComponent, transformCommandBuilder);

        // Rotating
        rotating(anchor, transformCommandBuilder, mousePoint, lastTransformAngle, lastEntityAngle, transformComponent);
    }
}
