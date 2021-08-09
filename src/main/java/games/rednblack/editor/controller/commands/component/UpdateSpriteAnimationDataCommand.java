package games.rednblack.editor.controller.commands.component;

import com.badlogic.gdx.graphics.g2d.Animation;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.EntityModifyRevertibleCommand;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationStateComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.h2d.common.MsgAPI;

/**
 * Created by CyberJoe on 6/18/2015.
 */
public class UpdateSpriteAnimationDataCommand extends EntityModifyRevertibleCommand {

    private Integer entityId;

    private int previousFps;
    private String previousAnimationName;
    private Animation.PlayMode previousPlayMode;

    @Override
    public void doAction() {
        Object[] payload = getNotification().getBody();
        int entity = (int) payload[0];
        entityId = EntityUtils.getEntityId(entity);

        int fps = (int) payload[1];
        String animName = (String) payload[2];
        Animation.PlayMode playMode = (Animation.PlayMode) payload[3];

        SpriteAnimationComponent spriteAnimationComponent = SandboxComponentRetriever.get(entity, SpriteAnimationComponent.class);
        SpriteAnimationStateComponent spriteAnimationStateComponent = SandboxComponentRetriever.get(entity, SpriteAnimationStateComponent.class);
        previousFps = spriteAnimationComponent.fps;
        previousAnimationName = spriteAnimationComponent.currentAnimation;
        previousPlayMode = spriteAnimationComponent.playMode;

        spriteAnimationComponent.fps = fps;
        spriteAnimationComponent.currentAnimation = animName;
        spriteAnimationComponent.playMode = playMode;
        spriteAnimationStateComponent.set(spriteAnimationComponent);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    @Override
    public void undoAction() {
        int entity = EntityUtils.getByUniqueId(entityId);

        SpriteAnimationComponent spriteAnimationComponent = SandboxComponentRetriever.get(entity, SpriteAnimationComponent.class);
        SpriteAnimationStateComponent spriteAnimationStateComponent = SandboxComponentRetriever.get(entity, SpriteAnimationStateComponent.class);
        spriteAnimationComponent.fps = previousFps;
        spriteAnimationComponent.currentAnimation = previousAnimationName;
        spriteAnimationComponent.playMode = previousPlayMode;
        spriteAnimationStateComponent.set(spriteAnimationComponent);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ITEM_DATA_UPDATED, entity);
    }

    public static Object payload(int entity, int fps, String animName, Animation.PlayMode playMode) {
        Object[] payload = new Object[4];
        payload[0] = entity;
        payload[1] = fps;
        payload[2] = animName;
        payload[3] = playMode;

        return payload;
    }
}
