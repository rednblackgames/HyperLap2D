package games.rednblack.editor.controller.commands.resource;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.components.sprite.SpriteAnimationComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.SpriteAnimationVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Sasun Poghosyan on 5/12/2016.
 */
public class DeleteSpriteAnimation extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteSpriteAnimation";
    public static final String DONE = CLASS_NAME + "DONE";

    private final ArrayList<Entity> entityList = new ArrayList<>();
    private final ArrayList<SpriteAnimationVO> tmpSpriteAnimVoList = new ArrayList<>();

    @Override
    protected String confirmDialogTitle() {
        return "Delete Sprite Animation";
    }

    @Override
    public void doAction() {
        String spriteAnimationName = notification.getBody();
        if (projectManager.deleteSpriteAnimationForAllResolutions(spriteAnimationName)) {
            deleteEntitiesWithSpriteAnimation(sandbox.getRootEntity(), spriteAnimationName);
            deleteAllItemsSpriteAnimations(spriteAnimationName);
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions(true);
            facade.sendNotification(DONE, spriteAnimationName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }

    private void deleteAllItemsSpriteAnimations(String spriteAnimationName) {
        for (CompositeItemVO compositeItemVO : libraryItems.values()) {
            deleteAllSpriteAnimationsOfItem(compositeItemVO, spriteAnimationName);
        }
    }

    private void deleteAllSpriteAnimationsOfItem(CompositeItemVO rootItemVo, String spriteAnimationName) {
        Consumer<CompositeItemVO> action = (currentItemVo) -> deleteCurrentItemSpriteAnimations(currentItemVo, spriteAnimationName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(rootItemVo, action);
    }

    private void deleteCurrentItemSpriteAnimations(CompositeItemVO compositeItemVO, String spriteAnimationName) {
        if (compositeItemVO.composite != null && compositeItemVO.composite.sSpriteAnimations.size() != 0) {
            ArrayList<SpriteAnimationVO> spriteAnimations = compositeItemVO.composite.sSpriteAnimations;

            tmpSpriteAnimVoList.addAll(spriteAnimations
                    .stream()
                    .filter(spriteVO -> spriteVO.animationName.equals(spriteAnimationName))
                    .collect(Collectors.toList()));

            spriteAnimations.removeAll(tmpSpriteAnimVoList);
            tmpSpriteAnimVoList.clear();
        }
    }

    private void deleteEntitiesWithSpriteAnimation(Entity rootEntity, String spriteAnimationName) {
        entityList.clear();
        Consumer<Entity> action = (root) -> {
            SpriteAnimationComponent spriteAnimationComponent = ComponentRetriever.get(root, SpriteAnimationComponent.class);
            if (spriteAnimationComponent != null && spriteAnimationComponent.animationName.equals(spriteAnimationName)) {
                entityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(entityList);
    }
}


