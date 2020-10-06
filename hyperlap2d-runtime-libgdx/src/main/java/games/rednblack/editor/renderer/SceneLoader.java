package games.rednblack.editor.renderer;

import box2dLight.DirectionalLight;
import box2dLight.RayHandler;

import com.badlogic.ashley.core.*;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.commons.IExternalItemType;
import games.rednblack.editor.renderer.components.*;
import games.rednblack.editor.renderer.components.light.LightBodyComponent;
import games.rednblack.editor.renderer.components.light.LightObjectComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.factory.ActionFactory;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.resources.ResourceManager;
import games.rednblack.editor.renderer.scripts.IScript;
import games.rednblack.editor.renderer.systems.*;
import games.rednblack.editor.renderer.systems.action.ActionSystem;
import games.rednblack.editor.renderer.systems.action.data.ActionData;
import games.rednblack.editor.renderer.systems.render.HyperLap2dRenderer;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

/**
 * SceneLoader is important part of runtime that utilizes provided
 * IResourceRetriever (or creates default one shipped with runtime) in order to
 * load entire scene data into viewable actors provides the functionality to get
 * root actor of scene and load scenes.
 */
public class SceneLoader {

	private String curResolution = "orig";
	private SceneVO sceneVO;
	private IResourceRetriever rm = null;

    private PooledEngine engine = null;
	private RayHandler rayHandler;
	private World world;
	private Entity rootEntity;
	private DirectionalLight sceneDirectionalLight;

	private EntityFactory entityFactory;
	private ActionFactory actionFactory;

	private float pixelsPerWU = 1;

	private HyperLap2dRenderer renderer;

    public SceneLoader(World world, RayHandler rayHandler, boolean cullingEnabled, int entityPoolInitialSize, int entityPoolMaxSize, int componentPoolInitialSize, int componentPoolMaxSize) {
        this.world = world;
        this.rayHandler = rayHandler;

		ResourceManager rm = new ResourceManager();
        rm.initAllResources();
		this.rm = rm;

		initSceneLoader(cullingEnabled, entityPoolInitialSize, entityPoolMaxSize, componentPoolInitialSize, componentPoolMaxSize);
    }

    public SceneLoader(IResourceRetriever rm, World world, RayHandler rayHandler, boolean cullingEnabled, int entityPoolInitialSize, int entityPoolMaxSize, int componentPoolInitialSize, int componentPoolMaxSize) {
		this.world = world;
        this.rayHandler = rayHandler;
		this.rm = rm;

		initSceneLoader(cullingEnabled, entityPoolInitialSize, entityPoolMaxSize, componentPoolInitialSize, componentPoolMaxSize);
    }

	public SceneLoader() {
		this(null, null, true, 10, 100, 10, 100);
	}

	public SceneLoader(IResourceRetriever rm) {
		this(rm, null, null, true, 10, 100, 10, 100);
	}

	/**
	 * this method is called when rm has loaded all data
	 */
    private void initSceneLoader(boolean cullingEnabled, int entityPoolInitialSize, int entityPoolMaxSize, int componentPoolInitialSize, int componentPoolMaxSize) {
		this.engine = new PooledEngine(entityPoolInitialSize, entityPoolMaxSize, componentPoolInitialSize, componentPoolMaxSize);

        if (world == null) {
            world = new World(new Vector2(0,-10), true);
        }

        if (rayHandler == null) {
            RayHandler.setGammaCorrection(true);
            RayHandler.useDiffuseLight(true);

            rayHandler = new RayHandler(world);
            rayHandler.setAmbientLight(1f, 1f, 1f, 1f);
            rayHandler.setCulling(true);
            rayHandler.setBlur(true);
            rayHandler.setBlurNum(3);
            rayHandler.setShadows(true);
        }
        
        addSystems(cullingEnabled);
        entityFactory = new EntityFactory(engine, rayHandler, world, rm);
    }

	public void setResolution(String resolutionName) {
		ResolutionEntryVO resolution = getRm().getProjectVO().getResolution(resolutionName);
		if(resolution != null) {
			curResolution = resolutionName;
		}
	}

	public void injectExternalItemType(IExternalItemType itemType) {
		itemType.injectDependencies(engine, rayHandler, world, rm);
		itemType.injectMappers();
		entityFactory.addExternalFactory(itemType);
		engine.addSystem(itemType.getSystem());
		renderer.addDrawableType(itemType);
	}

