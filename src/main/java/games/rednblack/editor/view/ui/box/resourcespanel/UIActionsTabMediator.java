package games.rednblack.editor.view.ui.box.resourcespanel;

import org.puremvc.java.interfaces.INotification;

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
        /*searchText = searchText.toLowerCase();
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, CompositeItemVO> items = projectManager.currentProjectInfoVO.libraryItems;

        Array<DraggableResource> itemArray = new Array<>();
        for (String key : items.keySet()) {
            if(!key.toLowerCase().contains(searchText))continue;
            DraggableResource draggableResource = new DraggableResource(new LibraryItemResource(key));
            draggableResource.setFactoryFunction(ItemFactory.get()::createItemFromLibrary);
            draggableResource.initDragDrop();
            itemArray.add(draggableResource);
        }
        viewComponent.setItems(itemArray);*/
    }
}
