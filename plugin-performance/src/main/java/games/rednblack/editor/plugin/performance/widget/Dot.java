package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** A coloured mark that carries a severity without spending a word on it. */
public class Dot extends Widget {

    private final Color color = new Color(Palette.OK);
    private float radius = 3.5f;

    public void set(Color color) {
        this.color.set(color);
    }

    @Override
    public float getPrefWidth() {
        return radius * 2f + 2f;
    }

    @Override
    public float getPrefHeight() {
        return radius * 2f + 2f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShapeDrawer drawer = Shapes.get(batch);
        if (drawer == null) return;

        drawer.setColor(color);
        drawer.filledCircle(getX() + getWidth() * 0.5f, getY() + getHeight() * 0.5f, radius);
        batch.setColor(Color.WHITE);
    }
}