	private void addSystems(boolean cullingEnabled) {
		ParticleSystem particleSystem = new ParticleSystem();
		LightSystem lightSystem = new LightSystem();
		lightSystem.setRayHandler(rayHandler);
		SpriteAnimationSystem animationSystem = new SpriteAnimationSystem();
		LayerSystem layerSystem = new LayerSystem();
		PhysicsSystem physicsSystem = new PhysicsSystem(world);
		CompositeSystem compositeSystem = new CompositeSystem();
		LabelSystem labelSystem = new LabelSystem();
		TypingLabelSystem typingLabelSystem = new TypingLabelSystem();
        ScriptSystem scriptSystem = new ScriptSystem();
        ActionSystem actionSystem = new ActionSystem();
        BoundingBoxSystem boundingBoxSystem = new BoundingBoxSystem();
        CullingSystem cullingSystem = new CullingSystem();
		renderer = new HyperLap2dRenderer(new PolygonSpriteBatch(2000, createDefaultShader()));
		renderer.setRayHandler(rayHandler);
		//renderer.setBox2dWorld(world);
		
		engine.addSystem(animationSystem);
		engine.addSystem(particleSystem);
		engine.addSystem(layerSystem);
		engine.addSystem(physicsSystem);
		engine.addSystem(lightSystem);
		engine.addSystem(typingLabelSystem);
		engine.addSystem(compositeSystem);
		engine.addSystem(labelSystem);
        engine.addSystem(scriptSystem);
        engine.addSystem(actionSystem);

        if (cullingEnabled) {
			engine.addSystem(boundingBoxSystem);
			engine.addSystem(cullingSystem);
		}

        engine.addSystem(renderer);

        // additional
        engine.addSystem(new ButtonSystem());

		addEntityRemoveListener();
	}

	private void addEntityRemoveListener() {
		engine.addEntityListener(new EntityListener() {
			@Override
			public void entityAdded(Entity entity) {
				// TODO: Gev knows what to do. (do this for all entities)

				// mae sure we assign correct z-index here
				/*
				ZindexComponent zindexComponent = ComponentRetriever.get(entity, ZindexComponent.class);
				ParentNodeComponent parentNodeComponent = ComponentRetriever.get(entity, ParentNodeComponent.class);
				if (parentNodeComponent != null) {
					NodeComponent nodeComponent = parentNodeComponent.parentEntity.getComponent(NodeComponent.class);
					zindexComponent.setZIndex(nodeComponent.children.size);
					zindexComponent.needReOrder = false;
				}*/

				// call init for a system
				ScriptComponent scriptComponent = entity.getComponent(ScriptComponent.class);
				if (scriptComponent != null) {
					for (IScript script : scriptComponent.scripts) {
						script.init(entity);
					}
				}
			}

			@Override
			public void entityRemoved(Entity entity) {
				ParentNodeComponent parentComponent = ComponentRetriever.get(entity, ParentNodeComponent.class);

				if (parentComponent == null) {
					return;
				}

				Entity parentEntity = parentComponent.parentEntity;
				NodeComponent parentNodeComponent = ComponentRetriever.get(parentEntity, NodeComponent.class);
				if (parentNodeComponent != null)
					parentNodeComponent.removeChild(entity);

				// check if composite and remove all children
				NodeComponent nodeComponent = ComponentRetriever.get(entity, NodeComponent.class);
				if (nodeComponent != null) {
					// it is composite
					for (Entity node : nodeComponent.children) {
						engine.removeEntity(node);
					}
				}

				//check for physics
				PhysicsBodyComponent physicsBodyComponent = ComponentRetriever.get(entity, PhysicsBodyComponent.class);
				if (physicsBodyComponent != null && physicsBodyComponent.body != null) {
					world.destroyBody(physicsBodyComponent.body);
				}

                // check if it is light
                LightObjectComponent lightObjectComponent = ComponentRetriever.get(entity, LightObjectComponent.class);
                if(lightObjectComponent != null) {
                    lightObjectComponent.lightObject.remove(true);
                }

				LightBodyComponent lightBodyComponent = ComponentRetriever.get(entity, LightBodyComponent.class);
				if(lightBodyComponent != null && lightBodyComponent.lightObject != null) {
					lightBodyComponent.lightObject.remove(true);
				}
			}
		});
	}

	public SceneVO loadScene(String sceneName, Viewport viewport) {
		return loadScene(sceneName, viewport, false);
	}

	public SceneVO loadScene(String sceneName) {
		return loadScene(sceneName, false);
	}

	public SceneVO loadScene(String sceneName, boolean customLight) {
		ProjectInfoVO projectVO = rm.getProjectVO();
		Viewport viewport = new ScalingViewport(Scaling.stretch, (float)projectVO.originalResolution.width/ pixelsPerWU, (float)projectVO.originalResolution.height/ pixelsPerWU, new OrthographicCamera());
		return loadScene(sceneName, viewport, customLight);
	}

	public SceneVO loadScene(String sceneName, Viewport viewport, boolean customLight) {
		// this has to be done differently.
		engine.removeAllEntities();
		entityFactory.clean();
		engine.clearPools();

		pixelsPerWU = rm.getProjectVO().pixelToWorld;

		sceneVO = rm.getSceneVO(sceneName);
		world.setGravity(new Vector2(sceneVO.physicsPropertiesVO.gravityX, sceneVO.physicsPropertiesVO.gravityY));
		PhysicsSystem physicsSystem = engine.getSystem(PhysicsSystem.class);
		if (physicsSystem != null)
			physicsSystem.setPhysicsOn(sceneVO.physicsPropertiesVO.enabled);

		if(sceneVO.composite == null) {
			sceneVO.composite = new CompositeVO();
		}
		rootEntity = entityFactory.createRootEntity(sceneVO.composite, viewport);
		engine.addEntity(rootEntity);

		if(sceneVO.composite != null) {
			entityFactory.initAllChildren(engine, rootEntity, sceneVO.composite);
		}
		if (!customLight) {
			setAmbientInfo(sceneVO);
		}

		actionFactory = new ActionFactory(rm.getProjectVO().libraryActions);

		return sceneVO;
	}

