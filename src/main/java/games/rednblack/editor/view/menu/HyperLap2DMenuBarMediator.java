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

package games.rednblack.editor.view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import games.rednblack.editor.HyperLap2DApp;
import games.rednblack.editor.HyperLap2DFacade;
import games.rednblack.editor.controller.commands.ShowNotificationCommand;
import games.rednblack.editor.data.manager.PreferencesManager;
import games.rednblack.editor.proxy.CommandManager;
import games.rednblack.editor.proxy.ProjectManager;
import games.rednblack.editor.proxy.SettingsManager;
import games.rednblack.editor.renderer.data.SceneVO;
import games.rednblack.editor.view.stage.Sandbox;
import games.rednblack.h2d.common.MenuAPI;
import games.rednblack.h2d.common.MsgAPI;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.puremvc.java.interfaces.INotification;
import org.puremvc.java.patterns.mediator.Mediator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sargis on 3/25/15.
 */
public class HyperLap2DMenuBarMediator extends Mediator<HyperLap2DMenuBar> {
    private static final String TAG = HyperLap2DMenuBarMediator.class.getCanonicalName();
    public static final String NAME = TAG;

    private ProjectManager projectManager;
    private SettingsManager settingsManager;

    public HyperLap2DMenuBarMediator() {
        super(NAME, new HyperLap2DMenuBar());
    }

    @Override
    public void onRegister() {
        super.onRegister();
        facade = HyperLap2DFacade.getInstance();
        projectManager = facade.retrieveProxy(ProjectManager.NAME);
        settingsManager = facade.retrieveProxy(SettingsManager.NAME);
    }

    @Override
    public String[] listNotificationInterests() {
        return new String[]{
                //FILE
                FileMenu.NEW_PROJECT,
                FileMenu.OPEN_PROJECT,
                FileMenu.SAVE_PROJECT,
                FileMenu.SAVE_PROJECT_AS,
                FileMenu.EXPORT,
                FileMenu.RECENT_PROJECTS,
                FileMenu.CLEAR_RECENT,
                FileMenu.EXIT,
                //EDIT
                EditMenu.CUT,
                EditMenu.COPY,
                EditMenu.PASTE,
                EditMenu.UNDO,
                EditMenu.REDO,
                //General
                ProjectManager.PROJECT_OPENED,
                HyperLap2DMenuBar.RECENT_LIST_MODIFIED,
                MsgAPI.CREATE,
                MsgAPI.AUTO_SAVE_PROJECT
        };
    }

    @Override
    public void handleNotification(INotification notification) {
        super.handleNotification(notification);
        String type = notification.getType();

        if (notification.getName().equals(HyperLap2DMenuBar.RECENT_LIST_MODIFIED)) {
            PreferencesManager prefs = PreferencesManager.getInstance();
            viewComponent.reInitRecent(prefs.getRecentHistory());
        }

        if (type == null) {
            handleGeneralNotification(notification);
            return;
        }
        switch (type) {
            case MenuAPI.FILE_MENU:
                handleFileMenuNotification(notification);
                break;
            case MenuAPI.EDIT_MENU:
                handleEditMenuNotification(notification);
                break;
            default:
                break;
        }
    }

    private void handleGeneralNotification(INotification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        switch (notification.getName()) {
            case ProjectManager.PROJECT_OPENED:
                onProjectOpened();
                break;
            case MsgAPI.CREATE:
                viewComponent.setProjectOpen(false);
                break;
            case MsgAPI.AUTO_SAVE_PROJECT:
                SceneVO vo = sandbox.sceneVoFromItems();
                projectManager.saveCurrentProject(vo);
                facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "Auto Save successfully", ShowNotificationCommand.TYPE_CLEAR_STACK);
                break;
        }
    }

    private void handleEditMenuNotification(INotification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        switch (notification.getName()) {
            case EditMenu.CUT:
                facade.sendNotification(MsgAPI.ACTION_CUT);
                break;
            case EditMenu.COPY:
                facade.sendNotification(MsgAPI.ACTION_COPY);
                break;
            case EditMenu.PASTE:
                facade.sendNotification(MsgAPI.ACTION_PASTE);
                break;
            case EditMenu.UNDO:
                CommandManager commandManager = facade.retrieveProxy(CommandManager.NAME);
                commandManager.undoCommand();
                break;
            case EditMenu.REDO:
                commandManager = facade.retrieveProxy(CommandManager.NAME);
                commandManager.redoCommand();
                break;
        }
    }

    private void handleFileMenuNotification(INotification notification) {
        Sandbox sandbox = Sandbox.getInstance();
        switch (notification.getName()) {
            case FileMenu.NEW_PROJECT:
                break;
            case FileMenu.OPEN_PROJECT:
                showOpenProject();
                break;
            case FileMenu.SAVE_PROJECT:
                SceneVO vo = sandbox.sceneVoFromItems();
                projectManager.saveCurrentProject(vo);
                facade.sendNotification(MsgAPI.SHOW_NOTIFICATION, "Project saved successfully");
                break;
            case FileMenu.SAVE_PROJECT_AS:
                projectManager.saveProjectAs();
                break;
            case FileMenu.RECENT_PROJECTS:
                recentProjectItemClicked(notification.getBody());
                break;
            case FileMenu.CLEAR_RECENT:
                clearRecent();
                break;
            case FileMenu.EXPORT:
                facade.sendNotification(MsgAPI.ACTION_EXPORT_PROJECT);
                break;
            case FileMenu.EXIT:
                HyperLap2DApp.getInstance().hyperlap2D.closeRequested();
                break;
        }
    }

    private void onProjectOpened() {
        viewComponent.setProjectOpen(true);
    }

    public void showOpenProject() {
        facade.sendNotification(MsgAPI.SHOW_BLACK_OVERLAY);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer aFilterPatterns = stack.mallocPointer(1);
                aFilterPatterns.put(stack.UTF8("*.h2d"));
                aFilterPatterns.flip();

                FileHandle workspacePath = (settingsManager.getWorkspacePath() == null || !settingsManager.getWorkspacePath().exists()) ?
                        Gdx.files.absolute(System.getProperty("user.home")) : settingsManager.getWorkspacePath();

                String projectPath = TinyFileDialogs.tinyfd_openFileDialog("Open HyperLap2D Project...", workspacePath.path(), aFilterPatterns, "HyperLap2D Project (*.h2d)", false);
                Gdx.app.postRunnable(() -> {
                    facade.sendNotification(MsgAPI.HIDE_BLACK_OVERLAY);
                    if (projectPath != null && projectPath.length() > 0) {
                        facade.sendNotification(MsgAPI.CHECK_EDITS_ACTION, (Runnable) () -> projectManager.openProjectFromPath(projectPath));
                    }
                });
            }
        });
        executor.shutdown();
    }

    public void recentProjectItemClicked(String path) {
        PreferencesManager prefs = PreferencesManager.getInstance();
        prefs.buildRecentHistory();
        prefs.pushHistory(path);

        facade.sendNotification(MsgAPI.CHECK_EDITS_ACTION, (Runnable) () -> projectManager.openProjectFromPath(path));
    }

    public void clearRecent() {
        PreferencesManager prefs = PreferencesManager.getInstance();
        prefs.clearHistory();
        viewComponent.reInitRecent(prefs.getRecentHistory());
    }

    public void addMenuItem(String menu, String subMenuName, String notificationName) {
        viewComponent.addMenuItem(menu, subMenuName, notificationName);
    }
}
