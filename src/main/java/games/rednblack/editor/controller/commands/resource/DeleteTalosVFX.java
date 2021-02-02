package games.rednblack.editor.controller.commands.resource;

import com.badlogic.ashley.core.Entity;
import games.rednblack.editor.renderer.components.particle.TalosDataComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.TalosVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

public class DeleteTalosVFX extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteTalosVFX";
    public static final String DONE = CLASS_NAME + "DONE";

    private final ArrayList<Entity> entityList = new ArrayList<>();
    private final ArrayList<TalosVO> tmpParticleEffectList = new ArrayList<>();

    @Override
    protected String confirmDialogTitle() {
        return "Delete Talos VFX";
    }

    @Override
    public void doAction() {
        String particleName = notification.getBody();
        if (projectManager.deleteTalosVFX(particleName)) {
            deleteEntitiesWithParticleEffects(sandbox.getRootEntity(), particleName); // delete entities from scene
            deleteAllItemsWithParticleName(particleName);
            projectManager.loadProjectData(projectManager.getCurrentProjectPath());
            sendNotification(DONE, particleName);
            SceneVO vo = sandbox.sceneVoFromItems();
            projectManager.saveCurrentProject(vo);
        } else {
            cancel();
        }
    }

    private void deleteAllItemsWithParticleName(String name) {
        for (CompositeItemVO compositeItemVO : libraryItems.values()) {
            deleteAllParticles(compositeItemVO, name);
        }
    }

    private void deleteAllParticles(CompositeItemVO compositeItemVO, String name) {
        Consumer<CompositeItemVO> action = (rootItemVo) -> getParticles(rootItemVo, name);
        EntityUtils.applyActionRecursivelyOnLibraryItems(compositeItemVO, action);
    }

    private void getParticles(CompositeItemVO compositeItemVO, String name) {
        if (compositeItemVO.composite != null && compositeItemVO.composite.sTalosVFX.size() != 0) {
            ArrayList<TalosVO> particleEffectList = compositeItemVO.composite.sTalosVFX;
            for (TalosVO particleEffectVO : particleEffectList) {
                if (particleEffectVO.particleName.equals(name)) {
                    tmpParticleEffectList.add(particleEffectVO);
                }
            }
            particleEffectList.removeAll(tmpParticleEffectList);
            tmpParticleEffectList.clear();
        }
    }

    private void deleteEntitiesWithParticleEffects(Entity rootEntity, String particleName) {
        entityList.clear();
        Consumer<Entity> action = (root) -> {
            TalosDataComponent particleComponent = ComponentRetriever.get(root, TalosDataComponent.class);
            if (particleComponent != null && particleComponent.particleName.equals(particleName)) {
                entityList.add(root);
            }
        };
        EntityUtils.applyActionRecursivelyOnEntities(rootEntity, action);
        EntityUtils.removeEntities(entityList);
    }
}
