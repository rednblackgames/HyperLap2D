package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.CompositeCameraChangeCommand;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

/**
 * Created by CyberJoe on 4/22/2015.
 */
public class UICompositeHierarchyMediator extends Mediator<UICompositeHierarchy> {
    private static final String TAG = UICompositeHierarchyMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private Sandbox sandbox;

    public UICompositeHierarchyMediator() {
        super(NAME, new UICompositeHierarchy());
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                MsgAPI.SCENE_LOADED,
                CompositeCameraChangeCommand.DONE,
                UICompositeHierarchy.SWITCH_VIEW_COMPOSITE_CLICKED
        };
    }

    @Override
    public void handleNotification(INotification notification) {
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

    private void buildCompositeTree(int entity) {
        viewComponent.clearItems();

        Array<CompositeHierarchyItem> items = new Array<>();

        ParentNodeComponent parentNodeComponent;
        int currEntity = entity;

        do {
            CompositeHierarchyItem item = new CompositeHierarchyItem(EntityUtils.getItemName(currEntity), EntityUtils.getEntityId(currEntity));

            parentNodeComponent = SandboxComponentRetriever.get(currEntity, ParentNodeComponent.class);
            if (parentNodeComponent != null) {
                currEntity = parentNodeComponent.parentEntity;
                item.isRoot = false;
            } else {
                item.isRoot = true;
            }

            items.add(item);
        } while (parentNodeComponent != null && currEntity != -1);

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
}
