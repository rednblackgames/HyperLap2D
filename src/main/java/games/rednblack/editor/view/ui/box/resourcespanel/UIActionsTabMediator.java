package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.controller.commands.resource.DeleteLibraryAction;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.list.LibraryActionResource;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;
import games.rednblack.puremvc.interfaces.INotification;
import games.rednblack.puremvc.util.Interests;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashMap;

public class UIActionsTabMediator extends UIResourcesTabMediator<UIActionsTab> {

    private static final String TAG = UIActionsTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final Array<DraggableResource> itemArray = new Array<>();

    public UIActionsTabMediator() {
        super(NAME, new UIActionsTab());
    }

    @Override
    public void listNotificationInterests(Interests interests) {
        super.listNotificationInterests(interests);
        interests.add(MsgAPI.LIBRARY_ACTIONS_UPDATED, DeleteLibraryAction.DONE);
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case MsgAPI.LIBRARY_ACTIONS_UPDATED:
            case DeleteLibraryAction.DONE:
                initList(viewComponent.searchString);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initList(String searchText) {
        searchText = searchText.toLowerCase();
        ProjectManager projectManager = Facade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, GraphVO> items = projectManager.currentProjectInfoVO.libraryActions;

        itemArray.clear();
        for (String key : items.keySet()) {
            if(!key.toLowerCase().contains(searchText))continue;
            DraggableResource draggableResource = new DraggableResource(new LibraryActionResource(key));
            itemArray.add(draggableResource);
        }
        itemArray.sort();
        viewComponent.setItems(itemArray);
    }
}
