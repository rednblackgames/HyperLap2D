package games.rednblack.editor.view.stage.tools.transformStrategy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.NinePatchComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.h2d.common.command.TransformCommandBuilder;

public class NinePatchStrategy extends AbstractTransformStrategy {

    private static final float[] tmp1 = new float[3];
    private static final float[] tmp2 = new float[3];

    @Override
    public void calculate(float mouseDx, float mouseDy, int anchor, int entity, TransformCommandBuilder transformCommandBuilder, Vector2 mousePointStage, float lastTransformAngle, float lastEntityAngle) {
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        NinePatchComponent ninePatchComponent = SandboxComponentRetriever.get(entity, NinePatchComponent.class);

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

        float minWidth = ninePatchComponent.ninePatch.getTotalWidth();
        float minHeight = ninePatchComponent.ninePatch.getTotalHeight();

        float currentWidth = dimensionsComponent.width;
        float currentHeight = dimensionsComponent.height;

        float diffW = 0;
        float diffH = 0;

        float anchorShiftX = 0;
        float anchorShiftY = 0;

        switch (anchor) {
            case NormalSelectionFollower.L:
                float targetW_L = currentWidth - localDeltaW;
                float finalW_L = Math.max(minWidth, targetW_L);
                diffW = finalW_L - currentWidth;
                anchorShiftX = -diffW;
                break;

            case NormalSelectionFollower.R:
                float targetW_R = currentWidth + localDeltaW;
                float finalW_R = Math.max(minWidth, targetW_R);
                diffW = finalW_R - currentWidth;

                anchorShiftX = 0;
                break;

            case NormalSelectionFollower.B:
                float targetH_B = currentHeight - localDeltaH;
                float finalH_B = Math.max(minHeight, targetH_B);
                diffH = finalH_B - currentHeight;

                anchorShiftY = -diffH;
                break;

            case NormalSelectionFollower.T:
                float targetH_T = currentHeight + localDeltaH;
                float finalH_T = Math.max(minHeight, targetH_T);
                diffH = finalH_T - currentHeight;
                anchorShiftY = 0;
                break;

            case NormalSelectionFollower.LT:
                // Width (L logic)
                float tW_LT = currentWidth - localDeltaW;
                float fW_LT = Math.max(minWidth, tW_LT);
                diffW = fW_LT - currentWidth;
                anchorShiftX = -diffW;

                // Height (T logic)
                float tH_LT = currentHeight + localDeltaH;
                float fH_LT = Math.max(minHeight, tH_LT);
                diffH = fH_LT - currentHeight;
                anchorShiftY = 0;
                break;

            case NormalSelectionFollower.RT:
                // Width (R logic)
                float tW_RT = currentWidth + localDeltaW;
                float fW_RT = Math.max(minWidth, tW_RT);
                diffW = fW_RT - currentWidth;
                anchorShiftX = 0;

                // Height (T logic)
                float tH_RT = currentHeight + localDeltaH;
                float fH_RT = Math.max(minHeight, tH_RT);
                diffH = fH_RT - currentHeight;
                anchorShiftY = 0;
                break;

            case NormalSelectionFollower.RB:
                // Width (R logic)
                float tW_RB = currentWidth + localDeltaW;
                float fW_RB = Math.max(minWidth, tW_RB);
                diffW = fW_RB - currentWidth;
                anchorShiftX = 0;

                // Height (B logic)
                float tH_RB = currentHeight - localDeltaH;
                float fH_RB = Math.max(minHeight, tH_RB);
                diffH = fH_RB - currentHeight;
                anchorShiftY = -diffH;
                break;

            case NormalSelectionFollower.LB:
                // Width (L logic)
                float tW_LB = currentWidth - localDeltaW;
                float fW_LB = Math.max(minWidth, tW_LB);
                diffW = fW_LB - currentWidth;
                anchorShiftX = -diffW;

                // Height (B logic)
                float tH_LB = currentHeight - localDeltaH;
                float fH_LB = Math.max(minHeight, tH_LB);
                diffH = fH_LB - currentHeight;
                anchorShiftY = -diffH;
                break;
        }

        dimensionsComponent.width = currentWidth + diffW;
        dimensionsComponent.height = currentHeight + diffH;

        if (anchorShiftX != 0 || anchorShiftY != 0) {
            float rot = transformComponent.rotation;
            float cos = MathUtils.cosDeg(rot);
            float sin = MathUtils.sinDeg(rot);

            float shiftVisX = anchorShiftX * sX;
            float shiftVisY = anchorShiftY * sY;

            float worldShiftX = shiftVisX * cos - shiftVisY * sin;
            float worldShiftY = shiftVisX * sin + shiftVisY * cos;

            transformComponent.x += worldShiftX;
            transformComponent.y += worldShiftY;
        }

        transformCommandBuilder.setPos(transformComponent.x, transformComponent.y);
        transformCommandBuilder.setSize(dimensionsComponent.width, dimensionsComponent.height);

        origin(visualDeltaW, visualDeltaH, anchor, transformComponent, transformCommandBuilder);

        // Rotating
        rotating(anchor, transformCommandBuilder, mousePointStage, lastTransformAngle, lastEntityAngle, transformComponent);
    }
}
