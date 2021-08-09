package games.rednblack.editor.controller.commands.component;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;

public class ReplaceRegionCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private String backupRegionName;
    private TextureRegion backupRegion;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        String regionName = (String) payload[1];
        TextureRegion region = (TextureRegion) payload[2];

        entityId = EntityUtils.getEntityId(entity);

        TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);
        DimensionsComponent size = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);

        backupRegionName = textureRegionComponent.regionName;
        backupRegion = textureRegionComponent.region;

        textureRegionComponent.regionName = regionName;
        textureRegionComponent.region = region;
        ProjectInfoVO projectInfoVO = Sandbox.getInstance().getSceneControl().sceneLoader.getRm().getProjectVO();
        float ppwu = projectInfoVO.pixelToWorld;
        size.width = textureRegionComponent.region.getRegionWidth() / ppwu;
        size.height = textureRegionComponent.region.getRegionHeight() / ppwu;

        transformComponent.originX = size.width / 2;
        transformComponent.originY = size.height / 2;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);
        DimensionsComponent size = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);

        textureRegionComponent.regionName = backupRegionName;
        textureRegionComponent.region = backupRegion;
        ProjectInfoVO projectInfoVO = Sandbox.getInstance().getSceneControl().sceneLoader.getRm().getProjectVO();
        float ppwu = projectInfoVO.pixelToWorld;
        size.width = textureRegionComponent.region.getRegionWidth() / ppwu;
        size.height = textureRegionComponent.region.getRegionHeight() / ppwu;

        transformComponent.originX = size.width / 2;
        transformComponent.originY = size.height / 2;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
