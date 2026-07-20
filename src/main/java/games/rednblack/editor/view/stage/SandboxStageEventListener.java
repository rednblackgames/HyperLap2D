package games.rednblack.editor.view.stage;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.editor.view.stage.input.EntityClickListener;
import games.rednblack.editor.view.stage.tools.PanTool;
import games.rednblack.editor.view.stage.tools.SelectionTool;
import games.rednblack.editor.view.stage.tools.TransformTool;
import games.rednblack.h2d.common.MsgAPI;


class SandboxStageEventListener extends EntityClickListener {

    private final SandboxMediator mediator;

    public SandboxStageEventListener(SandboxMediator mediator) {
        this.mediator = mediator;
        setTapCountInterval(.5f);
    }

    @Override
    public boolean keyDown(int entity, int keycode) {
        Sandbox sandbox = mediator.getViewComponent();
        if (sandbox.getSceneControl().getCurrentSceneVO() == null) {
            return false;
        }

        mediator.getFacade().sendNotification(MsgAPI.ACTION_KEY_DOWN, keycode);

        if(mediator.currentSelectedTool != null) {
            mediator.currentSelectedTool.keyDown(entity, keycode);
        }

        switch (KeyBindingsLayout.mapAction(keycode)) {
            case KeyBindingsLayout.SELECTION_TOOL:
                mediator.getFacade().sendNotification(MsgAPI.TOOL_CLICKED, SelectionTool.NAME);
                break;
            case KeyBindingsLayout.TRANSFORM_TOOL:
                mediator.getFacade().sendNotification(MsgAPI.TOOL_CLICKED, TransformTool.NAME);
                break;
            case KeyBindingsLayout.PAN_TOOL:
                mediator.toolHotSwap(mediator.sandboxTools.get(PanTool.NAME));
                break;
            case KeyBindingsLayout.ZOOM_PLUS:
                sandbox.zoomDivideBy(2f);
                break;
            case KeyBindingsLayout.ZOOM_MINUS:
                sandbox.zoomDivideBy(0.5f);
                break;
            case KeyBindingsLayout.Z_INDEX_UP:
                // going to front of next item in z-index ladder
                mediator.getFacade().sendNotification(MsgAPI.ACTION_SET_Z_INDEX, new Object[]{sandbox.getSelector().getCurrentSelection(), true});
                break;
            case KeyBindingsLayout.Z_INDEX_DOWN:
                // going behind the next item in z-index ladder
                mediator.getFacade().sendNotification(MsgAPI.ACTION_SET_Z_INDEX, new Object[]{sandbox.getSelector().getCurrentSelection(), false});
                break;
            case KeyBindingsLayout.SELECT_ALL:
                // Ctrl+A means select all
                mediator.getFacade().sendNotification(MsgAPI.ACTION_SET_SELECTION, sandbox.getSelector().getAllFreeItems());
                break;
            case KeyBindingsLayout.COPY:
                mediator.getFacade().sendNotification(MsgAPI.ACTION_COPY);
                break;
            case KeyBindingsLayout.CUT:
                mediator.getFacade().sendNotification(MsgAPI.ACTION_CUT);
                break;
            case KeyBindingsLayout.PASTE:
                mediator.getFacade().sendNotification(MsgAPI.ACTION_PASTE);
                break;
            case KeyBindingsLayout.UNDO:
                CommandManager commandManager = mediator.getFacade().retrieveProxy(CommandManager.NAME);
                commandManager.undoCommand();
                break;
            case KeyBindingsLayout.REDO:
                commandManager = mediator.getFacade().retrieveProxy(CommandManager.NAME);
                commandManager.redoCommand();
                break;
            case KeyBindingsLayout.RESET_CAMERA:
                sandbox.getCamera().position.set(0 ,0, 0);
                sandbox.setZoomPercent(100, false);
                break;
            case KeyBindingsLayout.ALIGN_TOP:
                sandbox.getSelector().alignSelections(Align.top);
                break;
            case KeyBindingsLayout.ALIGN_LEFT:
                sandbox.getSelector().alignSelections(Align.left);
                break;
            case KeyBindingsLayout.ALIGN_BOTTOM:
                sandbox.getSelector().alignSelections(Align.bottom);
                break;
            case KeyBindingsLayout.ALIGN_RIGHT:
                sandbox.getSelector().alignSelections(Align.right);
                break;
        }

        if (keycode == Input.Keys.ESCAPE) {
            if (sandbox.getSelector().getSelectedItems().size() > 0) {
                mediator.getFacade().sendNotification(MsgAPI.ACTION_SET_SELECTION, null);
            } else {
                mediator.currentSelectedTool.stageMouseDoubleClick(0, 0);
            }
        }
        return true;
    }

