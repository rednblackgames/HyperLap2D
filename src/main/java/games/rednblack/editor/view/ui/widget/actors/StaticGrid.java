package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class StaticGrid extends Actor {

    private final Window window;

    private final Vector2 tmp = new Vector2();

    private ShapeDrawer shapeDrawer;

    public StaticGrid(Window window) {
        this.window = window;
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        if (shapeDrawer == null) {
            shapeDrawer = new ShapeDrawer(batch, WhitePixel.sharedInstance.textureRegion){
                /* OPTIONAL: Ensuring a certain smoothness. */
                @Override
                protected int estimateSidesRequired(float radiusX, float radiusY) {
                    return 200;
                }
            };
        }

        drawGrid(window.getX(), window.getY(), window.getWidth(), window.getHeight(), parentAlpha);
    }

    private void drawGrid (float x, float y, float windowWidth, float windowHeight, float parentAlpha) {
        tmp.set(windowWidth / 2, windowHeight / 2);

        int lineCount = (int)(windowWidth / 28f);

        for (int i = -lineCount / 2 - 1; i < lineCount / 2 + 1; i++) {
            float spacing = windowWidth / lineCount;
            shapeDrawer.setColor(0.2f, 0.2f, 0.2f, parentAlpha);

            float posX = tmp.x - i * spacing - tmp.x % spacing;
            float posY = tmp.y + i * spacing - tmp.y % spacing;
            shapeDrawer.line(posX, tmp.y - windowHeight/2f, posX, tmp.y + windowHeight/2f, 2f); // vertical
            shapeDrawer.line(tmp.x - windowWidth/2f, posY, tmp.x + windowWidth/2f, posY, 2f); // horizontal
        }
    }
}
