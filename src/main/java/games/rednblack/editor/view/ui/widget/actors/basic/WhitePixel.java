package games.rednblack.editor.view.ui.widget.actors.basic;

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
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();

        texture = new Texture(pixmap, true);
        texture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
        textureRegion = new TextureRegion(texture);
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
            textureRegion = null;
        }
    }
}
