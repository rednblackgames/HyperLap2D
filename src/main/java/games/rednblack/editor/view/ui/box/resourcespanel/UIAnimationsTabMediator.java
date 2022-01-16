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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import games.rednblack.editor.controller.commands.resource.DeleteSpineAnimation;
import games.rednblack.editor.controller.commands.resource.DeleteSpriteAnimation;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.BoxItemResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.SpineResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.box.SpriteResource;
import games.rednblack.h2d.extension.spine.SpineItemType;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIAnimationsTabMediator extends UIResourcesTabMediator<UIAnimationsTab> {

    private static final String TAG = UIAnimationsTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;
    private Array<DraggableResource> animationBoxes;

    public UIAnimationsTabMediator() {
        super(NAME, new UIAnimationsTab());
        animationBoxes = new Array<>();
    }

    @Override
    public String[] listNotificationInterests() {
        String[] listNotification = super.listNotificationInterests();
        listNotification = ArrayUtils.add(listNotification, DeleteSpineAnimation.DONE);
        listNotification = ArrayUtils.add(listNotification, DeleteSpriteAnimation.DONE);
        return listNotification;
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case DeleteSpineAnimation.DONE:
            case DeleteSpriteAnimation.DONE:
                initList(viewComponent.searchString);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initList(String searchText) {
        animationBoxes.clear();
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);

        if (new SpineItemType().getTypeId() == SpineItemType.SPINE_TYPE) {
            createAnimationResources(resourceManager.getProjectSpineAnimationsList().keySet(), SpineResource.class, ItemFactory.get()::createSpineAnimation, searchText);
        }
        createAnimationResources(resourceManager.getProjectSpriteAnimationsList().keySet(), SpriteResource.class, ItemFactory.get()::createSpriteAnimation, searchText);
        animationBoxes.sort();
        viewComponent.setThumbnailBoxes(animationBoxes);
    }

    private void createAnimationResources(Set<String> strings, Class<? extends BoxItemResource> resourceClass, BiFunction<String, Vector2, Boolean> factoryFunction, String searchText) {
        for (String animationName : strings) {
            if (!animationName.toLowerCase().contains(searchText)
                    || filterResource(animationName, resourceClass == SpineResource.class ? SpineItemType.SPINE_TYPE : EntityFactory.SPRITE_TYPE))
                continue;

            try {
                Constructor<? extends BoxItemResource> constructor = resourceClass.getConstructor(String.class, boolean.class);
                DraggableResource draggableResource = new DraggableResource(constructor.newInstance(animationName, true));
                draggableResource.initDragDrop();
                draggableResource.setFactoryFunction(factoryFunction);
                animationBoxes.add(draggableResource);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
