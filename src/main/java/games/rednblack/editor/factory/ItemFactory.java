package games.rednblack.editor.factory;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pools;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.PasteItemsCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.factory.v2.ComponentFactoryV2;
import games.rednblack.editor.renderer.factory.v2.EntityFactoryV2;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.tools.TextTool;
import games.rednblack.editor.view.ui.box.UILayerBoxMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.factory.IFactory;

import java.util.HashMap;

public class ItemFactory implements IFactory {

    private final EntityFactory entityFactory;
    private final EntityFactoryV2 entityFactoryV2;
    private final SceneLoader sceneLoader;
    private final Sandbox sandbox;
    private int createdEntity;

    private static ItemFactory instance;

    private ItemFactory(SceneLoader sceneLoader) {
        this.sceneLoader = sceneLoader;
        entityFactory = sceneLoader.getEntityFactory();
        entityFactoryV2 = sceneLoader.getEntityFactoryV2();
        sandbox = Sandbox.getInstance();
    }

    public static ItemFactory get() {
        if(instance == null) {
            instance = new ItemFactory(Sandbox.getInstance().sceneControl.sceneLoader);
        }

        return instance;
    }

    private boolean setEssentialData(MainItemVO vo, Vector2 position) {
        UILayerBoxMediator layerBoxMediator = HyperLap2DFacade.getInstance().retrieveMediator(UILayerBoxMediator.NAME);
        String layerName = layerBoxMediator.getCurrentSelectedLayerName();

        if(layerName == null) return false;

        vo.layerName = layerName;

        // This is for grid
        position.x = MathUtils.floor(position.x / sandbox.getWorldGridSize()) * sandbox.getWorldGridSize();
        position.y = MathUtils.floor(position.y / sandbox.getWorldGridSize()) * sandbox.getWorldGridSize();

        vo.x = position.x;
        vo.y = position.y;

        return true;
    }

    private boolean setEssentialData(ComponentFactoryV2.InitialData vo, Vector2 position) {
        UILayerBoxMediator layerBoxMediator = HyperLap2DFacade.getInstance().retrieveMediator(UILayerBoxMediator.NAME);
        String layerName = layerBoxMediator.getCurrentSelectedLayerName();

        if(layerName == null) return false;

        vo.layerName = layerName;

        // This is for grid
        position.x = MathUtils.floor(position.x / sandbox.getWorldGridSize()) * sandbox.getWorldGridSize();
        position.y = MathUtils.floor(position.y / sandbox.getWorldGridSize()) * sandbox.getWorldGridSize();

        vo.x = position.x;
        vo.y = position.y;

        return true;
    }

    @Override
    public boolean createSimpleImage(String regionName, Vector2 position) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }

        data.data = regionName;

        createdEntity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.IMAGE_TYPE, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        Pools.free(data);
        return true;
    }

    @Override
    public int getCreatedEntity() {
        return createdEntity;
    }

    public boolean create9Patch(String regionName, Vector2 position) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }
        data.data = regionName;

        createdEntity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.NINE_PATCH, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        Pools.free(data);
        return true;
    }

    @Override
    public boolean createSpriteAnimation(String animationName, Vector2 position) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }
        data.data = animationName;

        createdEntity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.SPRITE_TYPE, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        Pools.free(data);
        return true;
    }

    @Override
    public boolean createSpineAnimation(String animationName, Vector2 position) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }
        data.data = animationName;

        createdEntity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.SPINE_TYPE, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        Pools.free(data);
        return true;
    }

    public boolean createPrimitive(Vector2 position, PolygonShapeVO shape) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }
        data.data = shape.clone().polygons;
        createdEntity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.COLOR_PRIMITIVE, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);
        Pools.free(data);
        return true;
    }

    @Override
    public boolean createItemFromLibrary(String libraryName, Vector2 position) {
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, CompositeItemVO> libraryItems = projectManager.currentProjectInfoVO.libraryItems;

        CompositeItemVO itemVO = libraryItems.get(libraryName);
        itemVO.uniqueId = -1;
        PasteItemsCommand.forceIdChange(itemVO.composite);
        createdEntity = createCompositeItem(itemVO, position);

        if (createdEntity == -1) return false;

        //adding library name
        MainItemComponent mainItemComponent = SandboxComponentRetriever.get(createdEntity, MainItemComponent.class);
        mainItemComponent.libraryLink = libraryName;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        return true;
    }

    public int createCompositeItem(CompositeItemVO vo, Vector2 position) {
        if(!setEssentialData(vo, position)) return -1;

        int entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        EntityFactory factory = sceneLoader.getEntityFactory();
        factory.initAllChildren(entity, vo.composite);

        return entity;
    }

    public int createCompositeItem(Vector2 position) {
        CompositeItemVO vo = new CompositeItemVO();
        return createCompositeItem(vo, position);
    }

    public int createLightItem(LightVO vo, Vector2 position) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return -1;
        }
        data.data = vo.type;

        int entity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.LIGHT_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);

        Pools.free(data);
        return entity;
    }

    public boolean tryCreateParticleItem(String particleName, Vector2 position) {
        int entity = createParticleItem(particleName, position);
        return entity != -1;
    }

    public int createParticleItem(String particleName, Vector2 position) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return -1;
        }
        data.data = particleName;
        int entity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.PARTICLE_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);
        Pools.free(data);
        return entity;
    }

    public boolean tryCreateTalosItem(String particleName, Vector2 position) {
        int entity = createTalosItem(particleName, position);
        return entity != -1;
    }

    public int createTalosItem(String particleName, Vector2 position) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return -1;
        }
        data.data = particleName;
        int entity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.TALOS_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);

        Pools.free(data);
        return entity;
    }

    public int createLabel(TextTool textSettings, Vector2 position) {
        ComponentFactoryV2.InitialData data = Pools.obtain(ComponentFactoryV2.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return -1;
        }
        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);

        resourceManager.prepareEmbeddingFont(textSettings.getFontFamily(), textSettings.getFontSize(), false);

        Object[] params = new Object[5];
        params[0] = "LABEL";
        params[1] = textSettings.getFontFamily();
        params[2] = textSettings.getFontSize();
        params[3] = false;
        params[4] = false;
        data.data = params;

        int entity = entityFactoryV2.createEntity(sandbox.getCurrentViewingEntity(), EntityFactoryV2.LABEL_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);
        Pools.free(data);
        return entity;
    }
}
