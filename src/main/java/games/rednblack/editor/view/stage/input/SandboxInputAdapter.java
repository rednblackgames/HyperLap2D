package games.rednblack.editor.view.stage.input;

import com.artemis.BaseComponentMapper;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.renderer.components.NodeComponent;
import games.rednblack.editor.renderer.components.ParentNodeComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.renderer.data.LayerItemVO;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.TransformMathUtils;
import games.rednblack.editor.utils.EntityBounds;
import games.rednblack.editor.utils.runtime.EntityUtils;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.SandboxMediator;

public class SandboxInputAdapter implements InputProcessor {

	private HyperLap2DFacade facade;
	private int rootEntity;
	private InputListenerComponent inpputListenerComponent;
	private int target;
	private Vector2 hitTargetLocalCoordinates = new Vector2();
	private Sandbox sandbox;
	private final EntityBounds tempEntityBounds = new EntityBounds();

	public SandboxInputAdapter() {
		facade = HyperLap2DFacade.getInstance();
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

		Viewport viewPort = SandboxComponentRetriever.get(rootEntity, ViewPortComponent.class).viewPort;
		if (screenX < viewPort.getScreenX() || screenX >= viewPort.getScreenX() + viewPort.getScreenWidth()) return false;
		if (Gdx.graphics.getHeight() - screenY < viewPort.getScreenY()
			|| Gdx.graphics.getHeight() - screenY >= viewPort.getScreenY() + viewPort.getScreenHeight()) return false;

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
		inpputListenerComponent = mapper.get(target);
		if(inpputListenerComponent == null) return false;
		Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
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
		inpputListenerComponent = mapper.get(target);
		if(inpputListenerComponent == null) return false;
		Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
		for (int j = 0, s = listeners.size; j < s; j++){
			listeners.get(j).touchUp(target, screenX, screenY, pointer, button);
		}
		target = -1;
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		rootEntity = sandbox.getCurrentViewingEntity();

		if(rootEntity == -1){
			return false;
		}

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
		inpputListenerComponent = mapper.get(target);
		if(inpputListenerComponent == null) return false;
		Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
		for (int j = 0, s = listeners.size; j < s; j++){
			listeners.get(j).touchDragged(target, screenX, screenY, pointer);
		}
		return true;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
//		entities = engine.getEntitiesFor(root);
//		for (int i = 0, n = entities.size(); i < n; i++){
//			Entity entity = entities.get(i);
//			inpputListenerComponent = ComponentRetriever.get(target, InputListenerComponent.class);
//			Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
//			for (int j = 0, s = listeners.size; j < s; j++){
//				if (listeners.get(j).mouseMoved(entity, screenX, screenY)){
//					return true;
//				}
//			}
//			
//		}
		return false;
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
//		inpputListenerComponent = ComponentRetriever.get(entity, InputListenerComponent.class);
//		if(inpputListenerComponent == null) continue;
//		Array<InputListener> listeners = inpputListenerComponent.getAllListeners();
//		for (int j = 0, s = listeners.size; j < s; j++){				
//			if (listeners.get(j).scrolled(entity,amount)){
//				return true;
//			}
//		}


		return false;
	}

	Vector2 tmpVector2 = new Vector2();
	
	public int hit(int root, float x, float y){
		Vector2 localCoordinates  = tmpVector2.set(x, y);

		ComponentMapper<TransformComponent> transformMapper = (ComponentMapper<TransformComponent>) ComponentRetriever.getMapper(TransformComponent.class, sandbox.getEngine());
		TransformMathUtils.parentToLocalCoordinates(root, localCoordinates, transformMapper);

		NodeComponent nodeComponent = SandboxComponentRetriever.get(root, NodeComponent.class);
		SnapshotArray<Integer> childrenEntities = nodeComponent.children;
		int n = childrenEntities.size-1;
		for (int i = n; i >= 0; i--){
			int childEntity = childrenEntities.get(i);

			// get layer locked or not
			LayerItemVO layerItemVO = EntityUtils.getEntityLayer(childEntity);
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
		ViewPortComponent viewPortComponent = SandboxComponentRetriever.get(root, ViewPortComponent.class);
		viewPortComponent.viewPort.unproject(screenCoords);
		return screenCoords;
	}
}
