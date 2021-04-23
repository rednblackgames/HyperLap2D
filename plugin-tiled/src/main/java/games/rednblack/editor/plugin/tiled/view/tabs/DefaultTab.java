package games.rednblack.editor.plugin.tiled.view.tabs;

import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.kotcrab.vis.ui.widget.VisLabel;
import games.rednblack.editor.plugin.tiled.TiledPanel;
import games.rednblack.h2d.common.view.ui.widget.imagetabbedpane.ImageTab;

/**
 * Created by mariam on 10/30/15.
 */
public class DefaultTab extends ImageTab {

    protected TiledPanel panel;
    protected int tabIndex;
    protected Table content = new Table();
    protected String tabTitle = "";

    public DefaultTab(TiledPanel panel, String tabTitle, int tabIndex) {
        super(false, false); //tab is not savable, tab is not closeable by user
        this.panel = panel;
        this.tabTitle = tabTitle;
        this.tabIndex = tabIndex;
    }

    public void initView() {
        content.add(new VisLabel(tabTitle+" example"));
    }

    @Override
    public String getTabTitle () {
        return tabTitle;
    }

    @Override
    public String getTabIconStyle() {
        return null;
    }

    @Override
    public Table getContentTable () {
        return content;
    }

    public int getTabIndex() {
        return tabIndex;
    }
}
