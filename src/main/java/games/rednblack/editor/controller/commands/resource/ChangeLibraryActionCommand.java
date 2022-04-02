package games.rednblack.editor.controller.commands.resource;

import games.rednblack.editor.controller.commands.HistoricRevertibleCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.h2d.common.MsgAPI;

public class ChangeLibraryActionCommand extends HistoricRevertibleCommand {

    private final ProjectManager projectManager;

    public ChangeLibraryActionCommand() {
        projectManager = facade.retrieveProxy(ProjectManager.NAME);
    }

    @Override
    public void doAction() {
        String[] payload = notification.getBody();
        String oldName = payload[0];
        String newName = payload[1];

        GraphVO action = projectManager.currentProjectInfoVO.libraryActions.get(oldName);
        if (action != null) {
            projectManager.currentProjectInfoVO.libraryActions.remove(oldName);
            projectManager.currentProjectInfoVO.libraryActions.put(newName, action);

            facade.sendNotification(MsgAPI.LIBRARY_ACTIONS_UPDATED);
        }
    }

    @Override
    public void undoAction() {
        String[] payload = notification.getBody();
        String oldName = payload[0];
        String newName = payload[1];

        GraphVO action = projectManager.currentProjectInfoVO.libraryActions.get(newName);
        if (action != null) {
            projectManager.currentProjectInfoVO.libraryActions.remove(newName);
            projectManager.currentProjectInfoVO.libraryActions.put(oldName, action);

            facade.sendNotification(MsgAPI.LIBRARY_ACTIONS_UPDATED);
        }
    }
}
