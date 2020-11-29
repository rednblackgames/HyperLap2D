package games.rednblack.editor.controller.commands;

import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.renderer.data.StickyNoteVO;

import java.util.UUID;

public class CreateStickyNoteCommand extends RevertibleCommand {
    private static final String prefix = "games.rednblack.editor.controller.commands.CreateStickyNoteCommand";
    public static final String DONE = prefix + ".DONE";

    private String id;

    @Override
    public void doAction() {
        id = UUID.randomUUID().toString().replace("-", "");
        Vector2 position = notification.getBody();

        SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
        StickyNoteVO noteVO = new StickyNoteVO();
        noteVO.id = id;
        noteVO.x = position.x - 9f / sandbox.getPixelPerWU();
        noteVO.y = position.y - (noteVO.height - 9f) / sandbox.getPixelPerWU();

        sceneVO.composite.sStickyNotes.put(id, noteVO);

        facade.sendNotification(DONE, noteVO);
    }

    @Override
    public void undoAction() {
        SceneVO sceneVO = sandbox.getSceneControl().getCurrentSceneVO();
        sceneVO.composite.sStickyNotes.remove(id);

        facade.sendNotification(RemoveStickyNoteCommand.DONE, id);
    }
}
