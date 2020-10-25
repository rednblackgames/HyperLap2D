package games.rednblack.editor.view.ui.widget.actors.basic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.TimeUtils;
import space.earlygrey.shapedrawer.ShapeDrawer;

/**
 * Class for creating, showing, animating a dashed line as {@link PixelLine}
 *
 * @author alyrow
 */
public class PixelDashedLine extends Actor {

    private float thickness = 1f;
    private int visibleLength, invisibleLength, o = 0;
    private double lineLength;
    private long time;
    private ShapeDrawer shapeDrawer;

    public PixelDashedLine(int x, int y, int toX, int toY, int visibleLength, int invisibleLength) {
        this.visibleLength = visibleLength;
        this.invisibleLength = invisibleLength;
        time = TimeUtils.millis(); //For animation

        setPosition(x, y, toX, toY);
    }

    public void setPosition(float x, float y, float toX, float toY) {
        this.setX(x);
        this.setY(y);

        lineLength = Math.sqrt((toX - x) * (toX - x) + (toY - y) * (toY - y));
        this.setScaleX((float) lineLength);

        this.setRotation(90 - getAngle(x, y, toX, toY));
    }

    /**
     * Draw dashed line
     *
     * @param visibleLength   length of visible lines
     * @param invisibleLength length of invisible lines (space between visible lines)
     * @param offset          For animation
     * @return Return a texture
     */
    private void drawDash(int visibleLength, int invisibleLength, int offset) {
        int i = 0;
        boolean vertical = getRotation() == 90;
        if (vertical) {
            while (i <= lineLength) {
                if (i + visibleLength < lineLength)
                    shapeDrawer.filledRectangle(getX(), getY() + i + offset, thickness, visibleLength);
                i = i + visibleLength + invisibleLength;
            }
        } else {
            while (i <= lineLength) {
                if (i + visibleLength < lineLength)
                    shapeDrawer.filledRectangle(getX() + i + offset, getY(), visibleLength, thickness);
                i = i + visibleLength + invisibleLength;
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (TimeUtils.timeSinceMillis(time) > 80) { //Every 80ms
            time = TimeUtils.millis();
            o = o + 1;
            if (o >= visibleLength + invisibleLength) o = 0;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (shapeDrawer == null) {
            shapeDrawer = new ShapeDrawer(batch, WhitePixel.sharedInstance.textureRegion);
        }
        drawDash(visibleLength, invisibleLength, o);
    }

    private float getAngle(float x, float y, float toX, float toY) {
        float angle = (float) Math.toDegrees(Math.atan2(toX - x, toY - y));

        if (angle < 0) {
            angle += 360;
        }

        return angle;
    }

    public void setOpacity(float opacity) {
        Color clr = getColor();
        clr.a = opacity;
        setColor(clr);
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }
}
