package games.rednblack.editor.controller.commands.resource;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.components.SpineDataComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.SpineVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Sasun Poghosyan on 5/10/2016.
 */
public class DeleteSpineAnimation extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteSpineAnimation";
    public static final String DONE = CLASS_NAME + "DONE";

    private final ArrayList<Entity> entityList = new ArrayList<>();
    private final ArrayList<SpineVO> tmpSpineAnimList = new ArrayList<>();

    @Override
    protected String confirmDialogTitle() {
        return "Delete Spine Animation";
    }

    @Override
    public void doAction() {
        String spineItemName = notification.getBody();
        if (projectManager.deleteSpineForAllResolutions(spineItemName)) {
            deleteEntitiesWithParticleEffects(sandbox.getRootEntity(), spineItemName);
            deleteAllItemsSpineAnimations(spineItemName);
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions(true);
            sendNotification(DONE, spineItemName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }

    private void deleteAllItemsSpineAnimations(String spineAnimationName) {
        for (CompositeItemVO compositeItemVO : libraryItems.values()) {
            deleteAllSpineAnimationsOfItem(compositeItemVO, spineAnimationName);
        }
    }

    private void deleteAllSpineAnimationsOfItem(CompositeItemVO compositeItemVO, String spineAnimationName) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> deleteCurrentItemSpineAnimations(rootItemVo, spineAnimationName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void deleteCurrentItemSpineAnimations(CompositeItemVO compositeItemVO, String spineAnimationName) {
        if (compositeItemVO.composite != null && compositeItemVO.composite.sSpineAnimations.size() != 0) {
            ArrayList<SpineVO> spineAnimations = compositeItemVO.composite.sSpineAnimations;
            tmpSpineAnimList.addAll(spineAnimations
                    .stream()
                    .filter(spineVO -> spineVO.animationName.equals(spineAnimationName))
                    .collect(Collectors.toList()));
            spineAnimations.removeAll(tmpSpineAnimList);
            tmpSpineAnimList.clear();
        }
    }

    private void deleteEntitiesWithParticleEffects(Entity rootEntity, String particleName) {
        entityList.clear();
        Consumer<Entity> action = (root) -> {
            SpineDataComponent spineDataComponent = ComponentRetriever.get(root, SpineDataComponent.class);
            if (spineDataComponent != null && spineDataComponent.animationName.equals(particleName)) {
                entityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(entityList);
    }
}


