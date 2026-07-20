package games.rednblack.editor.view.stage;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.editor.view.stage.input.EntityClickListener;
import games.rednblack.editor.view.stage.tools.PanTool;
import games.rednblack.h2d.common.MsgAPI;


public class SandboxItemEventListener extends EntityClickListener {

    private final SandboxMediator mediator;

    public SandboxItemEventListener(SandboxMediator mediator, final int entity) {
        this.mediator = mediator;
    }

    @Override
    public boolean touchDown(int entity, float x, float y, int pointer, int button) {
        super.touchDown(entity, x, y, pointer, button);

        mediator.setSandboxFocus();

        switch (button) {
            case Input.Buttons.MIDDLE:
                // if middle button is pressed - PAN the scene
                mediator.toolHotSwap(mediator.sandboxTools.get(PanTool.NAME));
                break;
        }

        Vector2 coords = mediator.getStageCoordinates();
        return mediator.currentSelectedTool != null && mediator.currentSelectedTool.itemMouseDown(entity, coords.x, coords.y);
    }

    
    @Override
    public void touchUp(int entity, float x, float y, int pointer, int button) {
        super.touchUp(entity, x, y, pointer, button);
        Vector2 coords = mediator.getStageCoordinates();

        if (button == Input.Buttons.MIDDLE) {
            mediator.toolHotSwapBack();
        }

        if (mediator.currentSelectedTool != null) {
            mediator.currentSelectedTool.itemMouseUp(entity, x, y);

            if (getTapCount() == 2) {
                // this is double click
                mediator.currentSelectedTool.itemMouseDoubleClick(entity, coords.x, coords.y);
            }
        }

        if (button == Input.Buttons.RIGHT) {
            // if right clicked on an item, drop down for current selection
            mediator.getFacade().sendNotification(MsgAPI.ITEM_RIGHT_CLICK);
        }
    }

    @Override
    public void touchDragged(int entity, float x, float y, int pointer) {
        Vector2 coords = mediator.getStageCoordinates();

        if (mediator.currentSelectedTool != null) {
            mediator.currentSelectedTool.itemMouseDragged(entity, coords.x, coords.y);
        }
    }

    @Override
    public boolean scrolled(int entity, float amountX, float amountY) {

        return false;
    }
}
