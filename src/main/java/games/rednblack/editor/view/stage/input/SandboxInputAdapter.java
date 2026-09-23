package games.rednblack.editor.view.stage.input;
import games.rednblack.editor.proxy.EntityDataProxy;

import games.rednblack.editor.renderer.ecs.BaseComponentMapper;
import games.rednblack.editor.renderer.ecs.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.renderer.components.additional.InputTargetComponent;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.renderer.systems.UIInputSystem;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.TransformMathUtils;
import games.rednblack.editor.utils.EntityBounds;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.SandboxMediator;
import games.rednblack.puremvc.Facade;

public class SandboxInputAdapter implements InputProcessor {

	private Facade facade;
	private int rootEntity;
	private InputListenerComponent inputListenerComponent;
	private int target;
	private Vector2 hitTargetLocalCoordinates = new Vector2();
	private Sandbox sandbox;
	private final EntityBounds tempEntityBounds = new EntityBounds();
	/** The scene took this press on a handle of its own, such as a slider knob: the item stays put. */
	private boolean draggingHandle = false;

	public SandboxInputAdapter() {
		facade = Facade.getInstance();
		SandboxMediator sandboxMediator = facade.retrieveMediator(SandboxMediator.NAME);
		sandbox = sandboxMediator.getViewComponent();
	}

