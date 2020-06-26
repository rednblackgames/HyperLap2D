package games.rednblack.editor.view.ui.box;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import games.rednblack.h2d.common.MsgAPI;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.CompositeCameraChangeCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;

/**
 * Created by CyberJoe on 4/22/2015.
 */
public class UICompositeHierarchyMediator extends SimpleMediator<UICompositeHierarchy> {
    private static final String TAG = UICompositeHierarchyMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private Sandbox sandbox;

    public UICompositeHierarchyMediator() {
        super(NAME, new UICompositeHierarchy());
    }

    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.SCENE_LOADED,
                CompositeCameraChangeCommand.DONE,
                UICompositeHierarchy.SWITCH_VIEW_COMPOSITE_CLICKED
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        sandbox = Sandbox.getInstance();

        super.handleNotification(notification);

        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                buildCompositeTree(sandbox.getRootEntity());
                break;
            case CompositeCameraChangeCommand.DONE:
                Integer entityId = notification.getBody();
                changeComposite(entityId);
                break;
            case UICompositeHierarchy.SWITCH_VIEW_COMPOSITE_CLICKED:
                entityId = notification.getBody();
                HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE, EntityUtils.getByUniqueId(entityId));
                break;
            default:
                break;
        }
    }

    private void buildCompositeTree(Entity entity) {
        viewComponent.clearItems();

        Array<CompositeHierarchyItem> items = new Array<>();

        ParentNodeComponent parentNodeComponent;
        Entity currEntity = entity;

        do {
            CompositeHierarchyItem item = new CompositeHierarchyItem(EntityUtils.getItemName(currEntity), EntityUtils.getEntityId(currEntity));

            parentNodeComponent = ComponentRetriever.get(currEntity, ParentNodeComponent.class);
            if (parentNodeComponent != null) {
                currEntity = parentNodeComponent.parentEntity;
                item.isRoot = true;
            }

            items.add(item);
        } while (parentNodeComponent != null);

        items.reverse();

        for (CompositeHierarchyItem item : items) {
            viewComponent.addItem(item.name, item.entityId, item.isRoot);
        }
    }

    private void changeComposite(Integer entityId) {
        buildCompositeTree(EntityUtils.getByUniqueId(entityId));
    }

    private static class CompositeHierarchyItem {
        String name;
        int entityId;
        boolean isRoot = false;

        public CompositeHierarchyItem(String name, int entityId) {
            this.entityId = entityId;
            this.name = name;
        }
    }

    public void updateOriginalItem() {
    	//TODO fix and uncomment
        //updateOriginalItem(scenes.get(scenes.size() - 1), commands.sceneControl.getCurrentScene());
    }

    private void updateOriginalItem(CompositeItemVO updatableVo, Entity currItem) {
    	//TODO fix and uncomment
//        updatableVo.update(new CompositeItemVO(currItem.getDataVO().composite));
//
//        String libName = currItem.getDataVO().libraryLink;
//        CompositeItemVO libItem = commands.sceneControl.getCurrentSceneVO().libraryItems.get(libName);
//
//        if (libItem != null) {
//            libItem.update(currItem.getDataVO());
//
//
//            //TODO: update other items with same name
//            revursiveUpdateLibraryVO(libName, commands.sceneControl.getRootSceneVO(), currItem.getDataVO());
//        }
    }

    /*
    private void revursiveUpdateLibraryVO(String libName, CompositeItemVO initialVO, CompositeItemVO updatingWith) {
        for (int i = 0; i < initialVO.composite.sComposites.size(); i++) {
            if (initialVO.composite.sComposites.get(i).libraryLink.equals(libName)) {
                initialVO.composite.sComposites.get(i).update(updatingWith);
            } else {
                revursiveUpdateLibraryVO(libName, initialVO.composite.sComposites.get(i), updatingWith);
            }
        }
    }*/
}
