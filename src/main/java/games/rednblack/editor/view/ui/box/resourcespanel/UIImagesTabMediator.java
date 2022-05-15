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

package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import dev.lyze.gdxtinyvg.drawers.TinyVGShapeDrawer;
import games.rednblack.editor.controller.commands.resource.DeleteImageResource;
import games.rednblack.editor.controller.commands.resource.DeleteTinyVGResource;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.data.ProjectInfoVO;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.ImageResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.TinyVGResource;
import games.rednblack.editor.view.ui.widget.actors.basic.WhitePixel;
import games.rednblack.h2d.extension.tinyvg.CpuTinyVGShapeDrawer;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIImagesTabMediator extends UIResourcesTabMediator<UIImagesTab> {

    private static final String TAG = UIImagesTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final Array<DraggableResource> thumbnailBoxes = new Array<>();

    public UIImagesTabMediator() {
        super(NAME, new UIImagesTab());
    }

    private TinyVGShapeDrawer drawer;

    @Override
    public String[] listNotificationInterests() {
        String[] listNotification = super.listNotificationInterests();

        listNotification = ArrayUtils.add(listNotification, DeleteImageResource.DONE);
        listNotification = ArrayUtils.add(listNotification, DeleteTinyVGResource.DONE);

        return listNotification;
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case DeleteTinyVGResource.DONE:
            case DeleteImageResource.DONE:
                initList(viewComponent.searchString);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initList(String searchText) {
        if (drawer == null)
            drawer = new CpuTinyVGShapeDrawer(Sandbox.getInstance().getUIStage().getBatch(), WhitePixel.sharedInstance.textureRegion);

        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);
        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        ProjectInfoVO projectInfoVO = projectManager.getCurrentProjectInfoVO();

        thumbnailBoxes.clear();

        for (String atlasName : projectInfoVO.imagesPacks.keySet()) {
            TextureAtlas atlas = resourceManager.getTextureAtlas(atlasName);
            Array<TextureAtlas.AtlasRegion> atlasRegions = atlas.getRegions();

            for (TextureAtlas.AtlasRegion region : new Array.ArrayIterator<>(atlasRegions)) {
                if (region.name.equals("white-pixel")) continue;
                if(!projectInfoVO.imagesPacks.get(atlasName).regions.contains(region.name)
                        || !region.name.toLowerCase().contains(searchText)
                        || filterResource(region.name, EntityFactory.IMAGE_TYPE)) continue;

                boolean is9patch = region.findValue("split") != null;
                ImageResource imageResource = new ImageResource(region, true);
                DraggableResource draggableResource = new DraggableResource(imageResource);
                if (is9patch) {
                    draggableResource.setFactoryFunction(ItemFactory.get()::create9Patch);
                } else {
                    draggableResource.setFactoryFunction(ItemFactory.get()::createSimpleImage);
                }
                draggableResource.initDragDrop();
                thumbnailBoxes.add(draggableResource);
            }
        }

        for (String name : resourceManager.getTinyVGList().keySet()) {
            try {
                TinyVGResource tinyVGResource = new TinyVGResource(name, resourceManager.getOriginalTinyVG(name), drawer);
                DraggableResource draggableResource = new DraggableResource(tinyVGResource);
                draggableResource.setFactoryFunction(ItemFactory.get()::tryCreateTinyVGItem);
                draggableResource.initDragDrop();
                thumbnailBoxes.add(draggableResource);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        thumbnailBoxes.sort();
        viewComponent.setThumbnailBoxes(thumbnailBoxes);
    }
}
