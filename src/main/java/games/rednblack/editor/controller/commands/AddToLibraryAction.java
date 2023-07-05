package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.data.GraphVO;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.puremvc.Facade;

public class AddToLibraryAction extends NonRevertibleCommand {

    public AddToLibraryAction() {
        setShowConfirmDialog(false);
    }

    @Override
    public void doAction() {
        Object[] payload = notification.getBody();
        String key = (String) payload[0];
        GraphVO data = (GraphVO) payload[1];
        libraryActions.put(key, data);

        Facade.getInstance().sendNotification(MsgAPI.LIBRARY_ACTIONS_UPDATED);
    }

    public static Object[] getPayload(String key, GraphVO data) {
        Object[] payload = new Object[2];
        payload[0] = key;
        payload[1] = data;
        return payload;
    }
}
