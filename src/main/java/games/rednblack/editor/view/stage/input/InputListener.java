package games.rednblack.editor.view.stage.input;

public interface InputListener {
	
	boolean keyDown(int entity, int keycode);
	
	boolean keyUp(int entity, int keycode);
	
	boolean keyTyped(int entity, char character);

	boolean touchDown(int entity, float screenX, float screenY, int pointer, int button);
	
	void touchUp(int entity, float screenX, float screenY, int pointer, int button);
	
	void touchDragged(int entity, float screenX, float screenY, int pointer);
	
	boolean mouseMoved(int entity, float screenX, float screenY);
	
	boolean scrolled(int entity, float amountX, float amountY);

}
