package games.rednblack.editor.view.ui.box;

import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.HyperLap2DFacade;

public abstract class UIBaseBox extends VisTable {

    protected final HyperLap2DFacade facade;

    public UIBaseBox() {
        super();
        facade = HyperLap2DFacade.getInstance();
    }

    public abstract void update();
}
