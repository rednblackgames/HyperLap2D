package games.rednblack.editor.view.ui.widget.actors.basic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

/**
 * Class for showing dashed animated rectangle as {@link PixelRect}
 */
public class PixelDashedRectangle extends Group {

    private final PixelDashedLine[] lines = new PixelDashedLine[4];
    private final Image fill;

    public PixelDashedRectangle() {
        this(0, 0);
    }

    public PixelDashedRectangle(float width, float height) {
        lines[0] = new PixelDashedLine( 0, 0, (int) width, 0, 5,8);
        lines[1] = new PixelDashedLine( 0, 0, 0, (int) height,5,8);
        lines[2] = new PixelDashedLine( (int) width, 0, (int) width, (int) height,5,8);
        lines[3] = new PixelDashedLine( 0, (int) height, (int) width, (int) height, 5,8);

        fill = new Image(WhitePixel.sharedInstance.texture);
        fill.setColor(new Color(0, 0, 0, 0));

        addActor(fill);

        addActor(lines[0]);
        addActor(lines[1]);
        addActor(lines[2]);
        addActor(lines[3]);

        super.setWidth(width);
        super.setHeight(height);

        setOrigin(getWidth() / 2, getHeight() / 2);
        fill.setScaleX(getWidth());
        fill.setScaleY(getHeight());
    }

    public void setBorderColor(Color clr) {
        lines[0].setColor(clr);
        lines[1].setColor(clr);
        lines[2].setColor(clr);
        lines[3].setColor(clr);
    }

    public void setFillColor(Color clr) {
        fill.setColor(clr);
    }

    @Override
    public void setWidth(float width) {
        super.setWidth(width);

        lines[0].setPosition(0, 0, width, 0);
        lines[2].setPosition(width, 0, width, getHeight());
        lines[3].setPosition(0, getHeight(), width, getHeight());
        fill.setScaleX(getWidth());
    }

    @Override
    public void setHeight(float height) {
        super.setHeight(height);
        lines[1].setPosition(0, 0, 0, height);
        lines[2].setPosition(getWidth(), 0, getWidth(), height);
        lines[3].setPosition(0, height, getWidth(), height);
        fill.setScaleY(getHeight());
    }

    public void setOpacity(float opacity) {
        addAction(Actions.alpha(opacity, 0.3f, Interpolation.exp5Out));
    }

    public Rectangle getRect() {
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        if (width < 0) {
            width = -width;
            x = x - width;
        }
        if (height < 0) {
            height = -height;
            y = y - height;
        }
        Rectangle r = new Rectangle(x, y, width, height);

        return r;
    }

    public void setThickness(float thickness) {
        lines[0].setThickness(thickness);
        lines[1].setThickness(thickness);
        lines[2].setThickness(thickness);
        lines[3].setThickness(thickness);
    }

}
