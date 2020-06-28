package games.rednblack.editor.controller.commands.resource;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.data.CompositeItemVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.utils.runtime.EntityUtils;

import java.util.HashMap;

/**
 * Created by azakhary on 11/29/2015.
 */
public class DeleteLibraryItem extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteLibraryItem";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete Library Item";
    }

    @Override
    public void doAction() {
        String libraryItemName = notification.getBody();

        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, CompositeItemVO> libraryItems = projectManager.currentProjectInfoVO.libraryItems;

        libraryItems.remove(libraryItemName);

        Array<Entity> linkedEntities = EntityUtils.getByLibraryLink(libraryItemName);
        for (Entity entity : linkedEntities) {
            MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
            mainItemComponent.libraryLink = "";
        }
        facade.sendNotification(DONE, libraryItemName);
    }
}
