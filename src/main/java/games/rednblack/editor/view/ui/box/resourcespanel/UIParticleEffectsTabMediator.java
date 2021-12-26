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
import games.rednblack.editor.controller.commands.resource.DeleteParticleEffect;
import games.rednblack.editor.controller.commands.resource.DeleteTalosVFX;
import games.rednblack.editor.factory.ItemFactory;
import games.rednblack.editor.proxy.ResourceManager;
import games.rednblack.editor.renderer.factory.EntityFactory;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResourceView;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.list.ParticleEffectResource;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.list.TalosResource;
import games.rednblack.h2d.extension.talos.TalosItemType;
import org.apache.commons.lang3.ArrayUtils;
import org.puremvc.java.interfaces.INotification;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Created by azakhary on 4/17/2015.
 */
public class UIParticleEffectsTabMediator extends UIResourcesTabMediator<UIParticleEffectsTab> {

    private static final String TAG = UIParticleEffectsTabMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private final Array<DraggableResource> particlesList;

    public UIParticleEffectsTabMediator() {
        super(NAME, new UIParticleEffectsTab());
        particlesList = new Array<>();
    }

    @Override
    public String[] listNotificationInterests() {
        String[] listNotification = super.listNotificationInterests();
        listNotification = ArrayUtils.add(listNotification, DeleteParticleEffect.DONE);
        listNotification = ArrayUtils.add(listNotification, DeleteTalosVFX.DONE);
        return listNotification;
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        switch (notification.getName()) {
            case DeleteTalosVFX.DONE:
            case DeleteParticleEffect.DONE:
                initList(viewComponent.searchString);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initList(String searchText) {
        particlesList.clear();
        ResourceManager resourceManager = facade.retrieveProxy(ResourceManager.NAME);

        if (new TalosItemType().getTypeId() == TalosItemType.TALOS_TYPE) {
            createParticleResources(resourceManager.getProjectTalosList().keySet(), TalosResource.class, ItemFactory.get()::tryCreateTalosItem, searchText);
        }

        createParticleResources(resourceManager.getProjectParticleList().keySet(), ParticleEffectResource.class, ItemFactory.get()::tryCreateParticleItem, searchText);
        particlesList.sort();
        viewComponent.setItems(particlesList);
    }


    private void createParticleResources(Set<String> strings, Class resourceClass, BiFunction<String, Vector2, Boolean> factoryFunction, String searchText) {
        for (String particleName : strings) {
            if (!particleName.toLowerCase().contains(searchText)
                || filterResource(particleName, resourceClass == TalosResource.class ? TalosItemType.TALOS_TYPE : EntityFactory.PARTICLE_TYPE)) continue;
            try {
                Constructor constructor = resourceClass.getConstructor(String.class);
                DraggableResource draggableResource = new DraggableResource((DraggableResourceView) constructor.newInstance(particleName));
                draggableResource.setFactoryFunction(factoryFunction);
                particlesList.add(draggableResource);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
