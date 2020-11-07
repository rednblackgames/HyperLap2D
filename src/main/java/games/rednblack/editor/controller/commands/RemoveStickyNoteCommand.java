package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.StickyNoteVO;

public class RemoveStickyNoteCommand extends RevertibleCommand {
    private static final String prefix = "games.rednblack.editor.controller.commands.RemoveStickyNoteCommand";
    public static final String DONE = prefix + ".DONE";

    private StickyNoteVO backup;

    @Override
    public void doAction() {
        String id = notification.getBody();
        SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
        if (sceneVO.composite.sStickyNotes.get(id) == null) {
            cancel();
            return;
        }
        backup = new StickyNoteVO(sceneVO.composite.sStickyNotes.get(id));
        sceneVO.composite.sStickyNotes.remove(id);

        facade.sendNotification(RemoveStickyNoteCommand.DONE, id);
    }

    @Override
    public void undoAction() {
        SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
        sceneVO.composite.sStickyNotes.put(backup.id, backup);

        facade.sendNotification(CreateStickyNoteCommand.DONE, backup);
    }
}
