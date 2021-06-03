package games.rednblack.editor.view.stage.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;

/**
 * This processor serves other {@link InputListener} as a helper to know if the shift/alt/ctrl keys are pressed.
 * 
 * This class is implemented as a singleton.
 * 
 * @author Jan-Thierry Wegener
 *
 */
public final class MetaKeyInputProcessor implements InputProcessor {

	private static MetaKeyInputProcessor INSTANCE;

	private boolean isShiftDown = false;
	private boolean isCtrlDown = false;
	private boolean isAltDown = false;

	private MetaKeyInputProcessor() {
	}

    /**
     * Facade Singleton Factory method
     *
     * @return The Singleton instance of the processor.
     */
    public synchronized static MetaKeyInputProcessor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MetaKeyInputProcessor();
        }
        return INSTANCE;
    }

    /**
     * Returns whether a shift key is currently pressed or not.
     * 
     * @return <code>true</code> if one of the shift keys is pressed, <code>false</code> otherwise.
     */
	public boolean isShiftDown() {
		return isShiftDown;
	}

    /**
     * Returns whether a control key is currently pressed or not.
     * 
     * @return <code>true</code> if one of the ctrl keys is pressed, <code>false</code> otherwise.
     */
	public boolean isCtrlDown() {
		return isCtrlDown;
	}

    /**
     * Returns whether a alt key is currently pressed or not.
     * 
     * @return <code>true</code> if one of the alt keys is pressed, <code>false</code> otherwise.
     */
	public boolean isAltDown() {
		return isAltDown;
	}

	@Override
	public boolean keyDown(int keycode) {
		switch (keycode) {
			case Input.Keys.SHIFT_LEFT:
			case Input.Keys.SHIFT_RIGHT:
				isShiftDown = true;
				break;
			case Input.Keys.ALT_LEFT:
			case Input.Keys.ALT_RIGHT:
				isAltDown = true;
				break;
			case Input.Keys.CONTROL_LEFT:
			case Input.Keys.CONTROL_RIGHT:
				isCtrlDown = true;
				break;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		switch (keycode) {
		case Input.Keys.SHIFT_LEFT:
		case Input.Keys.SHIFT_RIGHT:
			isShiftDown = false;
			break;
		case Input.Keys.ALT_LEFT:
		case Input.Keys.ALT_RIGHT:
			isAltDown = false;
			break;
		case Input.Keys.CONTROL_LEFT:
		case Input.Keys.CONTROL_RIGHT:
			isCtrlDown = false;
			break;
	}
	return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		return false;
	}
	
}