	public SceneVO getSceneVO() {
		return sceneVO;
	}

	public Entity loadFromLibrary(String libraryName) {
		ProjectInfoVO projectInfoVO = getRm().getProjectVO();
		CompositeItemVO compositeItemVO = projectInfoVO.libraryItems.get(libraryName);

		if(compositeItemVO != null) {
			return entityFactory.createEntity(null, compositeItemVO);
		}

		return null;
	}

    public CompositeItemVO loadVoFromLibrary(String libraryName) {
        ProjectInfoVO projectInfoVO = getRm().getProjectVO();
        CompositeItemVO compositeItemVO = projectInfoVO.libraryItems.get(libraryName);

       return compositeItemVO;
    }

    public ActionData loadActionFromLibrary(String actionName) {
		return actionFactory.loadFromLibrary(actionName);
	}

    public void addComponentsByTagName(String tagName, Class componentClass) {
        ImmutableArray<Entity> entities = engine.getEntities();
        for(Entity entity: entities) {
            MainItemComponent mainItemComponent = ComponentRetriever.get(entity, MainItemComponent.class);
            if(mainItemComponent.tags.contains(tagName)) {
                try {
                    entity.add(ClassReflection.<Component>newInstance(componentClass));
                } catch (ReflectionException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	/**
	 * Sets ambient light to the one specified in scene from editor
	 *
	 * @param vo
	 *            - Scene data file to invalidate
	 */

	public void setAmbientInfo(SceneVO vo) {
		setAmbientInfo(vo, false);
	}

	public void setAmbientInfo(SceneVO vo, boolean override) {
		if (sceneDirectionalLight != null) {
			sceneDirectionalLight.remove();
			sceneDirectionalLight = null;
		}
        if(override || !vo.lightsPropertiesVO.enabled) {
			RayHandler.useDiffuseLight(true);
            rayHandler.setAmbientLight(1f, 1f, 1f, 1f);
            return;
        }
        RayHandler.useDiffuseLight(!vo.lightsPropertiesVO.lightType.equals("BRIGHT"));

        if (vo.lightsPropertiesVO.ambientColor != null) {
			Color clr = new Color(vo.lightsPropertiesVO.ambientColor[0], vo.lightsPropertiesVO.ambientColor[1],
					vo.lightsPropertiesVO.ambientColor[2], vo.lightsPropertiesVO.ambientColor[3]);

			if (vo.lightsPropertiesVO.lightType.equals("DIRECTIONAL"))  {
				Color lightColor = new Color(vo.lightsPropertiesVO.directionalColor[0], vo.lightsPropertiesVO.directionalColor[1],
						vo.lightsPropertiesVO.directionalColor[2], vo.lightsPropertiesVO.directionalColor[3]);
				sceneDirectionalLight = new DirectionalLight(rayHandler, vo.lightsPropertiesVO.directionalRays,
						lightColor, vo.lightsPropertiesVO.directionalDegree);
			}
			rayHandler.setAmbientLight(clr);
			rayHandler.setBlurNum(vo.lightsPropertiesVO.blurNum);
		}
	}


	public EntityFactory getEntityFactory() {
		return entityFactory;
	}

	 public IResourceRetriever getRm() {
	 	return rm;
	 }

    public PooledEngine getEngine() {
        return engine;
    }

	public RayHandler getRayHandler() {
		return rayHandler;
	}

	public World getWorld() {
		return world;
	}

	public float getPixelsPerWU() {
		return pixelsPerWU;
	}

	public Entity getRoot() {
		return rootEntity;
	}
	
	/** Returns a new instance of the default shader used by SpriteBatch for GL2 when no shader is specified. */
	static public ShaderProgram createDefaultShader () {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "varying LOWP vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "uniform sampler2D u_texture;\n" //
			+ "uniform vec2 atlasCoord;\n" //
			+ "uniform vec2 atlasSize;\n" //
			+ "uniform int isRepeat;\n" //
			+ "void main()\n"//
			+ "{\n" //
			+ "vec4 textureSample = vec4(0.0,0.0,0.0,0.0);\n"//
			+ "if(isRepeat == 1)\n"//
			+ "{\n"//
			+ "textureSample = v_color * texture2D(u_texture, atlasCoord+mod(v_texCoords, atlasSize));\n"//
			+ "}\n"//
			+ "else\n"//
			+ "{\n"//
			+ "textureSample = v_color * texture2D(u_texture, v_texCoords);\n"//
			+ "}\n"//
			+ "  gl_FragColor = textureSample;\n" //
			+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}

    public Batch getBatch() {
        return renderer.getBatch();
    }
}
