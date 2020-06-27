package games.rednblack.editor.view.menu;

import com.badlogic.gdx.Input;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;

public class EditMenu extends H2DMenu {

    public static final String EDIT_MENU = HyperLap2DMenuBar.prefix + ".EDIT_MENU";
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
        cut = new MenuItem("Cut", new MenuItemListener(CUT, null, EDIT_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.X);
        copy = new MenuItem("Copy", new MenuItemListener(COPY, null, EDIT_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.C);
        paste = new MenuItem("Paste", new MenuItemListener(PASTE, null, EDIT_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.V);
        undo = new MenuItem("Undo", new MenuItemListener(UNDO, null, EDIT_MENU)).setShortcut(Input.Keys.CONTROL_LEFT, Input.Keys.Z);
        redo = new MenuItem("Redo", new MenuItemListener(REDO, null, EDIT_MENU)).setShortcut(Input.Keys.CONTROL_LEFT,Input.Keys.SHIFT_LEFT, Input.Keys.Z);
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
