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

package games.rednblack.editor.view.ui.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileChooser;
import com.kotcrab.vis.ui.widget.file.FileChooserAdapter;
import com.puremvc.patterns.mediator.SimpleMediator;
import com.puremvc.patterns.observer.Notification;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.AssetImporter;
import games.rednblack.editor.utils.ImportUtils;
import games.rednblack.editor.view.frame.FileDropListener;
import games.rednblack.editor.view.menu.FileMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.ProgressHandler;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.view.ui.widget.HyperLapFileChooser;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;

public class ImportPanelMediator extends SimpleMediator<ImportPanel> {
    private static final String TAG = ImportPanelMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public ImportPanelMediator() {
        super(NAME, new ImportPanel());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        AssetImporter.getInstance().setProgressHandler(new AssetsImportProgressHandler());
        AssetImporter.getInstance().setViewComponent(viewComponent);
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                FileMenu.IMPORT_TO_LIBRARY,
                ImportPanel.BROWSE_BTN_CLICKED,
                ImportPanel.CANCEL_BTN_CLICKED,
                ImportPanel.IMPORT_BTN_CLICKED,
                ImportPanel.IMPORT_FAILED,
                FileDropListener.ACTION_DRAG_ENTER,
                FileDropListener.ACTION_DRAG_OVER,
                FileDropListener.ACTION_DRAG_EXIT,
                FileDropListener.ACTION_DROP,
        };
    }

    public Vector2 getLocationFromDtde(DropTargetDragEvent dtde) {
        Vector2 pos = new Vector2((float)(dtde).getLocation().getX(),(float)(dtde).getLocation().getY());

        return pos;
    }

    public Vector2 getLocationFromDropEvent(DropTargetDropEvent dtde) {
        Vector2 pos = new Vector2((float)(dtde).getLocation().getX(),(float)(dtde).getLocation().getY());

        return pos;
    }

    @Override
    public void handleNotification(Notification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case FileMenu.IMPORT_TO_LIBRARY:
                viewComponent.show(uiStage);
                break;
            case ImportPanel.BROWSE_BTN_CLICKED:
                showFileChoose();
                break;
            case FileDropListener.ACTION_DRAG_ENTER:
                //TODO for new drop system in LWJGL3
                /*Vector2 dropPos = getLocationFromDtde(notification.getBody());
                if(viewComponent.checkDropRegionHit(dropPos)) {
                    viewComponent.dragOver();
                }*/
                break;
            case FileDropListener.ACTION_DRAG_OVER:
                //TODO for new drop system in LWJGL3
                /*dropPos = getLocationFromDtde(notification.getBody());
                if(viewComponent.checkDropRegionHit(dropPos)) {
                    viewComponent.dragOver();
                }*/
                break;
            case FileDropListener.ACTION_DRAG_EXIT:
                //TODO for new drop system in LWJGL3
                /*dropPos = getLocationFromDtde(notification.getBody());
                if(viewComponent.checkDropRegionHit(dropPos)) {
                    viewComponent.dragExit();
                }*/
                break;
            case FileDropListener.ACTION_DROP:
                ImportPanel.DropBundle bundle = notification.getBody();
                if(viewComponent.checkDropRegionHit(bundle.pos)) {
                    AssetImporter.getInstance().setProgressHandler(new AssetsImportProgressHandler());
                    AssetImporter.getInstance().postPathObtainAction(bundle.paths);
                }
                break;
            case ImportPanel.CANCEL_BTN_CLICKED:
                viewComponent.setDroppingView();
                break;
            case ImportPanel.IMPORT_BTN_CLICKED:
                //startImport();
                break;
            case ImportPanel.IMPORT_FAILED:
                viewComponent.showError(ImportUtils.TYPE_FAILED);
                break;
        }
    }

    private void showFileChoose() {
         Sandbox sandbox = Sandbox.getInstance();
        FileChooser fileChooser = new HyperLapFileChooser(FileChooser.Mode.OPEN);

        fileChooser.setFileTypeFilter(ImportUtils.getInstance().getFileTypeFilter());

        SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
        FileHandle importPath = (settingsManager.getImportPath() == null || !settingsManager.getImportPath().exists()) ?
                Gdx.files.absolute(System.getProperty("user.home")) : settingsManager.getImportPath();
        fileChooser.setDirectory(importPath);

        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.setListener(new FileChooserAdapter() {
            @Override
            public void selected(Array<FileHandle> files) {
                String[] paths = new String[files.size];
                for(int i = 0; i < files.size; i++) {
                    paths[i] = files.get(i).path();
                }
                if(paths.length > 0) {
                    AssetImporter.getInstance().setProgressHandler(new AssetsImportProgressHandler());
                    AssetImporter.getInstance().postPathObtainAction(paths);
                }
            }
        });
        sandbox.getUIStage().addActor(fileChooser.fadeIn());
    }

    public class AssetsImportProgressHandler implements ProgressHandler {

        @Override
        public void progressStarted() {
            viewComponent.getProgressBar().setValue(0);
        }

        @Override
        public void progressChanged(float value) {
            viewComponent.getProgressBar().setValue(value);
        }

        @Override
        public void progressComplete() {
            Gdx.app.postRunnable(() -> {
                Sandbox sandbox = Sandbox.getInstance();
                ProjectManager projectManager = facade.retrieveProxy(ProjectManager.NAME);
                projectManager.openProjectAndLoadAllData(projectManager.getCurrentProjectPath());
                sandbox.loadCurrentProject();
                ImportPanelMediator.this.viewComponent.setDroppingView();
                facade.sendNotification(ProjectManager.PROJECT_DATA_UPDATED);
            });
        }

        @Override
        public void progressFailed() {
            Gdx.app.postRunnable(() -> {
                facade.sendNotification(ImportPanel.IMPORT_FAILED);
            });
        }
    }
}
