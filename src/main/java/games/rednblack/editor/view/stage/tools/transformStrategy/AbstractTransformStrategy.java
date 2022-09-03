package games.rednblack.editor.view.stage.tools.transformStrategy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.MathUtilsFix;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.h2d.common.command.TransformCommandBuilder;

/**
 * Created by Sasun Poghosyan on 4/14/2016.
 */
public abstract class AbstractTransformStrategy implements ITransformStrategy {


    /**
     * on every anchor drag calculates width/height, x and y amounts.
     *
     * @param mouseDeltaX mouse delta on x axis
     * @param mouseDeltaY mouse delta on y axis
     * @param rotation    entity rotation. If you want to find vertical anchors drag value add 90 to {@param rotation}
     * @return array of three floats (new float[]{width/height, xComponent, yComponent};)
     */
    float[] calculateSizeAndXyAmount(float mouseDeltaX, float mouseDeltaY, float rotation, float[] result) {
        float mouseDragAngle = MathUtilsFix.atan2(mouseDeltaY, mouseDeltaX) * MathUtils.radDeg;
        float deltaA = rotation - mouseDragAngle;
        float c = (float) Math.sqrt(mouseDeltaX * mouseDeltaX + mouseDeltaY * mouseDeltaY);
        float a = c * MathUtils.cosDeg(deltaA);
        float xComponent = a * MathUtils.cosDeg(rotation);
        float yComponent = a * MathUtils.sinDeg(rotation);

        result[0] = a;
        result[1] = xComponent;
        result[2] = yComponent;
        return  result;
    }

    void rotating(int anchor, TransformCommandBuilder transformCommandBuilder, Vector2 mousePointStage, float lastTransformAngle, float lastEntityAngle, TransformComponent transformComponent) {
        if (anchor >= NormalSelectionFollower.ROTATION_LT && anchor <= NormalSelectionFollower.ROTATION_LB) {
            mousePointStage.sub(transformComponent.x + transformComponent.originX, transformComponent.y + transformComponent.originY);
            float currentAngle = mousePointStage.angleDeg();
            float angleDiff = currentAngle - lastTransformAngle;
            float newRotation = lastEntityAngle + angleDiff;
            transformComponent.rotation = newRotation;
            transformCommandBuilder.setRotation(RoundUtils.round(newRotation, 2));
        }
    }

    void origin(float mouseDx, float mouseDy, int anchor, TransformComponent transformComponent, TransformCommandBuilder transformCommandBuilder) {
        if (anchor == NormalSelectionFollower.ORIGIN) {
            float newOriginX = transformComponent.originX;
            float newOriginY = transformComponent.originY;

            newOriginX = newOriginX + mouseDx;
            newOriginY = newOriginY + mouseDy;

            transformComponent.originX = newOriginX;
            transformComponent.originY = newOriginY;

            transformCommandBuilder.setOrigin(RoundUtils.round(newOriginX, 2), RoundUtils.round(newOriginY, 2));
        }
    }
}
