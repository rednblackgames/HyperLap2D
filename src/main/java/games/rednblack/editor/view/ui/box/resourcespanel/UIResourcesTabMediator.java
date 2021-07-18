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

import com.badlogic.gdx.utils.ObjectMap;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.filters.IAbstractResourceFilter;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

/**
 * Created by sargis on 5/12/15.
 */
public abstract class UIResourcesTabMediator<T extends UIResourcesTab> extends Mediator<T> {
    private static final String NAME = "games.rednblack.editor.view.ui.box.resourcespanel.UIResourcesTabMediator";
    public static final String CHANGE_ACTIVE_FILTER = NAME + ".CHANGE_ACTIVE_FILTER";

    protected ObjectMap<String, IAbstractResourceFilter> filters = new ObjectMap<>();

    private SettingsManager settingsManager;

    /**
     * Constructor.
     *
     * @param mediatorName
     * @param viewComponent
     */
    public UIResourcesTabMediator(String mediatorName, T viewComponent) {
        super(mediatorName, viewComponent);
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        settingsManager = facade.retrieveProxy(SettingsManager.NAME);
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ProjectManager.PROJECT_OPENED,
                ProjectManager.PROJECT_DATA_UPDATED,
                MsgAPI.ADD_RESOURCES_BOX_FILTER,
                MsgAPI.UPDATE_RESOURCES_LIST,
                CHANGE_ACTIVE_FILTER
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        IAbstractResourceFilter filter;

        switch (notification.getName()) {
            case ProjectManager.PROJECT_OPENED:
            case ProjectManager.PROJECT_DATA_UPDATED:
            case MsgAPI.UPDATE_RESOURCES_LIST:
                initList(viewComponent.searchString);
                break;
            case MsgAPI.ADD_RESOURCES_BOX_FILTER:
                filter = notification.getBody();
                filter.setActive(settingsManager.editorConfigVO.enabledFilters.getOrDefault(filter.id, false));
                if (!filters.containsKey(filter.id))
                    filters.put(filter.id, filter);
                break;
            case CHANGE_ACTIVE_FILTER:
                if (filters.containsKey(notification.getType())) {
                    filter = filters.get(notification.getType());
                    filter.setActive(notification.getBody());
                    settingsManager.editorConfigVO.enabledFilters.put(filter.id, filter.isActive());
                    settingsManager.saveEditorConfig();
                }
                initList(viewComponent.searchString);
                break;
            default:
                break;
        }
    }

    protected abstract void initList(String searchText);

    protected boolean filterResource(String resName, int resType) {
        for (IAbstractResourceFilter filter : filters.values()) {
            if (filter.isActive() && filter.filterResource(resName, resType))
                return true;
        }
        return false;
    }
}
