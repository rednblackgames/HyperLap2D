package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;

import java.util.HashMap;

public class DeleteLibraryAction extends DeleteResourceCommand {

    private static final String CLASS_NAME = "games.rednblack.editor.controller.commands.resource.DeleteLibraryAction";
    public static final String DONE = CLASS_NAME + "DONE";

    @Override
    protected String confirmDialogTitle() {
        return "Delete Library Action";
    }

    @Override
    public void doAction() {
        String libraryActionName = notification.getBody();

        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, String> libraryActions = projectManager.currentProjectInfoVO.libraryActions;

        libraryActions.remove(libraryActionName);

        facade.sendNotification(DONE, libraryActionName);
    }
}
