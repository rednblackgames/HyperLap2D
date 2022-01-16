package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceSpineCommandBuilder;
import games.rednblack.h2d.common.factory.IFactory;
import games.rednblack.h2d.extension.spine.SpineComponent;
import games.rednblack.h2d.extension.spine.SpineDataObject;
import games.rednblack.h2d.extension.spine.SpineItemType;

public class SpineDrawStrategy extends BasicDrawStrategy {

    private final ReplaceSpineCommandBuilder replaceSpineCommandBuilder = new ReplaceSpineCommandBuilder();

    public SpineDrawStrategy(TiledPlugin plugin) {
        super(plugin);
    }

    @Override
    public void drawTile(float x, float y, int row, int column) {
        int underneathTile = tiledPlugin.getPluginEntityWithParams(row, column);
        if (underneathTile != -1) {
            updateTile(underneathTile);
            return;
        }

        IFactory itemFactory =  tiledPlugin.getAPI().getItemFactory();
        temp.set(x, y);

        if (itemFactory.createSpineAnimation(tiledPlugin.getSelectedTileName(), temp)) {
            int imageEntity = itemFactory.getCreatedEntity();
            postProcessEntity(imageEntity, x, y, row, column);
        }
    }

    @Override
    public void updateTile(int entity) {
        if (!checkValidTile(entity)) return;

        SpineComponent spineComponent = ComponentRetriever.get(entity, SpineComponent.class, tiledPlugin.getAPI().getEngine());
        if (!spineComponent.animationName.equals(tiledPlugin.getSelectedTileName())) {
            replaceSpineCommandBuilder.begin(entity);
            String animName = tiledPlugin.getSelectedTileName();
            replaceSpineCommandBuilder.setAnimationName(animName);
            SpineDataObject spineDataObject = (SpineDataObject) tiledPlugin.getAPI().getSceneLoader().getRm().getExternalItemType(SpineItemType.SPINE_TYPE, animName);
            replaceSpineCommandBuilder.setSkeletonJson(spineDataObject.skeletonJson);
            SkeletonData skeletonData = spineDataObject.skeletonData;
            replaceSpineCommandBuilder.setSkeleton(new Skeleton(skeletonData));
            replaceSpineCommandBuilder.execute(tiledPlugin.facade);
        }
    }
}
