package games.rednblack.editor.view.menu;

import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;
import games.rednblack.editor.utils.KeyBindingsLayout;

import static games.rednblack.h2d.common.MenuAPI.EDIT_MENU;

public class EditMenu extends H2DMenu {

    public static final String CUT = HyperLap2DMenuBar.prefix + ".CUT";
    public static final String COPY = HyperLap2DMenuBar.prefix + ".COPY";
    public static final String PASTE = HyperLap2DMenuBar.prefix + ".PASTE";
    public static final String UNDO = HyperLap2DMenuBar.prefix + ".UNDO";
    public static final String REDO = HyperLap2DMenuBar.prefix + ".REDO";

    private final MenuItem cut;
    private final MenuItem copy;
    private final MenuItem paste;
    private final MenuItem undo;
    private final MenuItem redo;

    public EditMenu() {
        super("Edit");
        cut = new MenuItem("Cut", new MenuItemListener(CUT, null, EDIT_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.CUT));
        copy = new MenuItem("Copy", new MenuItemListener(COPY, null, EDIT_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.COPY));
        paste = new MenuItem("Paste", new MenuItemListener(PASTE, null, EDIT_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.PASTE));
        undo = new MenuItem("Undo", new MenuItemListener(UNDO, null, EDIT_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.UNDO));
        redo = new MenuItem("Redo", new MenuItemListener(REDO, null, EDIT_MENU)).setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.REDO));
        addItem(cut);
        addItem(copy);
        addItem(paste);
        addItem(undo);
        addItem(redo);
    }

    public void setProjectOpen(boolean open) {
        cut.setDisabled(!open);
        copy.setDisabled(!open);
        paste.setDisabled(!open);
        undo.setDisabled(!open);
        redo.setDisabled(!open);
    }

}
