package games.rednblack.editor.view.stage.tools.transformStrategy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.h2d.common.TransformCommandBuilder;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;

/**
 * Created by Sasun Poghosyan on 4/13/2016.
 */
public class BasicStrategy extends AbstractTransformStrategy {

    private float deltaW;
    private float deltaH;

    @Override
    public void calculate(float mouseDx, float mouseDy, int anchor, Entity entity, TransformCommandBuilder transformCommandBuilder, Vector2 mousePointStage, float lastTransformAngle, float lastEntityAngle) {
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);

        float newWidth = dimensionsComponent.width * transformComponent.scaleX;
        float newHeight = dimensionsComponent.height * transformComponent.scaleY;

        float[] horizontal = calculateSizeAndXyAmount(mouseDx, mouseDy, transformComponent.rotation);
        float[] vertical = calculateSizeAndXyAmount(mouseDx, mouseDy, transformComponent.rotation + 90);

        deltaW = horizontal[0];
        deltaH = vertical[0];

        switch (anchor) {
            case NormalSelectionFollower.L:
                positionHorizontally(transformComponent, horizontal);
                newWidth = dimensionsComponent.width * transformComponent.scaleX - deltaW;
                break;
            case NormalSelectionFollower.R:
                positionHorizontally(transformComponent, horizontal);
                newWidth = dimensionsComponent.width * transformComponent.scaleX + deltaW;
                break;
            case NormalSelectionFollower.B:
                positionVertically(transformComponent, vertical);
                newHeight = dimensionsComponent.height * transformComponent.scaleY - deltaH;
                break;
            case NormalSelectionFollower.T:
                positionVertically(transformComponent, vertical);
                newHeight = dimensionsComponent.height * transformComponent.scaleY + deltaH;
                break;
            case NormalSelectionFollower.LT:
                positionItem(transformComponent, horizontal, vertical);
                newWidth = dimensionsComponent.width * transformComponent.scaleX - deltaW;
                newHeight = dimensionsComponent.height * transformComponent.scaleY + deltaH;
                break;
            case NormalSelectionFollower.RT:
                positionItem(transformComponent, horizontal, vertical);
                newWidth = dimensionsComponent.width * transformComponent.scaleX + deltaW;
                newHeight = dimensionsComponent.height * transformComponent.scaleY + deltaH;
                break;
            case NormalSelectionFollower.RB:
                positionItem(transformComponent, horizontal, vertical);
                newWidth = dimensionsComponent.width * transformComponent.scaleX + deltaW;
                newHeight = dimensionsComponent.height * transformComponent.scaleY - deltaH;
                break;
            case NormalSelectionFollower.LB:
                positionItem(transformComponent, horizontal, vertical);
                newWidth = dimensionsComponent.width * transformComponent.scaleX - deltaW;
                newHeight = dimensionsComponent.height * transformComponent.scaleY - deltaH;
                break;
        }

        // Origin
        origin(deltaW, deltaH, anchor, transformComponent, transformCommandBuilder);

        // Rotating
        rotating(anchor, transformCommandBuilder, mousePointStage, lastTransformAngle, lastEntityAngle, transformComponent);

        float newScaleX = newWidth / dimensionsComponent.width;
        float newScaleY = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? newScaleX : newHeight / dimensionsComponent.height;
        transformComponent.scaleX = RoundUtils.round(newScaleX, 2);
        transformComponent.scaleY = RoundUtils.round(newScaleY, 2);
        transformCommandBuilder.setScale(newScaleX, newScaleY);

        EntityUtils.refreshComponents(entity);
    }

    private void positionItem(TransformComponent transformComponent, float[] horizontal, float[] vertical) {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            deltaW *= 2;
            deltaH *= 2;
        } else {
            transformComponent.x += horizontal[1] * 0.5f;
            transformComponent.y += horizontal[2] * 0.5f;
            transformComponent.x += vertical[1] * 0.5f;
            transformComponent.y += vertical[2] * 0.5f;
        }
    }

    private void positionHorizontally(TransformComponent transformComponent, float[] horizontal) {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            deltaW *= 2;
        } else {
            transformComponent.x += horizontal[1] * 0.5f;
            transformComponent.y += horizontal[2] * 0.5f;
        }
    }

    private void positionVertically(TransformComponent transformComponent, float[] vertical) {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            deltaH *= 2;
        } else {
            transformComponent.x += vertical[1] * 0.5f;
            transformComponent.y += vertical[2] * 0.5f;
        }
    }
}

