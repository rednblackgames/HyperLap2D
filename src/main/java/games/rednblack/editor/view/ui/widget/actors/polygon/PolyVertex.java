package games.rednblack.editor.view.ui.widget.actors.polygon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.view.ui.followers.PolygonFollower;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PolyVertex extends Actor implements Pool.Poolable {

    private static final int ANCHOR_SIZE = 12;

    private final ShapeDrawer shapeDrawer;

    private final Color normalColor, selectedColor;

    private boolean selected = false;
    private int index = -1;

    private final VisLabel indexLabel;

    public PolyVertex(ShapeDrawer shapeDrawer) {
        this.shapeDrawer = shapeDrawer;
        setSize(ANCHOR_SIZE, ANCHOR_SIZE);
        indexLabel = StandardWidgetsFactory.createLabel("i");
        normalColor = new Color();
        normalColor.set(Color.WHITE);
        selectedColor = new Color();
        selectedColor.set(PolygonFollower.overColor);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        indexLabel.setPosition(getX() + ((getWidth() - indexLabel.getWidth()) * 0.5f), getY() + ((getHeight() - indexLabel.getHeight()) * 0.5f));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setColor(selected ? selectedColor : normalColor);
        getColor().a *= parentAlpha;

        shapeDrawer.filledCircle(getX() + getWidth() * 0.5f, getY() + getHeight() * 0.5f, (getWidth() + 2) * 0.5f, Color.BLACK);
        shapeDrawer.filledCircle(getX() + getWidth() * 0.5f, getY() + getHeight() * 0.5f, getWidth() * 0.5f, getColor());
        if (selected)
            indexLabel.draw(batch, parentAlpha);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setIndex(int index) {
        this.index = index;
        indexLabel.setText(index);
        indexLabel.setSize(indexLabel.getPrefWidth(), indexLabel.getPrefHeight());
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void reset() {
        clearListeners();

        selected = false;
    }
}
