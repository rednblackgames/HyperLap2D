package games.rednblack.editor.view.ui.widget.actors.polygon;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Pool;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PolyLine extends Actor implements Pool.Poolable {
    private static final int CIRCLE_RADIUS = 5;

    private final Vector2 temp = new Vector2();
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
        updateBounds();
    }

    public void setPoint2(float x, float y) {
        point2.set(x, y);
        updateBounds();
    }

    public void setPoint1(Vector2 point) {
        point1.set(point);
        updateBounds();
    }

    public void setPoint2(Vector2 point) {
        point2.set(point);
        updateBounds();
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
        updateBounds();
    }

    public void scalePoints(float scaleX, float scaleY) {
        point1.scl(scaleX, scaleY);
        point2.scl(scaleX, scaleY);
        updateBounds();
    }

    public void offsetPoints(float offsetX, float offsetY) {
        point1.add(offsetX, offsetY);
        point2.add(offsetX, offsetY);
        updateBounds();
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    private void updateBounds() {
        setWidth(point1.dst(point2));
        setHeight(thickness);

        temp.set(point2).sub(point1);
        setRotation(temp.angleDeg());

        setPosition(point1.x, point1.y);
    }

    @Override
    public Actor hit(float x, float y, boolean touchable) {
        if (touchable && getTouchable() != Touchable.enabled) return null;
        if (!isVisible()) return null;
        return x >= -CIRCLE_RADIUS && x < getWidth() + CIRCLE_RADIUS && y >= -CIRCLE_RADIUS && y < getHeight() + CIRCLE_RADIUS ? this : null;
    }

    @Override
    public void reset() {
        clearListeners();

        thickness = 1f;
        point1.set(0, 0);
        point2.set(0, 0);

        setTouchable(Touchable.enabled);
    }
}
