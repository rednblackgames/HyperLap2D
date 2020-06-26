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

import games.rednblack.editor.view.menu.FileMenu;
import games.rednblack.h2d.common.MsgAPI;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.ResolutionManager;
import games.rednblack.editor.view.menu.HyperLap2DMenuBar;
import games.rednblack.editor.view.stage.UIStage;

/**
 * Created by sargis on 4/6/15.
 */
public class ExportSettingsDialogMediator extends SimpleMediator<ExportSettingsDialog> {
    private static final String TAG = ExportSettingsDialogMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public ExportSettingsDialogMediator() {
        super(NAME, new ExportSettingsDialog());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                FileMenu.EXPORT_SETTINGS,
                ExportSettingsDialog.SAVE_SETTINGS_BTN_CLICKED,
                ExportSettingsDialog.SAVE_SETTINGS_AND_EXPORT_BTN_CLICKED
        };
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case FileMenu.EXPORT_SETTINGS:
                viewComponent.setExportPath();
                viewComponent.show(uiStage);
                break;
            case ExportSettingsDialog.SAVE_SETTINGS_AND_EXPORT_BTN_CLICKED:
                saveExportSettings(notification.getBody());
                exportProject(notification.getBody());
                viewComponent.hide();
                break;
            case ExportSettingsDialog.SAVE_SETTINGS_BTN_CLICKED:
                saveExportSettings(notification.getBody());
                viewComponent.hide();
                break;
        }
    }

    private void exportProject(ExportSettingsDialog.ExportSettingsVO settingsVO) {
        saveExportSettings(settingsVO);

        facade.sendNotification(MsgAPI.ACTION_EXPORT_PROJECT);
    }

    private void saveExportSettings(ExportSettingsDialog.ExportSettingsVO settingsVO) {
        if(settingsVO.fileHandle == null) return;

        ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
        projectManager.setTexturePackerSizes(settingsVO.width, settingsVO.height);
        projectManager.setTexturePackerDuplicate(settingsVO.duplicate);
        ResolutionManager resolutionManager = facade.retrieveProxy(ResolutionManager.NAME);
        resolutionManager.rePackProjectImagesForAllResolutions();

        facade.sendNotification(MsgAPI.SAVE_EXPORT_PATH, settingsVO.fileHandle.file().getAbsolutePath());
    }
}