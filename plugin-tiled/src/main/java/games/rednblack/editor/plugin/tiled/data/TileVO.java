package games.rednblack.editor.plugin.tiled.data;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.factory.EntityFactory;

/**
 * Created by mariam on 5/13/16.
 */
public class TileVO {

    public String regionName = "";
    public Vector2 gridOffset;
    public int entityType = EntityFactory.IMAGE_TYPE;

    public TileVO() {
        gridOffset = new Vector2();
    }

    public TileVO(String regionName) {
        this.regionName = regionName;
        gridOffset = new Vector2();
    }

    public TileVO(String regionName, Vector2 offset) {
        this.regionName = regionName;
        this.gridOffset = offset;
    }
}
