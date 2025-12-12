package games.rednblack.editor.view.stage.tools.transformStrategy;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.h2d.common.command.TransformCommandBuilder;

public class LabelStrategy extends AbstractTransformStrategy {
    private static final float[] tmp1 = new float[3];
    private static final float[] tmp2 = new float[3];

    @Override
    public void calculate(float mouseDx, float mouseDy, int anchor, int entity, TransformCommandBuilder transformCommandBuilder, Vector2 mousePoint, float lastTransformAngle, float lastEntityAngle) {
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

        float[] horizontal = calculateSizeAndXyAmount(mouseDx, mouseDy, transformComponent.rotation, tmp1);
        float[] vertical = calculateSizeAndXyAmount(mouseDx, mouseDy, transformComponent.rotation + 90, tmp2);

        float visualDeltaW = horizontal[0];
        float visualDeltaH = vertical[0];

        float sX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
        float sY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);
        if (sX == 0) sX = 0.0001f;
        if (sY == 0) sY = 0.0001f;

        float localDeltaW = visualDeltaW / sX;
        float localDeltaH = visualDeltaH / sY;

        float newWidth = dimensionsComponent.width;
        float newHeight = dimensionsComponent.height;
        float newX = transformComponent.x;
        float newY = transformComponent.y;

        switch (anchor) {
            case NormalSelectionFollower.L:
                newWidth -= localDeltaW;
                newX += horizontal[1];
                newY += horizontal[2];
                break;
            case NormalSelectionFollower.R:
                newWidth += localDeltaW;
                break;
            case NormalSelectionFollower.T:
                newHeight += localDeltaH;
                break;
            case NormalSelectionFollower.B:
                newHeight -= localDeltaH;
                newX += vertical[1];
                newY += vertical[2];
                break;
            case NormalSelectionFollower.LT:
                newWidth -= localDeltaW;
                newHeight += localDeltaH;
                newX += horizontal[1];
                newY += horizontal[2];
                break;
            case NormalSelectionFollower.RT:
                newWidth += localDeltaW;
                newHeight += localDeltaH;
                break;
            case NormalSelectionFollower.RB:
                newWidth += localDeltaW;
                newHeight -= localDeltaH;
                newX += vertical[1];
                newY += vertical[2];
                break;
            case NormalSelectionFollower.LB:
                newWidth -= localDeltaW;
                newHeight -= localDeltaH;

                newX += horizontal[1];
                newY += horizontal[2];
                newX += vertical[1];
                newY += vertical[2];
                break;
        }

        transformComponent.x = newX;
        transformComponent.y = newY;
        dimensionsComponent.width = newWidth;
        dimensionsComponent.height = newHeight;

        transformCommandBuilder.setSize(newWidth, newHeight);
        transformCommandBuilder.setPos(newX, newY);

        // Origin
        origin(visualDeltaW, visualDeltaH, anchor, transformComponent, transformCommandBuilder);

        // Rotating
        rotating(anchor, transformCommandBuilder, mousePoint, lastTransformAngle, lastEntityAngle, transformComponent);
    }
}
