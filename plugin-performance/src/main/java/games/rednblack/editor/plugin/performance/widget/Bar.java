package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** A share of something, drawn on a track so that an empty bar still reads as a measurement. */
public class Bar extends Widget {

    private final Color color = new Color(Palette.RUNTIME);
    private float fraction;

    public void set(float fraction, Color color) {
        this.fraction = Math.max(0f, Math.min(1f, fraction));
        this.color.set(color);
    }

    @Override
    public float getPrefHeight() {
        return 8f;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShapeDrawer drawer = Shapes.get(batch);
        if (drawer == null) return;

        float height = Math.min(getHeight(), 8f);
        float y = getY() + (getHeight() - height) * 0.5f;

        Shapes.roundedRect(drawer, getX(), y, getWidth(), height, height * 0.5f, Palette.TILE_BG);
        float filled = getWidth() * fraction;
        if (filled > 1f) Shapes.roundedRect(drawer, getX(), y, filled, height, height * 0.5f, color);

        batch.setColor(Color.WHITE);
    }
}
