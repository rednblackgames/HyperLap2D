package games.rednblack.editor.plugin.performance.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.plugin.performance.data.Metric;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * One number worth watching: what it is, what it reads now, and what it has been doing.
 * <p>
 * The value carries the colour, not the card, so a glance across the row lands on whatever is in trouble.
 */
public class StatTile extends VisTable {

    private final VisLabel valueLabel;
    private final VisLabel unitLabel;
    private final Sparkline sparkline = new Sparkline();

    public StatTile(String title, String unit) {
        VisLabel titleLabel = Labels.fitted("small");
        titleLabel.setText(title.toUpperCase());
        titleLabel.setColor(Palette.TEXT_MUTED);

        Label.LabelStyle valueStyle = new Label.LabelStyle(VisUI.getSkin().getFont("big-font"), Palette.TEXT);
        valueLabel = new VisLabel("-", valueStyle);

        unitLabel = Labels.fitted("small");
        unitLabel.setText(unit);
        unitLabel.setColor(Palette.TEXT_MUTED);

        pad(8f, 10f, 8f, 10f);
        add(titleLabel).left().growX().row();

        VisTable valueRow = new VisTable();
        valueRow.add(valueLabel).left().bottom();
        valueRow.add(unitLabel).left().bottom().growX().padLeft(3f).padBottom(1f);
        add(valueRow).left().growX().padTop(1f).row();

        add(sparkline).growX().height(20f).padTop(4f);
    }

    /**
     * Four of these share the row equally, so what one asks for it asks for four times over. It asks for a
     * modest fixed width and lets the row decide the rest.
     */
    @Override
    public float getPrefWidth() {
        return 120f;
    }

    /** The builder is reused by the caller, so nothing here may keep hold of it. */
    public void setValue(CharSequence value) {
        valueLabel.setText(value);
    }

    public void setValueColor(Color color) {
        valueLabel.setColor(color);
    }

    public void setUnit(CharSequence unit) {
        unitLabel.setText(unit);
    }

    public void setSeries(Metric metric, Color color) {
        sparkline.setMetric(metric);
        sparkline.setSeriesColor(color);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        ShapeDrawer drawer = Shapes.get(batch);
        if (drawer != null) {
            Shapes.roundedRect(drawer, getX(), getY(), getWidth(), getHeight(), 6f, Palette.TILE_BG);
            batch.setColor(Color.WHITE);
        }
        super.draw(batch, parentAlpha);
    }
}
