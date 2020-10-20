package games.rednblack.editor.renderer.systems.render;

import box2dLight.RayHandler;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.commons.IExternalItemType;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.systems.render.logic.DrawableLogicMapper;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

import java.util.Map;

public class HyperLap2dRenderer extends IteratingSystem {
	private ComponentMapper<ViewPortComponent> viewPortMapper = ComponentMapper.getFor(ViewPortComponent.class);
	private ComponentMapper<CompositeTransformComponent> compositeTransformMapper = ComponentMapper.getFor(CompositeTransformComponent.class);
	private ComponentMapper<NodeComponent> nodeMapper = ComponentMapper.getFor(NodeComponent.class);
	private ComponentMapper<ParentNodeComponent> parentNodeMapper = ComponentMapper.getFor(ParentNodeComponent.class);
	private ComponentMapper<TransformComponent> transformMapper = ComponentMapper.getFor(TransformComponent.class);
	private ComponentMapper<MainItemComponent> mainItemComponentMapper = ComponentMapper.getFor(MainItemComponent.class);
	private ComponentMapper<ShaderComponent> shaderComponentMapper = ComponentMapper.getFor(ShaderComponent.class);
	private ComponentMapper<DimensionsComponent> dimensionsComponentComponentMapper = ComponentMapper.getFor(DimensionsComponent.class);
	
	private DrawableLogicMapper drawableLogicMapper;
	private RayHandler rayHandler;
	private Camera camera;

	public static float timeRunning = 0;
	
	public Batch batch;
	//ShaderManager shaderManager = new ShaderManager();

	public HyperLap2dRenderer(Batch batch) {
		super(Family.all(ViewPortComponent.class).get());
		this.batch = batch;
		drawableLogicMapper = new DrawableLogicMapper();
		//shaderManager.createFrameBuffer("main");
	}

	public void addDrawableType(IExternalItemType itemType) {
		drawableLogicMapper.addDrawableToMap(itemType.getTypeId(), itemType.getDrawable());
	}

	@Override
	public void processEntity(Entity entity, float deltaTime) {
		timeRunning+=deltaTime;

		ViewPortComponent ViewPortComponent = viewPortMapper.get(entity);
		Viewport viewport = ViewPortComponent.viewPort;
		camera = viewport.getCamera();

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		//shaderManager.beginFrameBuffer("main");
		batch.begin();
		drawRecursively(entity, 1f);
		batch.end();

		/*shaderManager.endFrameBuffer();
		shaderManager.begin(SceneLoader.createDefaultShader());
		shaderManager.renderFrameBuffer("main");
		shaderManager.end();*/

		if (rayHandler != null) {
			OrthographicCamera orthoCamera = (OrthographicCamera) camera;

			rayHandler.setCombinedMatrix(camera.combined,
					camera.position.x,
					camera.position.y,
					2 * camera.viewportWidth * orthoCamera.zoom,
					2 * camera.viewportHeight * orthoCamera.zoom);
			rayHandler.updateAndRender();
		}
		//debugRenderer.render(world, camera.combined);
	}

	private void drawRecursively(Entity rootEntity, float parentAlpha) {
		CompositeTransformComponent curCompositeTransformComponent = compositeTransformMapper.get(rootEntity);
		TransformComponent transform = transformMapper.get(rootEntity);

		boolean scissors = false;

		if (curCompositeTransformComponent.transform || transform.rotation != 0 || transform.scaleX !=1 || transform.scaleY !=1){
			computeTransform(rootEntity);
			applyTransform(rootEntity, batch);
		}

		if (curCompositeTransformComponent.scissorsEnabled) {
			batch.flush();
			//TODO Scissors rectangle does not rotate.. why? Uhm
			ScissorStack.calculateScissors(camera, curCompositeTransformComponent.oldTransform, curCompositeTransformComponent.clipBounds, curCompositeTransformComponent.scissors);
			if (ScissorStack.pushScissors(curCompositeTransformComponent.scissors)) {
				scissors = true;
			}
		}

		applyShader(rootEntity, batch);

        TintComponent tintComponent = ComponentRetriever.get(rootEntity, TintComponent.class);
        parentAlpha *= tintComponent.color.a;

		drawChildren(rootEntity, batch, curCompositeTransformComponent, parentAlpha);

		if (curCompositeTransformComponent.transform || transform.rotation != 0 || transform.scaleX !=1 || transform.scaleY !=1)
			resetTransform(rootEntity, batch);

		resetShader(rootEntity, batch);

		if (scissors) {
			batch.flush();
			ScissorStack.popScissors();
		}
	}

