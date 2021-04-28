package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

public abstract class BasicDrawStrategy implements IDrawStrategy {
    protected TiledPlugin tiledPlugin;
    protected final Vector2 temp = new Vector2();

    public BasicDrawStrategy(TiledPlugin plugin) {
        tiledPlugin = plugin;
    }

    protected void postProcessEntity(Entity entity, float x, float y, int row, int column) {
        MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
        mainItemComponent.tags.add(TiledPlugin.TILE_TAG);

        mainItemComponent.setCustomVars(TiledPlugin.ROW, Integer.toString(row));
        mainItemComponent.setCustomVars(TiledPlugin.COLUMN, Integer.toString(column));

        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
        transformComponent.x = x;
        transformComponent.y = y;
    }

    protected boolean checkValidTile(Entity entity) {
        return tiledPlugin.isOnCurrentSelectedLayer(entity) && tiledPlugin.isTile(entity)
                && ComponentRetriever.get(entity, MainItemComponent.class).entityType == tiledPlugin.getSelectedTileType();
    }
}
