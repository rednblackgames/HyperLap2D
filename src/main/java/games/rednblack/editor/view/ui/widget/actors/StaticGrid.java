package games.rednblack.editor.view.ui.widget.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class StaticGrid extends Actor {

    private final Window window;

    private final Vector2 tmp = new Vector2();

    private ShapeRenderer shapeRenderer;

    public StaticGrid(Window window) {
        this.window = window;
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void draw (Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawGrid(window.getX(), window.getY(), window.getWidth(), window.getHeight(), parentAlpha);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();
    }

    private void drawGrid (float x, float y, float windowWidth, float windowHeight, float parentAlpha) {
        tmp.set(x + windowWidth / 2, y + windowHeight / 2);

        int lineCount = (int)(windowWidth / 28f);

        for (int i = -lineCount / 2 - 1; i < lineCount / 2 + 1; i++) {
            float spacing = windowWidth / lineCount;
            shapeRenderer.setColor(0.2f, 0.2f, 0.2f, parentAlpha);

            float posX = tmp.x - i * spacing - tmp.x % spacing;
            float posY = tmp.y + i * spacing - tmp.y % spacing;
            shapeRenderer.rectLine(posX, tmp.y - windowHeight/2f, posX, tmp.y + windowHeight/2f, 2f); // vertical
            shapeRenderer.rectLine(tmp.x - windowWidth/2f, posY, tmp.x + windowWidth/2f, posY, 2f); // horizontal
        }
    }
}
