package games.rednblack.editor.controller.commands;

import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.StickyNoteVO;
import games.rednblack.editor.view.ui.widget.actors.StickyNoteActor;

public class ModifyStickyNoteCommand extends RevertibleCommand {
    private static final String prefix = "games.rednblack.editor.controller.commands.ModifyStickyNoteCommand";
    public static final String DONE = prefix + ".DONE";

    private StickyNoteVO backup;

    @Override
    public void doAction() {
        StickyNoteVO payload = notification.getBody();
        SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
        StickyNoteVO noteVO = sceneVO.composite.sStickyNotes.get(payload.id);

        if (payload.equals(noteVO)) {
            cancel();
            return;
        }

        backup = new StickyNoteVO(noteVO);

        noteVO.width = payload.width;
        noteVO.height = payload.height;
        noteVO.x = payload.x;
        noteVO.y = payload.y;
        noteVO.content = payload.content;

        facade.sendNotification(DONE, noteVO);
    }

    @Override
    public void undoAction() {
        SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
        StickyNoteVO noteVO = sceneVO.composite.sStickyNotes.get(backup.id);

        noteVO.width = backup.width;
        noteVO.height = backup.height;
        noteVO.x = backup.x;
        noteVO.y = backup.y;
        noteVO.content = backup.content;

        facade.sendNotification(DONE, noteVO);
    }

    public static StickyNoteVO payload(StickyNoteActor noteActor) {
        StickyNoteVO noteVO = new StickyNoteVO();

        noteVO.id = noteActor.id;
        noteVO.width = noteActor.getWidth();
        noteVO.height = noteActor.getHeight();
        noteVO.x = noteActor.getWorldX();
        noteVO.y = noteActor.getWorldY();
        noteVO.content = noteActor.getContent();

        return noteVO;
    }
}
