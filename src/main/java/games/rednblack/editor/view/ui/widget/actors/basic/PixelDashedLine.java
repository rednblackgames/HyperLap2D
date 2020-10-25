package games.rednblack.editor.view.ui.widget.actors.basic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.TimeUtils;
import games.rednblack.editor.proxy.EditorTextureManager;

/**
 * Class for creating, showing, animating a dashed line as {@link PixelLine}
 */
public class PixelDashedLine extends Image {

    private Texture tex;
    private float thickness = 1f;
    private Pixmap pix;
    private int visibleLength, invisibleLength, o = 0;
    private double lineLength;
    private long time;

    public PixelDashedLine(EditorTextureManager tm, int x, int y, int toX, int toY, int visibleLength, int invisibleLength) {
        super(prepareTexture(tm)); //Necessary for showing dashed line
        pix = new Pixmap(1000, 500, Pixmap.Format.RGBA8888); //Creating pix and tex here to stay away from
        tex = new Texture(pix);                                          //not initialised error, when disposing these two.
        this.visibleLength = visibleLength;
        this.invisibleLength = invisibleLength;
        time = TimeUtils.millis(); //For animation

        setPosition(x, y, toX, toY);
    }

    public void setPosition(float x, float y, float toX, float toY) {
        this.setX(x);
        this.setY(y);

        lineLength = Math.sqrt((toX-x)*(toX-x)+(toY-y)*(toY-y));
        this.setScaleX((float) lineLength);

        pix = new Pixmap((int)(lineLength), 1, Pixmap.Format.RGBA8888);
        tex.dispose(); //Free memory!
        tex = drawDash(visibleLength, invisibleLength, o); // o is the offset (for animation)
        this.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));
        pix.dispose(); //Free memory!

        this.setRotation(90-getAngle(x, y, toX, toY));
    }

    /**
     * Draw dashed line
     * @param visibleLength length of visible lines
     * @param invisibleLength length of invisible lines (space between visible lines)
     * @param offset For animation
     * @return Return a texture
     */
    private Texture drawDash(int visibleLength, int invisibleLength, int offset) {
        pix.setColor(Color.WHITE);
        int i = 0;
        while(i <= pix.getWidth()) {
            if (i+visibleLength < pix.getWidth())
                pix.fillRectangle(i+offset, 0, visibleLength, pix.getHeight());
            else {
                pix.fillRectangle(i+offset, 0, pix.getWidth(), pix.getHeight());
                pix.fillRectangle(0, 0, visibleLength, pix.getHeight());
            }
            i = i+visibleLength+invisibleLength;
        }
        return new Texture(pix);
    }

    public void setOpacity(float opacity) {
        Color clr = getColor();
        clr.a = opacity;
        setColor(clr);
    }

    private static Texture prepareTexture(EditorTextureManager tm) {
        return tm.getEditorAsset("pixel");
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        this.setScaleY(thickness);

        if (TimeUtils.timeSinceMillis(time) > 80) { //Every 80ms
            time = TimeUtils.millis();
            o = o + 1;
            if (o >= visibleLength+invisibleLength) o = 0;
            pix = new Pixmap((int) (lineLength), 1, Pixmap.Format.RGBA8888);
            tex.dispose();
            tex = drawDash(visibleLength, invisibleLength, o);
            this.setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));
            pix.dispose();
        }
    }

    private float getAngle(float x, float y, float toX, float toY) {
        float angle = (float) Math.toDegrees(Math.atan2(toX - x, toY - y));

        if(angle < 0){
            angle += 360;
        }

        return angle;
    }

    public void setThickness (float thickness) {
        this.thickness = thickness;
    }

}