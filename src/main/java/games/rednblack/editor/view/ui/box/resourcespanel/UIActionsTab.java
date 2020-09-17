package games.rednblack.editor.view.ui.box.resourcespanel;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.view.ui.box.resourcespanel.draggable.DraggableResource;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UIActionsTab extends UIResourcesTab {

    private VisTable list;

    @Override
    public String getTabTitle() {
        return "Actions";
    }

    @Override
    public String getTabIconStyle() {
        return "action-button";
    }

    @Override
    protected VisScrollPane crateScrollPane() {
        list = new VisTable();
        return StandardWidgetsFactory.createScrollPane(list);
    }

    public void setItems(Array<DraggableResource> items) {
        list.clear();
        for (DraggableResource box : items) {
            list.add((Actor) box.getViewComponent()).expandX().fillX();
            list.row();
        }
    }
}
