package games.rednblack.editor.view.ui;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import games.rednblack.editor.utils.AppConfig;
import games.rednblack.h2d.common.view.ui.StandardWidgetsFactory;

public class UIWindowTitle extends VisTable {

    private VisLabel title;

    public UIWindowTitle() {
        setBackground(VisUI.getSkin().getDrawable("menu-bg"));
        title = StandardWidgetsFactory.createLabel("HyperLap2D - Beta v" + AppConfig.getInstance().versionString, "default", Align.center);
        title.setEllipsis(true);
        title.setWrap(true);
        title.setTouchable(Touchable.disabled);
        add(title).growX();
        setTouchable(Touchable.enabled);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }
}
