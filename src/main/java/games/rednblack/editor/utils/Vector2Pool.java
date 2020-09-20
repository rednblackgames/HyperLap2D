package games.rednblack.editor.utils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

public class Vector2Pool extends Pool<Vector2> {

    public Vector2Pool(int capacity) {
        super(capacity);
    }

    @Override
    protected Vector2 newObject() {
        return new Vector2();
    }
}
