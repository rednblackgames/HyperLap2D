package games.rednblack.editor.view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.kotcrab.vis.ui.widget.MenuItem;
import games.rednblack.editor.event.MenuItemListener;
import games.rednblack.editor.utils.KeyBindingsLayout;
import games.rednblack.h2d.common.MsgAPI;

import static games.rednblack.h2d.common.MenuAPI.HELP_MENU;

public class HelpMenu extends H2DMenu {

    public static final String ABOUT_DIALOG_OPEN = HyperLap2DMenuBar.prefix + ".ABOUT_DIALOG_OPEN";

    public HelpMenu() {
        super("Help");
        MenuItem docs = new MenuItem("Documentation...", new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.net.openURI("https://hyperlap2d.rednblack.games/wiki/");
            }
        });
        addItem(docs);

        MenuItem console = new MenuItem("Console", new MenuItemListener(MsgAPI.OPEN_CONSOLE, null, HELP_MENU))
                .setShortcut(KeyBindingsLayout.getShortcutList(KeyBindingsLayout.OPEN_CONSOLE));
        addItem(console);
        addSeparator();

        MenuItem about = new MenuItem("About", new MenuItemListener(ABOUT_DIALOG_OPEN, null, HELP_MENU));
        addItem(about);
    }

    @Override
    public void setProjectOpen(boolean open) {

    }
}