	private void drawChildren(Entity rootEntity, Batch batch, CompositeTransformComponent curCompositeTransformComponent, float parentAlpha) {
		NodeComponent nodeComponent = nodeMapper.get(rootEntity);
		Entity[] children = nodeComponent.children.begin();
		TransformComponent transform = transformMapper.get(rootEntity);
		if (curCompositeTransformComponent.transform || transform.rotation != 0 || transform.scaleX !=1 || transform.scaleY !=1) {
			for (int i = 0, n = nodeComponent.children.size; i < n; i++) {
				Entity child = children[i];

				LayerMapComponent rootLayers = ComponentRetriever.get(rootEntity, LayerMapComponent.class);
				ZIndexComponent childZIndexComponent = ComponentRetriever.get(child, ZIndexComponent.class);

				if(!rootLayers.isVisible(childZIndexComponent.layerName)) {
					continue;
				}

				MainItemComponent childMainItemComponent = mainItemComponentMapper.get(child);
				if(!childMainItemComponent.visible){
					continue;
				}
				
				int entityType = childMainItemComponent.entityType;

				NodeComponent childNodeComponent = nodeMapper.get(child);
				
				if(childNodeComponent ==null){
					drawEntity(entityType, batch, child, parentAlpha);
				}else{
					//Step into Composite
					drawRecursively(child, parentAlpha);
				}
			}
		} else {
			// No transform for this group, offset each child.
			TransformComponent compositeTransform = transformMapper.get(rootEntity);
			
			float offsetX = compositeTransform.x, offsetY = compositeTransform.y;
			
			if(viewPortMapper.has(rootEntity)){
				offsetX = 0;
				offsetY = 0;
			}
			
			for (int i = 0, n = nodeComponent.children.size; i < n; i++) {
				Entity child = children[i];

				LayerMapComponent rootLayers = ComponentRetriever.get(rootEntity, LayerMapComponent.class);
				ZIndexComponent childZIndexComponent = ComponentRetriever.get(child, ZIndexComponent.class);

				if(!rootLayers.isVisible(childZIndexComponent.layerName)) {
					continue;
				}

				MainItemComponent childMainItemComponent = mainItemComponentMapper.get(child);
				if(!childMainItemComponent.visible){
					continue;
				}

				TransformComponent childTransformComponent = transformMapper.get(child);
				float cx = childTransformComponent.x, cy = childTransformComponent.y;
				childTransformComponent.x = cx + offsetX;
				childTransformComponent.y = cy + offsetY;
				
				NodeComponent childNodeComponent = nodeMapper.get(child);
				int entityType = mainItemComponentMapper.get(child).entityType;
				
				if(childNodeComponent ==null){
					drawEntity(entityType, batch, child, parentAlpha);
				}else{
					//Step into Composite
					drawRecursively(child, parentAlpha);
				}
				childTransformComponent.x = cx;
				childTransformComponent.y = cy;
			}
		}
		nodeComponent.children.end();
	}

	private void drawEntity(int entityType, Batch batch, Entity child, float parentAlpha) {
		applyShader(child, batch);
		//Find the logic from mapper and draw it
		drawableLogicMapper.getDrawable(entityType).draw(batch, child, parentAlpha);

		resetShader(child, batch);
	}

