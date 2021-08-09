package games.rednblack.editor.controller.commands.component;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationStateComponent;
import games.rednblack.editor.renderer.data.FrameRange;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;

public class ReplaceSpriteAnimationCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;
    private String backupAnimName;
    private Array<TextureAtlas.AtlasRegion> backupAnimRegions;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        String animName = (String) payload[1];
        Array<TextureAtlas.AtlasRegion> regions = (Array<TextureAtlas.AtlasRegion>) payload[2];

        entityId = EntityUtils.getEntityId(entity);

        SpriteAnimationComponent spriteAnimationComponent = SandboxComponentRetriever.get(entity, SpriteAnimationComponent.class);
        SpriteAnimationStateComponent spriteAnimationStateComponent = SandboxComponentRetriever.get(entity, SpriteAnimationStateComponent.class);
        TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);
        DimensionsComponent size = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);

        backupAnimName = spriteAnimationComponent.animationName;
        backupAnimRegions = spriteAnimationStateComponent.allRegions;

        spriteAnimationComponent.animationName = animName;
        spriteAnimationComponent.frameRangeMap.clear();

        spriteAnimationComponent.frameRangeMap.put("Default", new FrameRange("Default", 0, regions.size-1));
        spriteAnimationComponent.currentAnimation = "Default";
        spriteAnimationComponent.playMode = Animation.PlayMode.LOOP;

        spriteAnimationStateComponent.setAllRegions(regions);
        spriteAnimationStateComponent.set(spriteAnimationComponent);

        textureRegionComponent.region = regions.get(0);

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

        SpriteAnimationComponent spriteAnimationComponent = SandboxComponentRetriever.get(entity, SpriteAnimationComponent.class);
        SpriteAnimationStateComponent spriteAnimationStateComponent = SandboxComponentRetriever.get(entity, SpriteAnimationStateComponent.class);
        TextureRegionComponent textureRegionComponent = SandboxComponentRetriever.get(entity, TextureRegionComponent.class);
        DimensionsComponent size = SandboxComponentRetriever.get(entity, DimensionsComponent.class);
        TransformComponent transformComponent = SandboxComponentRetriever.get(entity, TransformComponent.class);

        spriteAnimationComponent.animationName = backupAnimName;
        spriteAnimationComponent.frameRangeMap.clear();

        spriteAnimationComponent.frameRangeMap.put("Default", new FrameRange("Default", 0, backupAnimRegions.size-1));
        spriteAnimationComponent.currentAnimation = "Default";
        spriteAnimationComponent.playMode = Animation.PlayMode.LOOP;

        spriteAnimationStateComponent.setAllRegions(backupAnimRegions);
        spriteAnimationStateComponent.set(spriteAnimationComponent);

        textureRegionComponent.region = backupAnimRegions.get(0);

        ProjectInfoVO projectInfoVO = Sandbox.getInstance().getSceneControl().sceneLoader.getRm().getProjectVO();
        float ppwu = projectInfoVO.pixelToWorld;
        size.width = textureRegionComponent.region.getRegionWidth() / ppwu;
        size.height = textureRegionComponent.region.getRegionHeight() / ppwu;

        transformComponent.originX = size.width / 2;
        transformComponent.originY = size.height / 2;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }
}
