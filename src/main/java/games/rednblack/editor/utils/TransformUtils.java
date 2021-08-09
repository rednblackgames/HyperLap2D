package games.rednblack.editor.utils;

import com.badlogic.gdx.math.Matrix3;
import games.rednblack.editor.renderer.components.TransformComponent;

/**
 * Created by Sasun Poghosyan on 4/18/2016.
 */
public class TransformUtils {

    private static final Matrix3 tempMat = new Matrix3();


    public static Matrix3 scaleRotMat(TransformComponent transformComponent) {
        float translationX = transformComponent.x + transformComponent.originX;
        float translationY = transformComponent.y + transformComponent.originY;
        float scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
        float scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);
        float angle = transformComponent.rotation;
        tempMat.idt();
        tempMat.translate(translationX, translationY).rotate(angle).scale(scaleX, scaleY).translate(-translationX, -translationY);
        return tempMat;
    }

    public static Matrix3 scaleRotMat(float translationX, float translationY, float scaleX, float scaleY, float angle) {
        tempMat.idt();
        tempMat.translate(translationX, translationY).rotate(angle).scale(scaleX, scaleY).translate(-translationX, -translationY);
        return tempMat;
    }

    public static Matrix3 scalingMat(TransformComponent transformComponent) {
        float translationX = transformComponent.x + transformComponent.originX;
        float translationY = transformComponent.y + transformComponent.originY;
        float scaleX = transformComponent.scaleX * (transformComponent.flipX ? -1 : 1);
        float scaleY = transformComponent.scaleY * (transformComponent.flipY ? -1 : 1);
        tempMat.idt();
        tempMat.translate(translationX, translationY).scale(scaleX, scaleY).translate(-translationX, -translationY);
        return tempMat;
    }

    public static Matrix3 scalingMat(float translationX, float translationY, float scaleX, float scaleY) {
        tempMat.idt();
        tempMat.translate(translationX, translationY).scale(scaleX, scaleY).translate(-translationX, -translationY);
        return tempMat;
    }

    public static Matrix3 rotationMat(TransformComponent transformComponent) {
        float translationX = transformComponent.x + transformComponent.originX;
        float translationY = transformComponent.y + transformComponent.originY;
        float angle = transformComponent.rotation;
        tempMat.idt();
        tempMat.translate(translationX, translationY).rotate(angle).translate(-translationX, -translationY);
        return tempMat;
    }

    public static Matrix3 rotationMat(float translationX, float translationY, float angle) {
        tempMat.idt();
        tempMat.translate(translationX, translationY).rotate(angle).translate(-translationX, -translationY);
        return tempMat;
    }

    public static Matrix3 identity() {
        return tempMat.idt();
    }

    public static Matrix3 copy(Matrix3 matrix3) {
        Matrix3 copy = new Matrix3();
        copy.set(matrix3.getValues());
        return copy;
    }

}
