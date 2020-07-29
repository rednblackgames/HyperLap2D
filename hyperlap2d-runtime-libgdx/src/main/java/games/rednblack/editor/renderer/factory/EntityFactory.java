package games.rednblack.editor.renderer.factory;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.Viewport;
import games.rednblack.editor.renderer.commons.IExternalItemType;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.ViewPortComponent;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.factory.component.*;
import games.rednblack.editor.renderer.resources.IResourceRetriever;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class EntityFactory {

    protected ComponentFactory compositeComponentFactory, lightComponentFactory, particleEffectComponentFactory,
            simpleImageComponentFactory, spriteComponentFactory, spriterComponentFactory, labelComponentFactory,
            ninePatchComponentFactory, colorPrimitiveFactory;

    private final HashMap<Integer, ComponentFactory> externalFactories = new HashMap<>();
    private final HashMap<Integer, Entity> entities = new HashMap<>();

    public static final int IMAGE_TYPE = 1;
    public static final int LABEL_TYPE = 2;
    public static final int SPRITE_TYPE = 3;
    public static final int SPINE_TYPE = 4;
    public static final int SPRITER_TYPE = 5;
    public static final int COMPOSITE_TYPE = 6;
    public static final int PARTICLE_TYPE = 7;
    public static final int LIGHT_TYPE = 8;
    public static final int NINE_PATCH = 9;
    public static final int COLOR_PRIMITIVE = 10;

    public RayHandler rayHandler;
    public World world;
    public IResourceRetriever rm = null;
    public PooledEngine engine;

    public EntityFactory(PooledEngine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        this.engine = engine;
        this.rayHandler = rayHandler;
        this.world = world;
        this.rm = rm;

        compositeComponentFactory = new CompositeComponentFactory(engine, rayHandler, world, rm);
        lightComponentFactory = new LightComponentFactory(engine, rayHandler, world, rm);
        particleEffectComponentFactory = new ParticleEffectComponentFactory(engine, rayHandler, world, rm);
        simpleImageComponentFactory = new SimpleImageComponentFactory(engine, rayHandler, world, rm);
        spriteComponentFactory = new SpriteComponentFactory(engine, rayHandler, world, rm);
        spriterComponentFactory = new SpriterComponentFactory(engine, rayHandler, world, rm);
        labelComponentFactory = new LabelComponentFactory(engine, rayHandler, world, rm);
        ninePatchComponentFactory = new NinePatchComponentFactory(engine, rayHandler, world, rm);
        colorPrimitiveFactory = new ColorPrimitiveComponentFactory(engine, rayHandler, world, rm);
    }

    public ComponentFactory getCompositeComponentFactory() {
        return compositeComponentFactory;
    }

    public SpriteComponentFactory getSpriteComponentFactory() {
        return (SpriteComponentFactory) spriteComponentFactory;
    }

    public void addExternalFactory(IExternalItemType itemType) {
        externalFactories.put(itemType.getTypeId(), itemType.getComponentFactory());
    }

    public Entity createEntity(Entity root, SimpleImageVO vo) {

        Entity entity = engine.createEntity();

        simpleImageComponentFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createEntity(Entity root, Image9patchVO vo) {

        Entity entity = engine.createEntity();

        ninePatchComponentFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createEntity(Entity root, LabelVO vo) {

        Entity entity = engine.createEntity();

        labelComponentFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createEntity(Entity root, ParticleEffectVO vo) {

        Entity entity = engine.createEntity();

        particleEffectComponentFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createEntity(Entity root, LightVO vo) {

        Entity entity = engine.createEntity();

        lightComponentFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createEntity(Entity root, SpineVO vo) {

        Entity entity = engine.createEntity();

        ComponentFactory factory = externalFactories.get(SPINE_TYPE);
        if (factory != null) {
            factory.createComponents(root, entity, vo);
            postProcessEntity(entity);
        }

        return entity;
    }

    public Entity createEntity(Entity root, SpriteAnimationVO vo) {

        Entity entity = engine.createEntity();

        spriteComponentFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createEntity(Entity root, SpriterVO vo) {

        Entity entity = engine.createEntity();

        spriterComponentFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createEntity(Entity root, CompositeItemVO vo) {

        Entity entity = engine.createEntity();

        compositeComponentFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createEntity(Entity root, ColorPrimitiveVO vo) {

        Entity entity = engine.createEntity();

        colorPrimitiveFactory.createComponents(root, entity, vo);

        postProcessEntity(entity);

        return entity;
    }

    public Entity createRootEntity(CompositeVO compositeVo, Viewport viewport) {

        CompositeItemVO vo = new CompositeItemVO();
        vo.composite = compositeVo;
        vo.automaticResize = false;

        Entity entity = engine.createEntity();

        compositeComponentFactory.createComponents(null, entity, vo);
        TransformComponent transform = engine.createComponent(TransformComponent.class);

        ViewPortComponent viewPortComponent = engine.createComponent(ViewPortComponent.class);
        viewPortComponent.viewPort = viewport;

        viewPortComponent.viewPort.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        entity.add(transform);
        entity.add(viewPortComponent);

        postProcessEntity(entity);

        return entity;
    }

    public Integer postProcessEntity(Entity entity) {
        ComponentMapper<MainItemComponent> mainItemComponentComponentMapper = ComponentMapper.getFor(MainItemComponent.class);
        MainItemComponent mainItemComponent = mainItemComponentComponentMapper.get(entity);
        if (mainItemComponent.uniqueId == -1) mainItemComponent.uniqueId = getFreeId();
        entities.put(mainItemComponent.uniqueId, entity);

        return mainItemComponent.uniqueId;
    }

    private int getFreeId() {
        if (entities == null || entities.size() == 0) return 1;
        ArrayList<Integer> ids = new ArrayList<Integer>(entities.keySet());
        Collections.sort(ids);
        for (int i = 1; i < ids.size(); i++) {
            if (ids.get(i) - ids.get(i - 1) > 1) {
                return ids.get(i - 1) + 1;
            }
        }
        return ids.get(ids.size() - 1) + 1;
    }

    public Integer updateMap(Entity entity) {
        ComponentMapper<MainItemComponent> mainItemComponentComponentMapper = ComponentMapper.getFor(MainItemComponent.class);
        MainItemComponent mainItemComponent = mainItemComponentComponentMapper.get(entity);
        entities.put(mainItemComponent.uniqueId, entity);

        return mainItemComponent.uniqueId;
    }

    public void initAllChildren(Engine engine, Entity entity, CompositeVO vo) {
        for (int i = 0; i < vo.sImages.size(); i++) {
            Entity child = createEntity(entity, vo.sImages.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sImage9patchs.size(); i++) {
            Entity child = createEntity(entity, vo.sImage9patchs.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sLabels.size(); i++) {
            Entity child = createEntity(entity, vo.sLabels.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sParticleEffects.size(); i++) {
            Entity child = createEntity(entity, vo.sParticleEffects.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sLights.size(); i++) {
            Entity child = createEntity(entity, vo.sLights.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sSpineAnimations.size(); i++) {
            Entity child = createEntity(entity, vo.sSpineAnimations.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sSpriteAnimations.size(); i++) {
            Entity child = createEntity(entity, vo.sSpriteAnimations.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sSpriterAnimations.size(); i++) {
            Entity child = createEntity(entity, vo.sSpriterAnimations.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sColorPrimitives.size(); i++) {
            Entity child = createEntity(entity, vo.sColorPrimitives.get(i));
            engine.addEntity(child);
        }

        for (int i = 0; i < vo.sComposites.size(); i++) {
            Entity child = createEntity(entity, vo.sComposites.get(i));
            engine.addEntity(child);
            initAllChildren(engine, child, vo.sComposites.get(i).composite);
        }
    }

    public Entity getEntityByUniqueId(Integer id) {
        return entities.get(id);
    }


    public void clean() {
        entities.clear();
    }
}
