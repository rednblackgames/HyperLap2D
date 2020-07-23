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

package games.rednblack.editor.renderer.factory.component;

import box2dLight.RayHandler;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.physics.box2d.World;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.PolygonComponent;
import games.rednblack.editor.renderer.components.TextureRegionComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.data.MainItemVO;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.data.ResolutionEntryVO;
import games.rednblack.editor.renderer.data.SimpleImageVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.renderer.resources.IResourceRetriever;
import games.rednblack.editor.renderer.utils.ComponentRetriever;

/**
 * Created by azakhary on 5/22/2015.
 */
public class SimpleImageComponentFactory extends ComponentFactory {

   private TextureRegionComponent textureRegionComponent;

    public SimpleImageComponentFactory(PooledEngine engine, RayHandler rayHandler, World world, IResourceRetriever rm) {
        super(engine, rayHandler, world, rm);
    }

    public void createComponents(Entity root, Entity entity, MainItemVO vo) {
    	textureRegionComponent = createTextureRegionComponent(entity, (SimpleImageVO) vo);
        createCommonComponents( entity, vo, EntityFactory.IMAGE_TYPE);
        createParentNodeComponent(root, entity);
        createNodeComponent(root, entity);
        updatePolygons(entity);
    }

    private void updatePolygons(Entity entity) {
    	TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
    	DimensionsComponent dimensionsComponent = ComponentRetriever.get(entity, DimensionsComponent.class);
    	
        ProjectInfoVO projectInfoVO = rm.getProjectVO();
        TransformComponent transformComponent = ComponentRetriever.get(entity, TransformComponent.class);
    	PolygonComponent polygonComponent = ComponentRetriever.get(entity, PolygonComponent.class);
    	if(textureRegionComponent.isPolygon && polygonComponent != null && polygonComponent.vertices != null) {
    		textureRegionComponent.setPolygonSprite(polygonComponent, projectInfoVO.pixelToWorld, transformComponent.scaleX, transformComponent.scaleY);
    		dimensionsComponent.setPolygon(polygonComponent);
    	}
	}

	@Override
    protected DimensionsComponent createDimensionsComponent(Entity entity, MainItemVO vo) {
        DimensionsComponent component = engine.createComponent(DimensionsComponent.class);

        TextureRegionComponent textureRegionComponent = ComponentRetriever.get(entity, TextureRegionComponent.class);
        
        ResolutionEntryVO resolutionEntryVO = rm.getLoadedResolution();
        ProjectInfoVO projectInfoVO = rm.getProjectVO();
        float multiplier = resolutionEntryVO.getMultiplier(rm.getProjectVO().originalResolution);
        
        component.width = (float) textureRegionComponent.region.getRegionWidth() * multiplier / projectInfoVO.pixelToWorld;
        component.height = (float) textureRegionComponent.region.getRegionHeight() * multiplier / projectInfoVO.pixelToWorld;
        entity.add(component);

        return component;
    }

    protected TextureRegionComponent createTextureRegionComponent(Entity entity, SimpleImageVO vo) {
        TextureRegionComponent component = engine.createComponent(TextureRegionComponent.class);
        component.regionName = vo.imageName;
        component.region = rm.getTextureRegion(vo.imageName);
        component.isRepeat = vo.isRepeat;
        component.isPolygon = vo.isPolygon;
        entity.add(component);

        return component;
    }
}
