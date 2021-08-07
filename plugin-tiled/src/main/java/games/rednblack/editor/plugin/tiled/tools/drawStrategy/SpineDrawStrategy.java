package games.rednblack.editor.plugin.tiled.tools.drawStrategy;

import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import games.rednblack.editor.plugin.tiled.TiledPlugin;
import games.rednblack.editor.renderer.components.SpineDataComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.h2d.common.command.ReplaceSpineCommandBuilder;
import games.rednblack.h2d.common.factory.IFactory;
import games.rednblack.h2d.extention.spine.ResourceRetrieverAttachmentLoader;

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

        SpineDataComponent spineDataComponent = ComponentRetriever.get(entity, SpineDataComponent.class, tiledPlugin.getAPI().getEngine());
        if (!spineDataComponent.animationName.equals(tiledPlugin.getSelectedTileName())) {
            replaceSpineCommandBuilder.begin(entity);
            String animName = tiledPlugin.getSelectedTileName();
            replaceSpineCommandBuilder.setAnimationName(animName);
            ResourceRetrieverAttachmentLoader atlasAttachmentLoader = new ResourceRetrieverAttachmentLoader(animName, tiledPlugin.getAPI().getSceneLoader().getRm());
            SkeletonJson skeletonJson = new SkeletonJson(atlasAttachmentLoader);
            replaceSpineCommandBuilder.setSkeletonJson(skeletonJson);
            SkeletonData skeletonData = skeletonJson.readSkeletonData((tiledPlugin.getAPI().getSceneLoader().getRm().getSkeletonJSON(animName)));
            replaceSpineCommandBuilder.setSkeleton(new Skeleton(skeletonData));
            replaceSpineCommandBuilder.execute(tiledPlugin.facade);
        }
    }
}
