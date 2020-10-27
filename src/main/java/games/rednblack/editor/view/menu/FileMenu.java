package games.rednblack.editor.view.menu;

import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.PopupMenu;
import games.rednblack.editor.data.manager.PreferencesManager;
import games.rednblack.editor.event.MenuItemListener;
import games.rednblack.editor.utils.KeyBindingsLayout;

import java.io.File;
import java.util.ArrayList;

public class FileMenu extends H2DMenu {

    public static final String FILE_MENU = HyperLap2DMenuBar.prefix + ".FILE_MENU";
    public static final String NEW_PROJECT = HyperLap2DMenuBar.prefix + ".NEW_PROJECT";
    public static final String OPEN_PROJECT = HyperLap2DMenuBar.prefix + ".OPEN_PROJECT";
    public static final String SAVE_PROJECT = HyperLap2DMenuBar.prefix + ".SAVE_PROJECT";
    public static final String IMPORT_TO_LIBRARY = HyperLap2DMenuBar.prefix + ".IMPORT_TO_LIBRARY";
    public static final String RECENT_PROJECTS = HyperLap2DMenuBar.prefix + ".RECENT_PROJECTS";
    public static final String CLEAR_RECENT = HyperLap2DMenuBar.prefix + ".CLEAR_RECENT";
    public static final String EXPORT = HyperLap2DMenuBar.prefix + ".EXPORT";
    public static final String SETTINGS = HyperLap2DMenuBar.prefix + ".SETTINGS";
    public static final String EXIT = HyperLap2DMenuBar.prefix + ".EXIT";

    private final MenuItem saveProject;
    private final MenuItem importToLibrary;
    private final MenuItem export;

    private final PopupMenu recentProjectsPopupMenu;
    private final Array<MenuItem> recentProjectsMenuItems;

    public FileMenu() {
        super("File"); //⌘⇧⌥
        saveProject = new MenuItem("Save Project", new MenuItemListener(SAVE_PROJECT, null, FILE_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.SAVE_PROJECT));
        addItem(new MenuItem("New Project...", new MenuItemListener(NEW_PROJECT, null, FILE_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.NEW_PROJECT)));
        addItem(new MenuItem("Open Project...", new MenuItemListener(OPEN_PROJECT, null, FILE_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.OPEN_PROJECT)));
        addItem(saveProject);
        //
        addSeparator();
        importToLibrary = new MenuItem("Import Resources...", new MenuItemListener(IMPORT_TO_LIBRARY, null, FILE_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.IMPORT_TO_LIBRARY));
        export = new MenuItem("Export", new MenuItemListener(EXPORT, null, FILE_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.EXPORT_PROJECT));
        addItem(importToLibrary);
        addItem(export);
        addItem(new MenuItem("Settings...", new MenuItemListener(SETTINGS, null, FILE_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.OPEN_SETTINGS)));
        //
        addSeparator();
        MenuItem recentProjectsMenuItem = new MenuItem("Recent Projects");
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
        if (recentProjectsMenuItems == null || recentProjectsPopupMenu == null)
            return;

        if (recentProjectsMenuItems.size != 0) {
            recentProjectsMenuItems.clear();
        }

        if (recentProjectsPopupMenu.hasChildren()) {
            recentProjectsPopupMenu.remove();
            recentProjectsPopupMenu.clearChildren();
        }

        addRecent(paths);
        if (paths.size() > 0) {
            recentProjectsPopupMenu.addSeparator();
        }

        MenuItem menuItem = new MenuItem("Clear history", new MenuItemListener(CLEAR_RECENT, null, FILE_MENU));
        recentProjectsMenuItems.add(menuItem);
        recentProjectsPopupMenu.addItem(menuItem);

        remove();
    }

    public void setProjectOpen(boolean open) {
        saveProject.setDisabled(!open);
        importToLibrary.setDisabled(!open);
        export.setDisabled(!open);
    }
}
