package games.rednblack.editor.plugin.tiled;

import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.h2d.common.filters.IAbstractResourceFilter;
import games.rednblack.h2d.extension.spine.SpineItemType;

public class TilesResourceFilter extends IAbstractResourceFilter {

    private final TiledPlugin tiledPlugin;

    public TilesResourceFilter(TiledPlugin tiledPlugin) {
        super("Filter Tiles", "filter-tiles");
        this.tiledPlugin = tiledPlugin;
    }

    @Override
    public boolean filterResource(String resName, int entityType) {
        if (entityType == EntityFactory.IMAGE_TYPE
                || entityType == EntityFactory.SPRITE_TYPE
                || entityType == SpineItemType.SPINE_TYPE) {
            return tiledPlugin.dataToSave.containsAutoTile(resName) || tiledPlugin.dataToSave.containsTile(resName);
        }
        return false;
    }
}
