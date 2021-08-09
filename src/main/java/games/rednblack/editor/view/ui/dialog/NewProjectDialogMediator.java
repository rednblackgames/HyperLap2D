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

package games.rednblack.editor.view.ui.dialog;

import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.view.menu.FileMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

/**
 * Created by sargis on 4/1/15.
 */
public class NewProjectDialogMediator extends Mediator<NewProjectDialog> {
    private static final String TAG = NewProjectDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public NewProjectDialogMediator() {
        super(NAME, new NewProjectDialog());
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                FileMenu.NEW_PROJECT,
                NewProjectDialog.CREATE_BTN_CLICKED
        };
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        viewComponent.setDefaultWorkspacePath(settingsManager.getWorkspacePath().path());
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case FileMenu.NEW_PROJECT:
                viewComponent.show(uiStage);
                break;
            case NewProjectDialog.CREATE_BTN_CLICKED:
                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                int originWidth = Integer.parseInt(viewComponent.getOriginWidth());
                int originHeight = Integer.parseInt(viewComponent.getOriginHeight());
                int pixelPerWorldUnit = Integer.parseInt(viewComponent.getPixelPerWorldUnit());
                projectManager.createNewProject(notification.getBody(), originWidth, originHeight, pixelPerWorldUnit);
                //TODO: this should be not here
                sandbox.loadCurrentProject();
                viewComponent.hide();
                break;
        }

    }
}
