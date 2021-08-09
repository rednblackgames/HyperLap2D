/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.resource.DeleteLibraryItem;
import games.rednblack.editor.controller.commands.resource.ExportLibraryItemCommand;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.list.LibraryItemResource;
import games.rednblack.h2d.common.MsgAPI;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import java.util.HashMap;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UILibraryItemsTabMediator extends UIResourcesTabMediator<UILibraryItemsTab> {

    private static final String TAG = UILibraryItemsTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final Array<DraggableResource> itemArray = new Array<>();

    public UILibraryItemsTabMediator() {
        super(NAME, new UILibraryItemsTab());
    }

    @Override
    public String[] listNotificationInterests() {
        String[] listNotification = super.listNotificationInterests();

        listNotification = ArrayUtils.add(listNotification, MsgAPI.LIBRARY_LIST_UPDATED);
        listNotification = ArrayUtils.add(listNotification, DeleteLibraryItem.DONE);
        listNotification = ArrayUtils.add(listNotification, ExportLibraryItemCommand.DONE);

        return listNotification;
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case MsgAPI.LIBRARY_LIST_UPDATED:
            case DeleteLibraryItem.DONE:
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
        HashMap<String, CompositeItemVO> items = projectManager.currentProjectInfoVO.libraryItems;

        itemArray.clear();
        for (String key : items.keySet()) {
            if (!key.toLowerCase().contains(searchText)
                    || filterResource(key, EntityFactory.COMPOSITE_TYPE)) continue;
            DraggableResource draggableResource = new DraggableResource(new LibraryItemResource(key));
            draggableResource.setFactoryFunction(ItemFactory.get()::createItemFromLibrary);
            draggableResource.initDragDrop();
            itemArray.add(draggableResource);
        }
        itemArray.sort();
        viewComponent.setItems(itemArray);
    }
}
