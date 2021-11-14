package games.rednblack.editor.view.stage.input;

import com.artemis.PooledComponent;
import com.artemis.annotations.Transient;
import com.badlogic.gdx.utils.Array;

@Transient
public class InputListenerComponent extends PooledComponent {
	private Array<InputListener> listeners = new Array<>(1);
	
	public void addListener(InputListener listener){
		if (!listeners.contains(listener, true)) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(InputListener listener){
		listeners.removeValue(listener, true);
	}
	
	public void removeAllListener(){
		listeners.clear();
	}
	
	public Array<InputListener> getAllListeners(){
		listeners.shrink();
		return listeners;
	}

	@Override
	protected void reset() {
		listeners.clear();
	}
}
