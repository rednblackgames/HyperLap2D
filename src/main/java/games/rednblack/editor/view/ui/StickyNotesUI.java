package games.rednblack.editor.view.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import games.rednblack.editor.renderer.data.StickyNoteVO;
import games.rednblack.editor.view.ui.widget.actors.StickyNoteActor;

public class StickyNotesUI extends Group {

    public StickyNotesUI() {
    }

    public void attachNote(StickyNoteVO noteVO) {
        StickyNoteActor note = new StickyNoteActor(noteVO.id);
        note.setPosition(noteVO.x, noteVO.y);
        note.setSize(noteVO.width, noteVO.height);
        note.setContent(noteVO.content);
        note.setColor(noteVO.tint[0], noteVO.tint[1], noteVO.tint[2], noteVO.tint[3]);
        note.show(this);
    }

    public void updateNote(StickyNoteVO noteVO) {
        StickyNoteActor note = getNoteActor(noteVO.id);
        if (note != null) {
            note.setPosition(noteVO.x, noteVO.y);
            note.setSize(noteVO.width, noteVO.height);
            note.setContent(noteVO.content);
            note.setColor(noteVO.tint[0], noteVO.tint[1], noteVO.tint[2], noteVO.tint[3]);
        }
    }

    public void removeNote(String id) {
        StickyNoteActor note = getNoteActor(id);

        if (note != null)
            note.close();
    }

    private StickyNoteActor getNoteActor(String id) {
        StickyNoteActor note = null;
        for (Actor a : getChildren()) {
            if (a instanceof StickyNoteActor) {
                if (((StickyNoteActor) a).id.equals(id)) {
                    note = (StickyNoteActor) a;
                    break;
                }
            }
        }
        return note;
    }
}
