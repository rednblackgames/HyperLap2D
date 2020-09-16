package games.rednblack.editor.graph.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class WhitePixel {
    public static WhitePixel sharedInstance;

    public static void initializeShared() {
        sharedInstance = new WhitePixel();
    }

    public static void disposeShared() {
        sharedInstance.dispose();
    }

    public Texture texture;
    public TextureRegion textureRegion;

    public WhitePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        texture = new Texture(pixmap);
        textureRegion = new TextureRegion(texture);
    }

    public void dispose() {
        texture.dispose();
        texture = null;
        textureRegion = null;
    }
}
