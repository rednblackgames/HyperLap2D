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

        if (noteVO == null || payload.equals(noteVO)) {
            cancel();
            return;
        }

        backup = new StickyNoteVO(noteVO);

        noteVO.width = payload.width;
        noteVO.height = payload.height;
        noteVO.x = payload.x;
        noteVO.y = payload.y;
        noteVO.content = payload.content;
        noteVO.tint[0] = payload.tint[0];
        noteVO.tint[1] = payload.tint[1];
        noteVO.tint[2] = payload.tint[2];
        noteVO.tint[3] = payload.tint[3];

        facade.sendNotification(DONE, noteVO);
    }

    @Override
    public void undoAction() {
        SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
        StickyNoteVO noteVO = sceneVO.composite.sStickyNotes.get(backup.id);

        if (noteVO == null || backup.equals(noteVO)) {
            cancel();
            return;
        }

        noteVO.width = backup.width;
        noteVO.height = backup.height;
        noteVO.x = backup.x;
        noteVO.y = backup.y;
        noteVO.content = backup.content;
        noteVO.tint[0] = backup.tint[0];
        noteVO.tint[1] = backup.tint[1];
        noteVO.tint[2] = backup.tint[2];
        noteVO.tint[3] = backup.tint[3];

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
        noteVO.tint = new float[4];
        noteVO.tint[0] = noteActor.getColor().r;
        noteVO.tint[1] = noteActor.getColor().g;
        noteVO.tint[2] = noteActor.getColor().b;
        noteVO.tint[3] = noteActor.getColor().a;

        return noteVO;
    }
}
