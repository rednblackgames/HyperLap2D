package games.rednblack.editor.view.menu;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import games.rednblack.editor.data.manager.PreferencesManager;
import games.rednblack.editor.event.MenuItemListener;
import games.rednblack.editor.renderer.data.SceneVO;

import java.io.File;
import java.util.ArrayList;

public class FileMenu extends H2DMenu {

    public static final String FILE_MENU = HyperLap2DMenuBar.prefix + ".FILE_MENU";
    public static final String NEW_PROJECT = HyperLap2DMenuBar.prefix + ".NEW_PROJECT";
    public static final String OPEN_PROJECT = HyperLap2DMenuBar.prefix + ".OPEN_PROJECT";
    public static final String SAVE_PROJECT = HyperLap2DMenuBar.prefix + ".SAVE_PROJECT";
    public static final String IMPORT_TO_LIBRARY = HyperLap2DMenuBar.prefix + ".IMPORT_TO_LIBRARY";
    public static final String RECENT_PROJECTS = HyperLap2DMenuBar.prefix + ".RECENT_PROJECTS";
    public static final String CLEAR_RECENTS = HyperLap2DMenuBar.prefix + ".CLEAR_RECENTS";
    public static final String EXPORT = HyperLap2DMenuBar.prefix + ".EXPORT";
    public static final String EXPORT_SETTINGS = HyperLap2DMenuBar.prefix + ".EXPORT_SETTINGS";
    public static final String EXIT = HyperLap2DMenuBar.prefix + ".EXIT";
    public static final String NEW_SCENE = HyperLap2DMenuBar.prefix + ".NEW_SCENE";
    public static final String SELECT_SCENE = HyperLap2DMenuBar.prefix + ".SELECT_SCENE";
    public static final String DELETE_CURRENT_SCENE = HyperLap2DMenuBar.prefix + ".DELETE_CURRENT_SCENE";
    
    private final PopupMenu scenesPopupMenu;
    private final Array<MenuItem> sceneMenuItems;
    private final MenuItem saveProject;
    private final MenuItem scenesMenuItem;
    private final MenuItem importToLibrary;
    private final MenuItem export;
    private final MenuItem exportSettings;

    private final PopupMenu recentProjectsPopupMenu;
    private final Array<MenuItem> recentProjectsMenuItems;
    private final MenuItem recentProjectsMenuItem;

    public FileMenu() {
        super("File");
        saveProject = new MenuItem("Save Project", new MenuItemListener(SAVE_PROJECT, null, FILE_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.S);
        addItem(new MenuItem("New Project", new MenuItemListener(NEW_PROJECT, null, FILE_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.N));
        addItem(new MenuItem("Open Project", new MenuItemListener(OPEN_PROJECT, null, FILE_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.O));
        addItem(saveProject);
        //saveProject.debug();
        //
        scenesMenuItem = new MenuItem("Scenes");
        scenesPopupMenu = new PopupMenu();

        scenesMenuItem.setSubMenu(scenesPopupMenu);
        addItem(scenesMenuItem);
        //
        addSeparator();
        importToLibrary = new MenuItem("Import Resources", new MenuItemListener(IMPORT_TO_LIBRARY, null, FILE_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.I);
        export = new MenuItem("Export", new MenuItemListener(EXPORT, null, FILE_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.E);
        exportSettings = new MenuItem("Export Settings", new MenuItemListener(EXPORT_SETTINGS, null, FILE_MENU));
        addItem(importToLibrary);
        addItem(export);
        addItem(exportSettings);
        //
        addSeparator();
        recentProjectsMenuItem = new MenuItem("Recent Projects...");
        recentProjectsPopupMenu = new PopupMenu();
        recentProjectsMenuItem.setSubMenu(recentProjectsPopupMenu);
        recentProjectsMenuItems = new Array<>();
        addItem(recentProjectsMenuItem);

        PreferencesManager prefs = PreferencesManager.getInstance();
        prefs.buildRecentHistory();
        reInitRecent(prefs.getRecentHistory());
        //
        addSeparator();
        addItem(new MenuItem("Exit", new MenuItemListener(EXIT, null , FILE_MENU)));
        sceneMenuItems = new Array<>();
    }

    public void addScenes(ArrayList<SceneVO> scenes) {
        for (SceneVO sceneVO : scenes) {
            MenuItem menuItem = new MenuItem(sceneVO.sceneName, new MenuItemListener(SELECT_SCENE, sceneVO.sceneName, FILE_MENU));
            sceneMenuItems.add(menuItem);
            scenesPopupMenu.addItem(menuItem);
        }
    }

    public void reInitScenes(ArrayList<SceneVO> scenes) {
        sceneMenuItems.clear();
        scenesPopupMenu.clear();
        scenesPopupMenu.addItem(new MenuItem("Create New Scene", new MenuItemListener(NEW_SCENE, null, FILE_MENU)));
        scenesPopupMenu.addItem(new MenuItem("Delete Current Scene", new MenuItemListener(DELETE_CURRENT_SCENE, null, FILE_MENU)));
        scenesPopupMenu.addSeparator();
        addScenes(scenes);
    }

    public String getFolderNameAndPath(String path) {
        File path1 = new File(path);
        File path2 = new File(path1.getParent());
        return path2.getName() + " - [ " + path + "]";
    }

    public void addRecent(ArrayList<String> paths) {
        for (String path : paths) {
            MenuItem menuItem = new MenuItem(getFolderNameAndPath(path) , new MenuItemListener(RECENT_PROJECTS, path, FILE_MENU));
            recentProjectsMenuItems.add(menuItem);
            recentProjectsPopupMenu.addItem(menuItem);
        }
    }

    public void reInitRecent(ArrayList<String> paths) {
        if (recentProjectsMenuItems != null && recentProjectsMenuItems.size != 0) {
            recentProjectsMenuItems.clear();
        }

        if (recentProjectsPopupMenu != null && recentProjectsPopupMenu.hasChildren()) {
            recentProjectsPopupMenu.remove();
            recentProjectsPopupMenu.clearChildren();
        }
        addRecent(paths);
        if (paths.size() > 0) {
            recentProjectsPopupMenu.addSeparator();
        }

        MenuItem menuItem = new MenuItem("Clear list", new MenuItemListener(CLEAR_RECENTS, null, FILE_MENU));
        recentProjectsMenuItems.add(menuItem);
        recentProjectsPopupMenu.addItem(menuItem);

        remove();
    }

    public void setProjectOpen(boolean open) {
        saveProject.setDisabled(!open);
        scenesMenuItem.setDisabled(!open);
        importToLibrary.setDisabled(!open);
        export.setDisabled(!open);
        exportSettings.setDisabled(!open);
    }

//        private class RecentProjectListener extends ChangeListener {
//            private final String path;
//
//            public RecentProjectListener(String path) {
//                this.path = path;
//            }
//
//            @Override
//            public void changed(ChangeEvent event, Actor actor) {
//                Gdx.app.log(TAG, "recentProject : " + path);
//                mediator.recentProjectItemClicked(path);
//            }
//        }
}
