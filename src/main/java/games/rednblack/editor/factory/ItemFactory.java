/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package games.rednblack.editor.factory;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.PasteItemsCommand;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.MainItemComponent;
import games.rednblack.editor.renderer.data.*;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.tools.TextTool;
import games.rednblack.editor.view.ui.box.UILayerBoxMediator;
import games.rednblack.h2d.common.factory.IFactory;

import java.util.HashMap;

/**
 * Created by azakhary on 6/5/2015.
 *
 */
public class ItemFactory implements IFactory {

    private final EntityFactory entityFactory;
    private final SceneLoader sceneLoader;
    private final Sandbox sandbox;
    private Entity createdEntity;

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

    @Override
    public boolean createSimpleImage(String regionName, Vector2 position) {
        SimpleImageVO vo = new SimpleImageVO();
        vo.imageName = regionName;

        if(!setEssentialData(vo, position)) return false;
        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        return true;
    }

    @Override
    public Entity getCreatedEntity() {
        return createdEntity;
    }

    public boolean create9Patch(String regionName, Vector2 position) {
        Image9patchVO vo = new Image9patchVO();
        vo.imageName = regionName;

        if(!setEssentialData(vo, position)) return false;
        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        return true;
    }

    @Override
    public boolean createSpriteAnimation(String animationName, Vector2 position) {
        SpriteAnimationVO vo = new SpriteAnimationVO();
        vo.animationName = animationName;
        vo.playMode = 2;

        if(!setEssentialData(vo, position)) return false;
        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        return true;
    }

    @Override
    public boolean createSpineAnimation(String animationName, Vector2 position) {
        SpineVO vo = new SpineVO();
        vo.animationName = animationName;

        if(!setEssentialData(vo, position)) return false;
        createdEntity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        return true;
    }

    public boolean createPrimitive(Vector2 position, ShapeVO shape) {
        ColorPrimitiveVO vo = new ColorPrimitiveVO();
        vo.shape = shape.clone();
        vo.originX = vo.shape.polygons[0][2].x / 2;
        vo.originY = vo.shape.polygons[0][2].y / 2;

        if(!setEssentialData(vo, position)) return false;
        Entity entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);

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

        if (createdEntity == null) return false;

        //adding library name
        MainItemComponent mainItemComponent = ComponentRetriever.get(createdEntity, MainItemComponent.class);
        mainItemComponent.libraryLink = libraryName;

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, createdEntity);

        return true;
    }

    public Entity createCompositeItem(CompositeItemVO vo, Vector2 position) {
        if(!setEssentialData(vo, position)) return null;

        Entity entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        Engine engine = sceneLoader.getEngine();
        EntityFactory factory = sceneLoader.getEntityFactory();
        factory.initAllChildren(engine, entity, vo.composite);

        return entity;
    }

    public Entity createCompositeItem(Vector2 position) {
        CompositeItemVO vo = new CompositeItemVO();
        Entity entity = createCompositeItem(vo, position);
        return entity;
    }

    public Entity createLightItem(LightVO vo, Vector2 position) {
        if(!setEssentialData(vo, position)) return null;
        Entity entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);

        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);
/*
        DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
        float boundBoxSize = 10f;
        dimensionsComponent.boundBox = new Rectangle(-boundBoxSize / 2f, -boundBoxSize / 2f, boundBoxSize, boundBoxSize);*/


        return entity;
    }

    public boolean tryCreateParticleItem(String particleName, Vector2 position) {
        Entity entity = createParticleItem(particleName, position);

       /* DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
        float boundBoxSize = 10f;
        dimensionsComponent.boundBox = new Rectangle(-boundBoxSize / 2f, -boundBoxSize / 2f, boundBoxSize, boundBoxSize);*/

        if(entity == null) return false;

        return true;
    }

    public Entity createParticleItem(String particleName, Vector2 position) {
        ParticleEffectVO vo = new ParticleEffectVO();
        vo.particleName = particleName;

        if(!setEssentialData(vo, position)) return null;
        Entity entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);

        return entity;
    }

    public boolean tryCreateTalosItem(String particleName, Vector2 position) {
        Entity entity = createTalosItem(particleName, position);

       /* DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
        float boundBoxSize = 10f;
        dimensionsComponent.boundBox = new Rectangle(-boundBoxSize / 2f, -boundBoxSize / 2f, boundBoxSize, boundBoxSize);*/

        if(entity == null) return false;

        return true;
    }

    public Entity createTalosItem(String particleName, Vector2 position) {
        TalosVO vo = new TalosVO();
        vo.particleName = particleName;

        if(!setEssentialData(vo, position)) return null;
        Entity entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);

        return entity;
    }

    public Entity createLabel(TextTool textSettings, Vector2 position) {
        LabelVO vo = new LabelVO();
        if(!setEssentialData(vo, position)) return null;

        HyperLap2DFacade facade = HyperLap2DFacade.getInstance();
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);

        resourceManager.prepareEmbeddingFont(textSettings.getFontFamily(), textSettings.getFontSize());

        // using long unique name
        vo.style = textSettings.getFontFamily();
        vo.text = "LABEL";
        vo.size = textSettings.getFontSize();

        // need to calculate minimum bounds size here
        vo.width = 120f/Sandbox.getInstance().getPixelPerWU();
        vo.height = 50f/Sandbox.getInstance().getPixelPerWU();

        Entity entity = entityFactory.createEntity(sandbox.getCurrentViewingEntity(), vo);
        HyperLap2DFacade.getInstance().sendNotification(MsgAPI.ACTION_CREATE_ITEM, entity);

        return entity;
    }
}
