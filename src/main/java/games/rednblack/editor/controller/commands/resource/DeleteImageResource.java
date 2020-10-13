package games.rednblack.editor.controller.commands.resource;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.SimpleImageVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by azakhary on 11/29/2015.
 */
public class DeleteImageResource extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteImageResource";
    public static final String DONE = CLASS_NAME + "DONE";

    private final ArrayList<Entity> tmpEntityList = new ArrayList<>();
    private final ArrayList<SimpleImageVO> tmpImageList = new ArrayList<>();

    @Override
    protected String confirmDialogTitle() {
        return "Delete Image Resource";
    }

    @Override
    public void doAction() {
        String imageName = notification.getBody();
        if (projectManager.deleteSingleImageForAllResolutions(imageName)) {
            deleteEntitiesWithImages(sandbox.getRootEntity(), imageName);
            deleteAllItemsImages(imageName);
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            resolutionManager.rePackProjectImagesForAllResolutions(true);
            sendNotification(DONE, imageName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }

    private void deleteAllItemsImages(String imageName) {
        for (CompositeItemVO compositeItemVO : libraryItems.values()) {
            deleteAllImagesOfItem(compositeItemVO, imageName);
        }
    }

    private void deleteAllImagesOfItem(CompositeItemVO compositeItemVO, String imageName) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> deleteCurrentItemImage(rootItemVo, imageName);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void deleteCurrentItemImage(CompositeItemVO compositeItemVO, String imageName) {
        if (compositeItemVO.composite != null && compositeItemVO.composite.sImages.size() != 0) {
            ArrayList<SimpleImageVO> simpleImageVOs = compositeItemVO.composite.sImages;
            tmpImageList.addAll(simpleImageVOs
                    .stream()
                    .filter(simpleImageVO -> simpleImageVO.imageName.equals(imageName))
                    .collect(Collectors.toList()));
            simpleImageVOs.removeAll(tmpImageList);
            tmpImageList.clear();
        }
    }

    private void deleteEntitiesWithImages(Entity rootEntity, String regionName) {
        tmpEntityList.clear();
        Consumer<Entity> action = (root) -> {
            TextureRegionComponent regionComponent = ComponentRetriever.get(root, TextureRegionComponent.class);
            if (regionComponent != null && regionComponent.regionName.equals(regionName)) {
                tmpEntityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(tmpEntityList);
    }
}
