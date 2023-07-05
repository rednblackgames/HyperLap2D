package games.rednblack.editor.view.ui.box;

import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.puremvc.Facade;

public abstract class UIBaseBox extends VisTable {

    protected final Facade facade;

    public UIBaseBox() {
        super();
        facade = Facade.getInstance();
    }

    public abstract void update();
}
