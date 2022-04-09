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
import games.rednblack.editor.renderer.factory.component.ComponentFactory;
import games.rednblack.editor.utils.runtime.SandboxComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.tools.TextTool;
import games.rednblack.editor.view.ui.box.UILayerBoxMediator;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.factory.IFactory;
import games.rednblack.h2d.extension.spine.SpineItemType;
import games.rednblack.h2d.extension.talos.TalosItemType;
import games.rednblack.h2d.extension.tinyvg.TinyVGItemType;

import java.util.HashMap;

public class ItemFactory implements IFactory {

    private final EntityFactory entityFactory;
    private final SceneLoader sceneLoader;
    private final Sandbox sandbox;
    private int createdEntity;

    private static ItemFactory instance;

    private ItemFactory(SceneLoader sceneLoader) {
        this.sceneLoader = sceneLoader;
        entityFactory = sceneLoader.getEntityFactory();
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

    private boolean setEssentialData(ComponentFactory.InitialData vo, Vector2 position) {
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
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }

        data.data = regionName;

        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), EntityFactory.IMAGE_TYPE, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        Pools.free(data);
        return true;
    }

    @Override
    public int getCreatedEntity() {
        return createdEntity;
    }

    public boolean create9Patch(String regionName, Vector2 position) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }
        data.data = regionName;

        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), EntityFactory.NINE_PATCH, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        Pools.free(data);
        return true;
    }

    @Override
    public boolean createSpriteAnimation(String animationName, Vector2 position) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }
        data.data = animationName;

        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), EntityFactory.SPRITE_TYPE, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        Pools.free(data);
        return true;
    }

    @Override
    public boolean createSpineAnimation(String animationName, Vector2 position) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }
        data.data = animationName;

        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), SpineItemType.SPINE_TYPE, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        Pools.free(data);
        return true;
    }

    public boolean createPrimitive(Vector2 position, PolygonShapeVO shape) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return false;
        }
        Object[] params = new Object[5];
        data.data = params;
        params[0] = shape.clone().vertices;
        params[1] = shape.clone().polygonizedVertices;
        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), EntityFactory.COLOR_PRIMITIVE, data);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);
        Pools.free(data);
        return true;
    }

    @Override
    public boolean createItemFromLibrary(String libraryName, Vector2 position) {
        ProjectManager projectManager = HyperLap2DFacade.getInstance().retrieveProxy(ProjectManager.NAME);
        HashMap<String, CompositeItemVO> libraryItems = projectManager.currentProjectInfoVO.libraryItems;

        CompositeItemVO itemVO = libraryItems.get(libraryName);
        itemVO.cleanIds();
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
        factory.initAllChildren(entity, vo);

        return entity;
    }

    public int createCompositeItem(Vector2 position) {
        CompositeItemVO vo = new CompositeItemVO();
        return createCompositeItem(vo, position);
    }

    public int createLightItem(LightVO vo, Vector2 position) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return -1;
        }
        data.data = vo.type;

        int entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), EntityFactory.LIGHT_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);

        Pools.free(data);
        return entity;
    }

    public boolean tryCreateTinyVGItem(String name, Vector2 position) {
        int entity = createTinyVGItem(name, position);
        return entity != -1;
    }

    public int createTinyVGItem(String name, Vector2 position) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return -1;
        }
        data.data = name;
        int entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), TinyVGItemType.TINYVG_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);
        Pools.free(data);
        return entity;
    }

    public boolean tryCreateParticleItem(String particleName, Vector2 position) {
        int entity = createParticleItem(particleName, position);
        return entity != -1;
    }

    public int createParticleItem(String particleName, Vector2 position) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return -1;
        }
        data.data = particleName;
        int entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), EntityFactory.PARTICLE_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);
        Pools.free(data);
        return entity;
    }

    public boolean tryCreateTalosItem(String particleName, Vector2 position) {
        int entity = createTalosItem(particleName, position);
        return entity != -1;
    }

    public int createTalosItem(String particleName, Vector2 position) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
        if(!setEssentialData(data, position)) {
            Pools.free(data);
            return -1;
        }
        data.data = particleName;
        int entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), TalosItemType.TALOS_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);

        Pools.free(data);
        return entity;
    }

    public int createLabel(TextTool textSettings, Vector2 position) {
        ComponentFactory.InitialData data = Pools.obtain(ComponentFactory.InitialData.class);
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

        int entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), EntityFactory.LABEL_TYPE, data);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);
        Pools.free(data);
        return entity;
    }
}
