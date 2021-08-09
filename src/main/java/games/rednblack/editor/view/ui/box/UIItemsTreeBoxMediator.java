package games.rednblack.editor.view.ui.box;

import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.AddSelectionCommand;
import games.rednblack.editor.controller.commands.ItemsMoveCommand;
import games.rednblack.editor.controller.commands.ReleaseSelectionCommand;
import games.rednblack.editor.controller.commands.SetSelectionCommand;
import games.rednblack.editor.controller.commands.resource.DeleteResourceCommand;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MsgAPI;
import org.puremvc.java.interfaces.INotification;

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
                DeleteResourceCommand.DONE,
                MsgAPI.DELETE_ITEMS_COMMAND_DONE,
                MsgAPI.ACTION_Z_INDEX_CHANGED,
                MsgAPI.ACTION_CAMERA_CHANGE_COMPOSITE,
                MsgAPI.ITEM_DATA_UPDATED
        }).flatMap(Stream::of).toArray(String[]::new);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        switch (notification.getName()) {
            case MsgAPI.SCENE_LOADED:
                int rootEntity = sandbox.getCurrentViewingEntity();
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
            case DeleteResourceCommand.DONE:
                rootEntity = sandbox.getCurrentViewingEntity();
                if (notification.getType() == null || !notification.getType().equals(ItemsMoveCommand.TAG))
                    viewComponent.update(rootEntity);
                break;
            case UIItemsTreeBox.ITEMS_SELECTED:
                Selection<UIItemsTreeNode> selection = notification.getBody();
                Array<UIItemsTreeNode> nodes = selection.toArray();
                Set<Integer> items = new HashSet<>();

                for (UIItemsTreeNode node : nodes) {
                    Integer entityId = node.getValue().entityId;
                    int item = EntityUtils.getByUniqueId(entityId);
                    //layer lock thing
                    LayerItemVO layerItemVO = EntityUtils.getEntityLayer(item);
                    if(layerItemVO != null && layerItemVO.isLocked) {
                        continue;
                    }
                    if (item != -1) {
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

    private void sendSelectionNotification(Set<Integer> items) {
        Set<Integer> ntfItems = (items.isEmpty())? null : items;
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_SET_SELECTION, ntfItems);
    }
}