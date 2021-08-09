package games.rednblack.editor.view.stage.tools.transformStrategy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.utils.RoundUtils;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.ui.followers.NormalSelectionFollower;
import games.rednblack.editor.view.ui.properties.panels.UIBasicItemPropertiesMediator;
import games.rednblack.h2d.common.command.TransformCommandBuilder;
import org.puremvc.java.patterns.facade.Facade;

/**
 * Created by Sasun Poghosyan on 4/13/2016.
 */
public class BasicStrategy extends AbstractTransformStrategy {

    private float deltaW;
    private float deltaH;

    private static final float[] tmp1 = new float[3];
    private static final float[] tmp2 = new float[3];

    private final Facade facade = HyperLap2DFacade.getInstance();

    @Override
    public void calculate(float mouseDx, float mouseDy, int anchor, int entity, TransformCommandBuilder transformCommandBuilder, Vector2 mousePointStage, float lastTransformAngle, float lastEntityAngle) {
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);
        DimensionsComponent dimensionsComponent = SandboxComponentRetriever.get(entity, DimensionsComponent.class);

        float scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
        float scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);

        float newWidth = dimensionsComponent.width * scaleX;
        float newHeight = dimensionsComponent.height * scaleY;

        float[] horizontal = calculateSizeAndXyAmount(mouseDx, mouseDy, transformComponent.rotation, tmp1);
        float[] vertical = calculateSizeAndXyAmount(mouseDx, mouseDy, transformComponent.rotation + 90, tmp2);

        deltaW = horizontal[0];
        deltaH = vertical[0];

        switch (anchor) {
            case NormalSelectionFollower.L:
                positionHorizontally(transformComponent, dimensionsComponent, horizontal, true);
                newWidth = dimensionsComponent.width * scaleX - deltaW;
                break;
            case NormalSelectionFollower.R:
                positionHorizontally(transformComponent, dimensionsComponent, horizontal, false);
                newWidth = dimensionsComponent.width * scaleX + deltaW;
                break;
            case NormalSelectionFollower.B:
                positionVertically(transformComponent, dimensionsComponent, vertical, true);
                newHeight = dimensionsComponent.height * scaleY - deltaH;
                break;
            case NormalSelectionFollower.T:
                positionVertically(transformComponent, dimensionsComponent, vertical, false);
                newHeight = dimensionsComponent.height * scaleY + deltaH;
                break;
            case NormalSelectionFollower.LT:
                positionHorizontally(transformComponent, dimensionsComponent, horizontal, true);
                positionVertically(transformComponent, dimensionsComponent, vertical, false);
                newWidth = dimensionsComponent.width * scaleX - deltaW;
                newHeight = dimensionsComponent.height * scaleY + deltaH;
                break;
            case NormalSelectionFollower.RT:
                positionHorizontally(transformComponent, dimensionsComponent, horizontal, false);
                positionVertically(transformComponent, dimensionsComponent, vertical, false);
                newWidth = dimensionsComponent.width * scaleX + deltaW;
                newHeight = dimensionsComponent.height * scaleY + deltaH;
                break;
            case NormalSelectionFollower.RB:
                positionHorizontally(transformComponent, dimensionsComponent, horizontal, false);
                positionVertically(transformComponent, dimensionsComponent, vertical, true);
                newWidth = dimensionsComponent.width * scaleX + deltaW;
                newHeight = dimensionsComponent.height * scaleY - deltaH;
                break;
            case NormalSelectionFollower.LB:
                positionHorizontally(transformComponent, dimensionsComponent, horizontal, true);
                positionVertically(transformComponent, dimensionsComponent, vertical, true);
                newWidth = dimensionsComponent.width * scaleX - deltaW;
                newHeight = dimensionsComponent.height * scaleY - deltaH;
                break;
        }

        // Origin
        origin(deltaW, deltaH, anchor, transformComponent, transformCommandBuilder);

        // Rotating
        rotating(anchor, transformCommandBuilder, mousePointStage, lastTransformAngle, lastEntityAngle, transformComponent);

        float newScaleX = newWidth / dimensionsComponent.width;
        float newScaleY = isShiftPressed() ? newScaleX : newHeight / dimensionsComponent.height;
        newScaleX *= (transformComponent.flipX ? -1 : 1);
        newScaleY *= (transformComponent.flipY ? -1 : 1);

        transformComponent.scaleX = newScaleX;
        transformComponent.scaleY = newScaleY;
        transformCommandBuilder.setScale(RoundUtils.round(newScaleX, 3), RoundUtils.round(newScaleY, 3));

        EntityUtils.refreshComponents(entity);
    }

    private void positionHorizontally(TransformComponent t, DimensionsComponent d, float[] horizontal, boolean inverse) {
        if (isShiftPressed()) {
            deltaW *= 2;
        } else {
            float originX = t.originX / d.width;
            originX = inverse ? 1f - originX : originX;
            float originY = t.originY / d.height;
            originY = inverse ? 1f - originY : originY;

            t.x += horizontal[1] * originX;
            t.y += horizontal[2] * originY;
        }
    }

    private void positionVertically(TransformComponent t, DimensionsComponent d, float[] vertical, boolean inverse) {
        if (isShiftPressed()) {
            deltaH *= 2;
        } else {
            float originX = t.originX / d.width;
            originX = inverse ? 1f - originX : originX;
            float originY = t.originY / d.height;
            originY = inverse ? 1f - originY : originY;

            t.x += vertical[1] * originX;
            t.y += vertical[2] * originY;
        }
    }

    private boolean isShiftPressed() {
        UIBasicItemPropertiesMediator mediator = facade.retrieveMediator(UIBasicItemPropertiesMediator.NAME);
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
                || (mediator != null && mediator.isXYScaleLinked());
    }
}

