package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class CircleActor extends Actor {
    private float radius;
    private final ShapeDrawer shapeDrawer;

    public CircleActor(ShapeDrawer drawer, float r) {
        shapeDrawer = drawer;
        radius = r;
        setSize(r * 2, r * 2);
        setOrigin(Align.center);
        setTouchable(Touchable.disabled);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float prevColor = shapeDrawer.setColor(getColor());
        shapeDrawer.circle(getX(), getY(), radius);
        shapeDrawer.setColor(prevColor);
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
