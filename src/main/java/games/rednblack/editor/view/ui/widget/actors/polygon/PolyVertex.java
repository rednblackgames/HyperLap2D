package games.rednblack.editor.view.ui.widget.actors.polygon;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Pool;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PolyVertex extends Actor implements Pool.Poolable {

    private static final int ANCHOR_SIZE = 9;

    private final ShapeDrawer shapeDrawer;

    private final Color normalColor, selectedColor;

    private boolean selected = false;
    private int index = -1;

    public PolyVertex(ShapeDrawer shapeDrawer) {
        this.shapeDrawer = shapeDrawer;
        setSize(ANCHOR_SIZE, ANCHOR_SIZE);
        normalColor = new Color();
        normalColor.set(Color.WHITE);
        selectedColor = new Color();
        selectedColor.set(Color.ORANGE);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        setColor(selected ? selectedColor : normalColor);
        getColor().a *= parentAlpha;

        shapeDrawer.filledRectangle(getX(), getY(), getWidth(), getHeight(), getColor());

        setColor(Color.BLACK);
        getColor().a *= parentAlpha;

        shapeDrawer.filledRectangle(getX() + 1, getY() + 1, getWidth() - 2, getHeight() - 2, getColor());
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public void reset() {
        clearListeners();

        selected = false;
        normalColor.set(Color.WHITE);
        selectedColor.set(Color.ORANGE);
    }
}
