package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.list.LibraryItemResource;
import org.puremvc.java.interfaces.INotification;

import java.util.HashMap;

public class UIActionsTabMediator extends UIResourcesTabMediator<UIActionsTab> {

    private static final String TAG = UIActionsTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;


    public UIActionsTabMediator() {
        super(NAME, new UIActionsTab());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] listNotification = super.listNotificationInterests();


        return listNotification;
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            default:
                break;
        }
    }

    @Override
    protected void initList(String searchText) {
        searchText = searchText.toLowerCase();
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, String> items = projectManager.currentProjectInfoVO.libraryActions;

        Array<DraggableResource> itemArray = new Array<>();
        for (String key : items.keySet()) {
            if(!key.toLowerCase().contains(searchText))continue;
            DraggableResource draggableResource = new DraggableResource(new LibraryItemResource(key));
            itemArray.add(draggableResource);
        }
        viewComponent.setItems(itemArray);
    }
}
