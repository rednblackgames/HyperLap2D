package games.rednblack.editor.view.ui.widget.actors.table;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class CellHeader extends Table {

    public CellHeader(String text) {
        this(StandardWidgetsFactory.createLabel(text, "default", Align.center, true));
    }

    public CellHeader(Actor content) {
        setBackground(VisUI.getSkin().getDrawable("layer-bg"));
        add(content).grow();
    }
}
