package games.rednblack.editor.event;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import games.rednblack.puremvc.Facade;

/**
 * Created by CyberJoe on 4/21/2015.
 */

public class MenuItemListener extends ChangeListener {

    private final String menuCommand;
    private final String menuType;
    private final Object data;

    private final Facade facade;

    public MenuItemListener(String menuCommand) {
        this(menuCommand, null, null);
    }

    public MenuItemListener(String menuCommand, String data) {
        this(menuCommand, data, null);
    }

    public MenuItemListener(String menuCommand, Object data, String menuType) {
        this.menuCommand = menuCommand;
        this.data = data;
        this.menuType = menuType;

        facade = Facade.getInstance();
    }

    @Override
    public void changed(ChangeEvent event, Actor actor) {
        if(menuType == null) {
            if(data == null) {
                facade.sendNotification(menuCommand);
            } else {
                facade.sendNotification(menuCommand, data);
            }
        } else {
            facade.sendNotification(menuCommand, data, menuType);
        }

    }
}