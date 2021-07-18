package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.resource.DeleteLibraryAction;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.list.LibraryActionResource;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import java.util.HashMap;

public class UIActionsTabMediator extends UIResourcesTabMediator<UIActionsTab> {

    private static final String TAG = UIActionsTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final Array<DraggableResource> itemArray = new Array<>();

    public UIActionsTabMediator() {
        super(NAME, new UIActionsTab());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] listNotification = super.listNotificationInterests();

        listNotification = ArrayUtils.add(listNotification, MsgAPI.LIBRARY_ACTIONS_UPDATED);
        listNotification = ArrayUtils.add(listNotification, DeleteLibraryAction.DONE);

        return listNotification;
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
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
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
