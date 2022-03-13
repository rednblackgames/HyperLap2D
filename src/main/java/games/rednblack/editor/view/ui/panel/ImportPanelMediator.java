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
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.file.FileTypeFilter;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.utils.AssetIOManager;
import games.rednblack.editor.utils.AssetsUtils;
import games.rednblack.editor.view.menu.ResourcesMenu;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.editor.view.stage.UIStage;
import games.rednblack.h2d.common.MsgAPI;
import games.rednblack.h2d.common.ProgressHandler;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImportPanelMediator extends Mediator<ImportPanel> {
    private static final String TAG = ImportPanelMediator.class.getCanonicalName();
    private static final String NAME = TAG;

    public ImportPanelMediator() {
        super(NAME, new ImportPanel());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        AssetIOManager.getInstance().setProgressHandler(new AssetsImportProgressHandler());
        AssetIOManager.getInstance().setViewComponent(viewComponent);
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                ResourcesMenu.IMPORT_TO_LIBRARY,
                ImportPanel.BROWSE_BTN_CLICKED,
                ImportPanel.IMPORT_FAILED,
                MsgAPI.ACTION_FILES_DROPPED,
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        Sandbox sandbox = Sandbox.getInstance();
        UIStage uiStage = sandbox.getUIStage();
        switch (notification.getName()) {
            case ResourcesMenu.IMPORT_TO_LIBRARY:
                viewComponent.show(uiStage);
                break;
            case ImportPanel.BROWSE_BTN_CLICKED:
                showFileChoose();
                break;
            case MsgAPI.ACTION_FILES_DROPPED:
                ImportPanel.DropBundle bundle = notification.getBody();
                if (viewComponent.checkDropRegionHit(bundle.pos)) {
                    AssetIOManager.getInstance().setProgressHandler(new AssetsImportProgressHandler());
                    AssetIOManager.getInstance().postPathObtainAction(bundle.paths);
                }
                break;
            case ImportPanel.IMPORT_FAILED:
                viewComponent.showError(AssetsUtils.TYPE_FAILED);
                break;
        }
    }

    private void showFileChoose() {
        facade.sendNotification(MsgAPI.SHOW_BLACK_OVERLAY);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                FileTypeFilter.Rule allSupportedRule = AssetsUtils.getInstance().getFileTypeFilter().getRules().get(0);
                PointerBuffer aFilterPatterns = stack.mallocPointer(allSupportedRule.getExtensions().size);
                for (String ext : new Array.ArrayIterator<>(allSupportedRule.getExtensions())) {
                    aFilterPatterns.put(stack.UTF8("*." + ext));
                }
                aFilterPatterns.flip();

                SettingsManager settingsManager = facade.retrieveProxy(SettingsManager.NAME);
                FileHandle importPath = (settingsManager.getImportPath() == null || !settingsManager.getImportPath().exists()) ?
                        Gdx.files.absolute(System.getProperty("user.home")) : settingsManager.getImportPath();

                String files = TinyFileDialogs.tinyfd_openFileDialog("Import Resources...", importPath.path(), aFilterPatterns, allSupportedRule.getDescription(), true);
                Gdx.app.postRunnable(() -> {
                    facade.sendNotification(MsgAPI.HIDE_BLACK_OVERLAY);
                    if (files != null) {
                        String[] paths = files.split("\\|");
                        if(paths.length > 0) {
                            AssetIOManager.getInstance().setProgressHandler(new AssetsImportProgressHandler());
                            AssetIOManager.getInstance().postPathObtainAction(paths);
                        }
                    }
                });
            }
        });
        executor.shutdown();
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
