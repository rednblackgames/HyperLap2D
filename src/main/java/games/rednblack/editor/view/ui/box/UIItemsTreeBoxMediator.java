package games.rednblack.editor.view.ui.box;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import games.rednblack.h2d.common.MsgAPI;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddSelectionCommand;
import games.rednblack.editor.controller.commands.ReleaseSelectionCommand;
import games.rednblack.editor.controller.commands.SetSelectionCommand;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by sargis on 4/10/15.
 */
public class UIItemsTreeBoxMediator extends PanelMediator<UIItemsTreeBox> {
    private static final String TAG = UIItemsTreeBoxMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    public UIItemsTreeBoxMediator() {
        super(NAME, new UIItemsTreeBox());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] parentNotifications = super.listNotificationInterests();
        return Stream.of(parentNotifications, new String[]{
                MsgAPI.SCENE_LOADED,
                MsgAPI.NEW_ITEM_ADDED,
                UIItemsTreeBox.ITEMS_SELECTED,
                SetSelectionCommand.DONE,
                AddSelectionCommand.DONE,
                ReleaseSelectionCommand.DONE,
                MsgAPI.DELETE_ITEMS_COMMAND_DONE,
                MsgAPI.ACTION_Z_INDEX_CHANGED,
                MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE,
                MsgAPI.ITEM_DATA_UPDATED
        }).flatMap(Stream::of).toArray(String[]::new);
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                Entity rootEntity = sandbox.getCurrentViewingEntity();
                viewComponent.init(rootEntity);
                break;
            case MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE:
                rootEntity = notification.getBody();
                viewComponent.init(rootEntity);
                break;
            case MsgAPI.ITEM_DATA_UPDATED:
            case MsgAPI.ACTION_Z_INDEX_CHANGED:
            case MsgAPI.NEW_ITEM_ADDED:
            case MsgAPI.DELETE_ITEMS_COMMAND_DONE:
                rootEntity = sandbox.getCurrentViewingEntity();
                viewComponent.init(rootEntity);
                break;
            case UIItemsTreeBox.ITEMS_SELECTED:
                Selection<UIItemsTreeNode> selection = notification.getBody();
                Array<UIItemsTreeNode> nodes = selection.toArray();
                Set<Entity> items = new HashSet<>();

                for (UIItemsTreeNode node : nodes) {
                    Integer entityId = node.getValue().entityId;
                    Entity item = EntityUtils.getByUniqueId(entityId);
                    //layer lock thing
                    LayerItemVO layerItemVO = EntityUtils.getEntityLayer(item);
                    if(layerItemVO != null && layerItemVO.isLocked) {
                        continue;
                    }
                    if (item != null) {
                        items.add(item);
                    }
                }

                sendSelectionNotification(items);

                break;
            case SetSelectionCommand.DONE:
            case AddSelectionCommand.DONE:
            case ReleaseSelectionCommand.DONE:
                viewComponent.setSelection(sandbox.getSelector().getSelectedItems());
                break;
        }
    }

    private void sendSelectionNotification(Set<Entity> items) {
        Set<Entity> ntfItems = (items.isEmpty())? null : items;
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_SET_SELECTION, ntfItems);
    }
}