package games.rednblack.editor.renderer.components;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class BoundingBoxComponent implements BaseComponent {

    public Rectangle rectangle = new Rectangle();

    public Vector2[] points = new Vector2[4];
    public float checksum;

    {
        points[0] = new Vector2();
        points[1] = new Vector2();
        points[2] = new Vector2();
        points[3] = new Vector2();
    }

    /**
     * Returns a bounding box for this box.
     *
     * @return the bounding box
     */
    public Rectangle getBoundingRect() {
        rectangle.x = Math.min(Math.min(Math.min(points[0].x, points[1].x), points[2].x), points[3].x);
        rectangle.width = Math.max(Math.max(Math.max(points[0].x, points[1].x), points[2].x), points[3].x) - rectangle.x;
        rectangle.y = Math.min(Math.min(Math.min(points[0].y, points[1].y), points[2].y), points[3].y);
        rectangle.height = Math.max(Math.max(Math.max(points[0].y, points[1].y), points[2].y), points[3].y) - rectangle.y;
        return rectangle;
    }

    @Override
    public void reset() {
        rectangle.set(0, 0, 0, 0);
        for (Vector2 vector2 : points) {
            vector2.set(0, 0);
        }
        checksum = 0;
    }
}
