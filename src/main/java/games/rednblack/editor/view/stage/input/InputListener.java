package games.rednblack.editor.view.stage.input;

import com.badlogic.ashley.core.Entity;

public interface InputListener {
	
	boolean keyDown(Entity entity, int keycode);
	
	boolean keyUp(Entity entity, int keycode);
	
	boolean keyTyped(Entity entity, char character);

	boolean touchDown(Entity entity, float screenX, float screenY, int pointer, int button);
	
	void touchUp(Entity entity, float screenX, float screenY, int pointer, int button);
	
	void touchDragged(Entity entity, float screenX, float screenY, int pointer);
	
	boolean mouseMoved(Entity entity, float screenX, float screenY);
	
	boolean scrolled(Entity entity, float amountX, float amountY);

}
