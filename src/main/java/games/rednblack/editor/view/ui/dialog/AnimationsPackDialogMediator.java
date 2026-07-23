package games.rednblack.editor.view.ui.dialog;
import com.badlogic.gdx.utils.ObjectSet;
import games.rednblack.editor.proxy.PluginUIBridge;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.controller.commands.resource.DeleteImageResource;
import games.rednblack.editor.controller.commands.resource.DeleteSpineAnimation;
import games.rednblack.editor.controller.commands.resource.DeleteSpriteAnimation;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.renderer.data.TexturePackVO;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Mediator;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;

import java.util.HashMap;
import java.util.HashSet;

public class AnimationsPackDialogMediator extends Mediator<AtlasesPackDialog> {
    private static final String TAG = AnimationsPackDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    private static final String NEW_IMAGES_PACK = "games.rednblack.editor.view.ui.dialog.AnimationsPackDialogMediator.NEW_IMAGES_PACK";
    private static final String MOVE_REGION_TO_PACK = "games.rednblack.editor.view.ui.dialog.AnimationsPackDialogMediator.MOVE_REGION_TO_PACK";
    private static final String UPDATE_CURRENT_LIST = "games.rednblack.editor.view.ui.dialog.AnimationsPackDialogMediator.UPDATE_CURRENT_LIST";
    private static final String REMOVE_PACK = "games.rednblack.editor.view.ui.dialog.AnimationsPackDialogMediator.REMOVE_PACK";
    private static final String APPLY_PACKS = "games.rednblack.editor.view.ui.dialog.AnimationsPackDialogMediator.APPLY_PACKS";

    /** Deep copy of the live animations packs, edited in place by the dialog and committed on Apply/OK. */
    private final HashMap<String, TexturePackVO> editablePacks = new HashMap<>();
    /** VO pack names touched since the dialog opened; scopes the single repack fired on commit. */
    private final ObjectSet<String> pendingPacks = new ObjectSet<>();

    public AnimationsPackDialogMediator() {
        super(NAME, new AtlasesPackDialog("Animations Atlases", NEW_IMAGES_PACK, MOVE_REGION_TO_PACK, UPDATE_CURRENT_LIST, REMOVE_PACK, APPLY_PACKS));
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        interests.add(ResourcesMenu.OPEN_ANIMATIONS_PACK,
                ProjectManager.PROJECT_OPENED,
                NEW_IMAGES_PACK,
                MOVE_REGION_TO_PACK);
        interests.add(APPLY_PACKS);
        interests.add(UPDATE_CURRENT_LIST,
                REMOVE_PACK,
                MsgAPI.UPDATE_ATLAS_PACK_LIST,
                ProjectManager.PROJECT_DATA_UPDATED);
        interests.add(DeleteImageResource.DONE,
                DeleteSpineAnimation.DONE,
                DeleteSpriteAnimation.DONE);
    }

    private HashMap<String, TexturePackVO> livePacks() {
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        return projectManager.currentProjectInfoVO.animationsPacks;
    }

    /** Re-seeds the editable copy (and clears pending) from the committed live packs. */
    private void resetEditable() {
        editablePacks.clear();
        pendingPacks.clear();
        for (TexturePackVO vo : livePacks().values()) {
            TexturePackVO copy = new TexturePackVO();
            copy.name = vo.name;
            copy.regions = new HashSet<>(vo.regions);
            editablePacks.put(copy.name, copy);
        }
    }

    /** Rebuilds tabs + lists from the editable copy (used when the pack set may have changed). */
    private void populateView() {
        viewComponent.initPacks(editablePacks.keySet());
        viewComponent.updateMainPack(editablePacks.get("main").regions);
        if (viewComponent.getSelectedTab() == null)
            viewComponent.clearCurrentPack();
    }

    /** Writes the editable copy back into the live VO, saves, and fires one scoped repack. */
    private void commit() {
        HashMap<String, TexturePackVO> live = livePacks();
        live.clear();
        for (TexturePackVO vo : editablePacks.values()) {
            TexturePackVO copy = new TexturePackVO();
            copy.name = vo.name;
            copy.regions = new HashSet<>(vo.regions);
            live.put(copy.name, copy);
        }
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        projectManager.saveCurrentProject();
        if (pendingPacks.size > 0) {
            ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
            // copy: the repack runs async and reads forcePacks on its thread
            ObjectSet<String> forcePacks = new ObjectSet<>(pendingPacks);
            resolutionManager.rePackProjectImagesForAllResolutions(true, forcePacks, null);
        }
        resetEditable();
    }

    @Override
    public void handleNotification(INotification notification) {
        Sandbox sandbox = PluginUIBridge.get().getSandbox();
        UIStage uiStage = sandbox.getUIStage();
        String currentTab;
        switch (notification.getName()) {
            case ResourcesMenu.OPEN_ANIMATIONS_PACK:
                resetEditable();
                viewComponent.show(uiStage);
                populateView();
                break;
            case ProjectManager.PROJECT_DATA_UPDATED:
            case DeleteImageResource.DONE:
            case DeleteSpineAnimation.DONE:
            case DeleteSpriteAnimation.DONE:
                // refresh region contents from live without rebuilding tabs (keep selection)
                resetEditable();
                viewComponent.updateMainPack(editablePacks.get("main").regions);
                currentTab = viewComponent.getSelectedTab();
                if (currentTab != null)
                    viewComponent.updateCurrentPack(editablePacks.get(currentTab).regions);
                else
                    viewComponent.clearCurrentPack();
                break;
            case MsgAPI.UPDATE_ATLAS_PACK_LIST:
            case ProjectManager.PROJECT_OPENED:
                resetEditable();
                populateView();
                break;
            case NEW_IMAGES_PACK:
                TexturePackVO newVo = new TexturePackVO();
                newVo.name = notification.getBody();

                editablePacks.put(newVo.name, newVo);
                pendingPacks.add(newVo.name);

                viewComponent.addNewPack(newVo.name);
                break;
            case UPDATE_CURRENT_LIST:
                currentTab = viewComponent.getSelectedTab();
                if (currentTab != null)
                    viewComponent.updateCurrentPack(editablePacks.get(currentTab).regions);
                break;
            case MOVE_REGION_TO_PACK:
                String toPack = viewComponent.getMainSelected().size > 0 ? viewComponent.getSelectedTab() : "main";
                String fromPack = viewComponent.getMainSelected().size == 0 ? viewComponent.getSelectedTab() : "main";
                Array<String> regions = viewComponent.getMainSelected().size > 0 ? viewComponent.getMainSelected() : viewComponent.getCurrentSelected();

                for (String region : regions) {
                    editablePacks.get(fromPack).regions.remove(region);
                    editablePacks.get(toPack).regions.add(region);
                }
                pendingPacks.add(fromPack);
                pendingPacks.add(toPack);

                viewComponent.updateCurrentPack(editablePacks.get(viewComponent.getSelectedTab()).regions);
                viewComponent.updateMainPack(editablePacks.get("main").regions);
                break;
            case REMOVE_PACK:
                String packToRemove = notification.getBody();
                editablePacks.get("main").regions.addAll(editablePacks.get(packToRemove).regions);
                editablePacks.remove(packToRemove);
                pendingPacks.add("main");

                viewComponent.updateMainPack(editablePacks.get("main").regions);
                viewComponent.clearCurrentPack();
                break;
            case APPLY_PACKS:
                commit();
                break;
        }
    }
}