	@Override
	public boolean keyDown(int keycode) {
		Array<InputListener> sandboxListeners = sandbox.getAllListeners();
		for (int i = 0, s = sandboxListeners.size; i < s; i++) {
			sandboxListeners.get(i).keyDown(-1, keycode);
		}

		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		Array<InputListener> sandboxListeners = sandbox.getAllListeners();
		for (int i = 0, s = sandboxListeners.size; i < s; i++) {
			sandboxListeners.get(i).keyUp(-1, keycode);
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		Array<InputListener> sandboxListeners = sandbox.getAllListeners();
		for (int i = 0, s = sandboxListeners.size; i < s; i++) {
			sandboxListeners.get(i).keyTyped(-1, character);
		}

		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {

		//Handle Global Listeners than others
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == -1){
			return false;
		}

		if (!insideSandbox(screenX, screenY)) return false;

		UIInputSystem uiInput = uiInput();
		if (uiInput != null) {
			//a press on a handle drags the widget's own value, so the item must not follow the pointer
			draggingHandle = isDragHandle(uiInput.hit(screenX, screenY));
			uiInput.touchDown(screenX, screenY, pointer, button);
		}

		hitTargetLocalCoordinates.set(screenX, screenY);
		screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

		target = hit(rootEntity, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y);
		if(target == -1){
			hitTargetLocalCoordinates.set(screenX, screenY);
			screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

			Array<InputListener> sandboxListeners = sandbox.getAllListeners();
			for (int i = 0, s = sandboxListeners.size; i < s; i++) {
				sandboxListeners.get(i).touchDown(-1, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y, pointer, button);
			}

			return false;
		}

		hitTargetLocalCoordinates.set(screenX, screenY);
		screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

		BaseComponentMapper<InputListenerComponent> mapper = ComponentMapper.getFor(InputListenerComponent.class, sandbox.getEngine());
		inputListenerComponent = mapper.get(target);
		if(inputListenerComponent == null) return false;
		Array<InputListener> listeners = inputListenerComponent.getAllListeners();
		ComponentMapper<TransformComponent> transformMapper = (ComponentMapper<TransformComponent>) ComponentRetriever.getMapper(TransformComponent.class, sandbox.getEngine());
		ComponentMapper<ParentNodeComponent> parentMapper = (ComponentMapper<ParentNodeComponent>) ComponentRetriever.getMapper(ParentNodeComponent.class, sandbox.getEngine());
		TransformMathUtils.sceneToLocalCoordinates(target, hitTargetLocalCoordinates, transformMapper, parentMapper);
		for (int j = 0, s = listeners.size; j < s; j++) {
			if (listeners.get(j).touchDown(target, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y, pointer, button)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == -1){
			return false;
		}

		UIInputSystem uiInput = uiInput();
		if (uiInput != null) uiInput.touchUp(screenX, screenY, pointer, button);
		draggingHandle = false;

		if(target == -1){
			hitTargetLocalCoordinates.set(screenX, screenY);
			screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

			Array<InputListener> sandboxListeners = sandbox.getAllListeners();
			for (int i = 0, s = sandboxListeners.size; i < s; i++) {
				sandboxListeners.get(i).touchUp(-1, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y, pointer, button);
			}

			return false;
		}
		BaseComponentMapper<InputListenerComponent> mapper = ComponentMapper.getFor(InputListenerComponent.class, sandbox.getEngine());
		inputListenerComponent = mapper.get(target);
		if(inputListenerComponent == null) return false;
		Array<InputListener> listeners = inputListenerComponent.getAllListeners();
		for (int j = 0, s = listeners.size; j < s; j++){
			listeners.get(j).touchUp(target, screenX, screenY, pointer, button);
		}
		target = -1;
		return true;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == -1){
			return false;
		}

		UIInputSystem uiInput = uiInput();
		if (uiInput != null) uiInput.touchDragged(screenX, screenY, pointer);

		//the widget is being dragged, not the item: it was selected on the press and stays where it is
		if (draggingHandle) return true;

		if(target == -1){
			hitTargetLocalCoordinates.set(screenX, screenY);
			screenToSceneCoordinates(rootEntity, hitTargetLocalCoordinates);

			Array<InputListener> sandboxListeners = sandbox.getAllListeners();
			for (int i = 0, s = sandboxListeners.size; i < s; i++) {
				sandboxListeners.get(i).touchDragged(-1, hitTargetLocalCoordinates.x, hitTargetLocalCoordinates.y, pointer);
			}
			return false;
		}

		BaseComponentMapper<InputListenerComponent> mapper = ComponentMapper.getFor(InputListenerComponent.class, sandbox.getEngine());
		inputListenerComponent = mapper.get(target);
		if(inputListenerComponent == null) return false;
		Array<InputListener> listeners = inputListenerComponent.getAllListeners();
		for (int j = 0, s = listeners.size; j < s; j++){
			listeners.get(j).touchDragged(target, screenX, screenY, pointer);
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		UIInputSystem uiInput = uiInput();
		if (uiInput == null) return false;

		//outside the sandbox the pointer belongs to the editor, nothing in the scene is hovered
		if (insideSandbox(screenX, screenY)) uiInput.mouseMoved(screenX, screenY);
		else uiInput.clearHover();

		return false;
	}

	/**
	 * The input of the scene being edited, so widgets can be tried out on the spot: a button lights
	 * up and clicks, a slider follows the pointer. What it does with an event is never taken into
	 * account, so selecting, dragging and entering composites keep working over a live widget.
	 */
	private UIInputSystem uiInput() {
		return sandbox.getEngine() == null ? null : sandbox.getEngine().getSystem(UIInputSystem.class);
	}

	/** Whether the entity is a handle the scene drags by itself, such as the knob of a slider. */
	private boolean isDragHandle(int entity) {
		if (entity == -1) return false;

		InputTargetComponent inputTarget = EntityDataProxy.get().get(entity, InputTargetComponent.class);
		return inputTarget != null && inputTarget.dragHandle;
	}

	private boolean insideSandbox(int screenX, int screenY) {
		rootEntity = sandbox.getCurrentViewingEntity();
		if (rootEntity == -1) return false;

		ViewPortComponent viewPortComponent = EntityDataProxy.get().get(rootEntity, ViewPortComponent.class);
		if (viewPortComponent == null || viewPortComponent.viewPort == null) return false;

		Viewport viewPort = viewPortComponent.viewPort;
		if (screenX < viewPort.getScreenX() || screenX >= viewPort.getScreenX() + viewPort.getScreenWidth()) return false;
		return Gdx.graphics.getHeight() - screenY >= viewPort.getScreenY()
				&& Gdx.graphics.getHeight() - screenY < viewPort.getScreenY() + viewPort.getScreenHeight();
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == -1){
			return false;
		}

		Array<InputListener> sandboxListeners = sandbox.getAllListeners();
		for (int i = 0, s = sandboxListeners.size; i < s; i++) {
			sandboxListeners.get(i).scrolled(-1, amountX, amountY);
		}

		//TODO scroll for other Entities don't know how deep tis should go all entities or only hit tested
		return false;
	}

	Vector2 tmpVector2 = new Vector2();
	
	public int hit(int root, float x, float y){
		Vector2 localCoordinates  = tmpVector2.set(x, y);

		ComponentMapper<TransformComponent> transformMapper = (ComponentMapper<TransformComponent>) ComponentRetriever.getMapper(TransformComponent.class, sandbox.getEngine());
		TransformMathUtils.parentToLocalCoordinates(root, localCoordinates, transformMapper);

		NodeComponent nodeComponent = EntityDataProxy.get().get(root, NodeComponent.class);
		SnapshotArray<Integer> childrenEntities = nodeComponent.children;
		int n = childrenEntities.size-1;
		for (int i = n; i >= 0; i--){
			int childEntity = childrenEntities.get(i);

			// get layer locked or not
			LayerItemVO layerItemVO = EntityDataProxy.get().metadata().getLayer(childEntity);
			if(layerItemVO != null && (layerItemVO.isLocked || !layerItemVO.isVisible)) {
				continue;
			}

			if (Intersector.isPointInPolygon(tempEntityBounds.getBoundPointsList(childEntity), localCoordinates)) {
				return childEntity;
			}
		}
		return -1;
	}
	
	public Vector2 screenToSceneCoordinates (int root, Vector2 screenCoords) {
		ViewPortComponent viewPortComponent = EntityDataProxy.get().get(root, ViewPortComponent.class);
		viewPortComponent.viewPort.unproject(screenCoords);
		return screenCoords;
	}
}
