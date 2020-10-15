package games.rednblack.editor.controller.commands;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;

public class UpdateRegionCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private String backupRegionName;
    private TextureRegion backupRegion;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        Entity entity = (Entity) payload[0];
        String regionName = (String) payload[1];
        TextureRegion region = (TextureRegion) payload[2];

        entityId = EntityUtils.getEntityId(entity);

        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
        DimensionsComponent size = ComponentRetriever.get(entity, DimensionsComponent.class);

        backupRegionName = textureRegionComponent.regionName;
        backupRegion = textureRegionComponent.region;

        textureRegionComponent.regionName = regionName;
        textureRegionComponent.region = region;
        ProjectInfoVO projectInfoVO = Sandbox.getInstance().getSceneControl().sceneLoader.getRm().getProjectVO();
        float ppwu = projectInfoVO.pixelToWorld;
        size.width = textureRegionComponent.region.getRegionWidth() / ppwu;
        size.height = textureRegionComponent.region.getRegionHeight() / ppwu;
    }

    @Override
    public void undoAction() {
        Entity entity = EntityUtils.getByUniqueId(entityId);

        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
        DimensionsComponent size = ComponentRetriever.get(entity, DimensionsComponent.class);

        textureRegionComponent.regionName = backupRegionName;
        textureRegionComponent.region = backupRegion;
        ProjectInfoVO projectInfoVO = Sandbox.getInstance().getSceneControl().sceneLoader.getRm().getProjectVO();
        float ppwu = projectInfoVO.pixelToWorld;
        size.width = textureRegionComponent.region.getRegionWidth() / ppwu;
        size.height = textureRegionComponent.region.getRegionHeight() / ppwu;
    }
}
