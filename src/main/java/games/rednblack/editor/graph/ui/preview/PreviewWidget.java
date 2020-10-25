package games.rednblack.editor.graph.ui.preview;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;

public class PreviewWidget extends Widget {
    private Rectangle viewRectangle = new Rectangle();
    private float viewScale = 1;
    private NavigableCanvas navigableCanvas;
    private boolean movedThisFrame = false;

    public PreviewWidget(NavigableCanvas navigableCanvas) {
        this.navigableCanvas = navigableCanvas;
        addListener(
                new ClickListener(Input.Buttons.LEFT) {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        navigateCanvasTo(x + getX(), y + getY());
                    }
                });
        DragListener moveCanvasDragListener = new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                if (event.getTarget() == PreviewWidget.this && !movedThisFrame) {
                    navigateCanvasTo(x + getX(), y + getY());
                    movedThisFrame = true;
                }
            }
        };
        moveCanvasDragListener.setTapSquareSize(0);
        addListener(moveCanvasDragListener);
    }

    private void navigateCanvasTo(float x, float y) {
        float currentMiddleX = viewRectangle.x + viewRectangle.width / 2;
        float currentMiddleY = viewRectangle.y + viewRectangle.height / 2;

        float difX = x - currentMiddleX;
        float difY = y - currentMiddleY;

        Vector2 tmp = new Vector2();
        navigableCanvas.getCanvasPosition(tmp);
        float canvasX = tmp.x;
        float canvasY = tmp.y;

        navigableCanvas.navigateTo(MathUtils.round(canvasX + difX / viewScale), MathUtils.round(canvasY + difY / viewScale));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        float width = getWidth();
        float height = getHeight();
        float x = getX();
        float y = getY();

        Vector2 tmp = new Vector2();
        navigableCanvas.getCanvasPosition(tmp);
        float canvasX = tmp.x;
        float canvasY = tmp.y;
        navigableCanvas.getCanvasSize(tmp);
        float canvasWidth = tmp.x;
        float canvasHeight = tmp.y;
        float canvasRatio = canvasWidth / canvasHeight;
        float ratio = width / height;
        if (canvasRatio > ratio) {
            viewScale = width / canvasWidth;
        } else {
            viewScale = height / canvasHeight;
        }

        float originX = (width - canvasWidth * viewScale) / 2f;
        float originY = (height - canvasHeight * viewScale) / 2f;

        if (clipBegin(x, y, width, height)) {
            batch.setColor(Color.DARK_GRAY);
            batch.draw(WhitePixel.sharedInstance.texture, x + originX, y + originY, canvasWidth * viewScale, canvasHeight * viewScale);

            batch.setColor(Color.LIGHT_GRAY);
            for (Actor child : navigableCanvas.getElements()) {
                float childX = child.getX();
                float childY = child.getY();
                float childWidth = child.getWidth();
                float childHeight = child.getHeight();

                batch.draw(WhitePixel.sharedInstance.texture, x + originX + (childX + canvasX) * viewScale, y + originY + (childY + canvasY) * viewScale,
                        childWidth * viewScale, childHeight * viewScale);
            }

            navigableCanvas.getVisibleSize(tmp);
            batch.setColor(new Color(1f, 1f, 1f, 0.5f));
            batch.draw(WhitePixel.sharedInstance.texture, x + originX + canvasX * viewScale, y + originY + canvasY * viewScale,
                    tmp.x * viewScale, tmp.y * viewScale);
            batch.flush();
            clipEnd();
        }

        viewRectangle.set(x + originX + canvasX * viewScale, y + originY + canvasY * viewScale,
                tmp.x * viewScale, tmp.y * viewScale);
        movedThisFrame = false;
    }
}
