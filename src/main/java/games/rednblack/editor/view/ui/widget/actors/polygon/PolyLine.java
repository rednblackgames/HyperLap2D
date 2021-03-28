package games.rednblack.editor.view.ui.widget.actors.polygon;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Pool;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PolyLine extends Actor implements Pool.Poolable {

    private static final int CIRCLE_RADIUS = 10;
    private static final Vector2 tmpVector = new Vector2();

    private final ShapeDrawer shapeDrawer;
    private final Vector2 point1, point2;
    private float thickness = 1f;

    private int index = -1;

    public PolyLine(ShapeDrawer shapeDrawer) {
        this.shapeDrawer = shapeDrawer;
        point1 = new Vector2();
        point2 = new Vector2();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float a = getColor().a;
        getColor().a *= parentAlpha;
        shapeDrawer.line(point1, point2, getColor(), thickness);
        getColor().a = a;
    }

    public void setPoint1(float x, float y) {
        point1.set(x, y);
    }

    public void setPoint2(float x, float y) {
        point2.set(x, y);
    }

    public void setPoint1(Vector2 point) {
        point1.set(point);
    }

    public void setPoint2(Vector2 point) {
        point2.set(point);
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }

    public void scalePoints(float scaleX, float scaleY) {
        point1.scl(scaleX, scaleY);
        point2.scl(scaleX, scaleY);
    }

    public void offsetPoints(float offsetX, float offsetY) {
        point1.add(offsetX, offsetY);
        point2.add(offsetX, offsetY);
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (touchable && getTouchable() != Touchable.enabled) return null;
        if (!isVisible()) return null;

        tmpVector.set(x, y);

        float circleSqr = CIRCLE_RADIUS * CIRCLE_RADIUS;

        return Intersector.intersectSegmentCircle(point1, point2, tmpVector, circleSqr) ? this : null;
    }

    @Override
    public void reset() {
        clearListeners();

        thickness = 1f;
        point1.set(0, 0);
        point2.set(0, 0);
    }
}