	/** Returns the transform for this group's coordinate system. 
	 * @param rootEntity */
	protected Matrix4 computeTransform(Entity rootEntity) {
		CompositeTransformComponent curCompositeTransformComponent = compositeTransformMapper.get(rootEntity);
		ParentNodeComponent parentNodeComponent = parentNodeMapper.get(rootEntity);
		TransformComponent curTransform = transformMapper.get(rootEntity);
		Affine2 worldTransform = curCompositeTransformComponent.worldTransform;

		float originX = curTransform.originX;
		float originY = curTransform.originY;
		float x = curTransform.x;
		float y = curTransform.y;
		float rotation = curTransform.rotation;
		float scaleX = curTransform.scaleX;
		float scaleY = curTransform.scaleY;

		worldTransform.setToTrnRotScl(x + originX, y + originY, rotation, scaleX, scaleY);
		if (originX != 0 || originY != 0) worldTransform.translate(-originX, -originY);

		// Find the parent that transforms.
		
		CompositeTransformComponent parentTransformComponent = null;
		
		Entity parentEntity = null;
		if(parentNodeComponent != null){
			parentEntity = parentNodeComponent.parentEntity;
		}
		
		if (parentEntity != null){
			parentTransformComponent = compositeTransformMapper.get(parentEntity);
			TransformComponent transform = transformMapper.get(parentEntity);
			if(curCompositeTransformComponent.transform || transform.rotation != 0 || transform.scaleX !=1 || transform.scaleY !=1)
				worldTransform.preMul(parentTransformComponent.worldTransform);
		}

		curCompositeTransformComponent.computedTransform.set(worldTransform);
		return curCompositeTransformComponent.computedTransform;
	}

	protected void applyTransform (Entity rootEntity, Batch batch) {
		CompositeTransformComponent curCompositeTransformComponent = compositeTransformMapper.get(rootEntity);
		curCompositeTransformComponent.oldTransform.set(batch.getTransformMatrix());
		batch.setTransformMatrix(curCompositeTransformComponent.computedTransform);
	}

	protected void resetTransform (Entity rootEntity, Batch batch) {
		CompositeTransformComponent curCompositeTransformComponent = compositeTransformMapper.get(rootEntity);
		batch.setTransformMatrix(curCompositeTransformComponent.oldTransform);
	}

	protected void applyShader(Entity entity, Batch batch) {
		if(shaderComponentMapper.has(entity)){
			ShaderComponent shaderComponent = shaderComponentMapper.get(entity);
			if(shaderComponent.getShader() != null) {
				batch.setShader(shaderComponent.getShader());

				batch.getShader().setUniformf("deltaTime", Gdx.graphics.getDeltaTime());
				batch.getShader().setUniformf("time", HyperLap2dRenderer.timeRunning);
				batch.getShader().setUniformf("screen_size", dimensionsComponentComponentMapper.get(entity).width,
						dimensionsComponentComponentMapper.get(entity).height);

				for (Map.Entry<String, String> entry : mainItemComponentMapper.get(entity).customVariables.getHashMap().entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();
					if (key.startsWith("_shader_")) {
						batch.getShader().setUniformf(key.replace("_shader_", ""), Float.parseFloat(value));
					}
				}

				GL20 gl = Gdx.gl20;
				int error;
				if ((error = gl.glGetError()) != GL20.GL_NO_ERROR) {
					Gdx.app.log("opengl", "Error: " + error);
					Gdx.app.log("opengl", shaderComponent.getShader().getLog());
					//throw new RuntimeException( ": glError " + error);
				}
			}
		}
	}

	protected void resetShader(Entity entity, Batch batch) {
		if(shaderComponentMapper.has(entity)){
			batch.setShader(null);
		}
	}
	
	public void setRayHandler(RayHandler rayHandler){
		this.rayHandler = rayHandler;
	}

	public Batch getBatch() {
        return batch;
    }
}