    @Override
    public boolean keyUp(int entity, int keycode) {
        mediator.getFacade().sendNotification(MsgAPI.ACTION_KEY_UP, keycode);

        switch (KeyBindingsLayout.mapAction(keycode)) {
            case KeyBindingsLayout.PAN_TOOL:
                // if pan mode is disabled set cursor back
                mediator.toolHotSwapBack();
                break;
        }

        if(mediator.currentSelectedTool != null) {
            mediator.currentSelectedTool.keyUp(entity, keycode);
        }

        return true;
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

        if (mediator.currentSelectedTool != null) {
            mediator.currentSelectedTool.stageMouseDown(x, y);
        }

        return true;
    }

    @Override
    public void touchUp(int entity, float x, float y, int pointer, int button) {
        super.touchUp(entity, x, y, pointer, button);

        if(mediator.currentSelectedTool != null) {
            mediator.currentSelectedTool.stageMouseUp(x, y);
        }

        Sandbox sandbox = mediator.getViewComponent();
        if (button == Input.Buttons.RIGHT) {
            // if clicked on empty space, selections need to be cleared
            sandbox.getSelector().clearSelections();

            // show default dropdown
            mediator.getFacade().sendNotification(MsgAPI.SCENE_RIGHT_CLICK, new Vector2(x, y));

            return;
        }

        if (button == Input.Buttons.MIDDLE) {
            mediator.toolHotSwapBack();
        }

        if (getTapCount() == 2 && button == Input.Buttons.LEFT) {
            doubleClick(entity, x, y);
        }

    }

    private void doubleClick(int entity, float x, float y) {
        if (mediator.currentSelectedTool != null) {
            Sandbox sandbox = mediator.getViewComponent();
            mediator.currentSelectedTool.stageMouseDoubleClick(x, y);
        }
    }

    @Override
    public void touchDragged(int entity, float x, float y, int pointer) {
        if (mediator.currentSelectedTool != null) {
            Sandbox sandbox = mediator.getViewComponent();
            mediator.currentSelectedTool.stageMouseDragged(x, y);
        }
    }


    @Override
    public boolean scrolled(int entity, float amountX, float amountY) {
        Sandbox sandbox = mediator.getViewComponent();
        // well, duh
        if (amountX == 0 && amountY == 0) return false;

        // Control pressed as well
        if (isControlPressed()) {
            float zoomPercent = sandbox.getZoomPercent();
            zoomPercent-= amountY * 4f;
            if(zoomPercent < 5 ) zoomPercent = 5;

            sandbox.setZoomPercent(zoomPercent, true);
        } else {
            if (mediator.currentSelectedTool != null
                    && !mediator.currentSelectedTool.stageMouseScrolled(amountX, amountY)) {

                float scale = mediator.settingsManager.editorConfigVO.scrollVelocity / sandbox.getPixelPerWU();
                mediator.getViewComponent().panSceneBy(amountX * scale, -amountY * scale);
            }
        }

        return false;
    }

    private boolean isControlPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.SYM)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
    }
}
