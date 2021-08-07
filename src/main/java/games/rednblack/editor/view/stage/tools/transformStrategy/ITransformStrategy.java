package games.rednblack.editor.view.stage.tools.transformStrategy;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.h2d.common.command.TransformCommandBuilder;

/**
 * Created by Sasun Poghosyan on 4/13/2016.
 */
public interface ITransformStrategy {
    void calculate(float mouseDx, float mouseDy, int anchor, int entity, TransformCommandBuilder transformCommandBuilder, Vector2 mousePoint, float lastTransformAngle, float lastEntityAngle);
}
